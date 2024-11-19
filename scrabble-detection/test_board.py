from pprint import pprint
import pytest
import os

import cv2

from scrabble_detector import find_main_contours, \
    filter_letter_contours_by_position_from_center, filter_letter_contours_by_size
from utils import rotate_image, KNN
from scrabble_detector import get_contour_center, warp_perspective
import numpy as np
import tests


knn = None
letters_with_alts_for_print = {
        'a_alt': 'a\'',
        'c_alt': 'c\'',
        'e_alt': 'e\'',
        'l_alt': 'l\'',
        'n_alt': 'n\'',
        'o_alt': 'o\'',
        's_alt': 's\'',
        'y_alt': 'y\'',
        'z_alt': 'z\''
}

letters_with_alts = {
        'a_alt': 'ą',
        'c_alt': 'ć',
        'e_alt': 'ę',
        'l_alt': 'ł',
        'n_alt': 'ń',
        'o_alt': 'ó',
        's_alt': 'ś',
        'y_alt': 'ź',
        'z_alt': 'ż'
}


"""
Extracts a rectangular area from the image based on its contours
"""
def get_area_from_contours(img, contours):
    if not len(contours):
        return img
    img_h, img_w = img.shape[:2]
    y_top = img_h
    x_left = img_w
    y_bottom = 0
    x_right = 0
    for cnt in contours:
        x, y, w, h = cv2.boundingRect(cnt)
        if y < y_top:
            y_top = y
        if x < x_left:
            x_left = x
        if y + h > y_bottom:
            y_bottom = y + h
        if x + w > x_right:
            x_right = x + w
    return img[y_top:y_bottom, x_left: x_right]


def detect_board_cells(img):
    result = []
    # convert the image to greyscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # get the threshold
    _, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    # calculate the main contour which encloses the board area
    contours = find_main_contours(thresh)

    # transform the image to a 1800x1800 square
    square = warp_perspective(img, contours, 1800)

    # get the array of image parts (ROIs)
    rois = get_squares_rois_v2(square, overflow_percent=0.1)
    rois, blanks = filter_letters_by_black_color(rois)

    for coords, roi in rois:
        roi = cv2.resize(roi, (240, 240))
        roi_c = process_contours(roi)

        # if the cell does not have any contours and is blank, append $, otherwise append @ - undefined
        if len(roi_c) == 0:
            if coords in blanks:
                result.append((coords, '$'))
            else:
                result.append((coords, '@'))
            continue
        # adjust tilt of the image based on the rotation angle of a min area rectangle
        rect = cv2.minAreaRect(roi_c[0])

        angle = rect[2]
        if 75 <= angle < 90 or 0 < angle <= 15:
            if 75 <= angle < 90:
                angle = angle - 90
        else:
            angle = 0
        rotated = rotate_image(roi, angle)

        # process contours on the tilted image
        roi_c_r = process_contours(rotated, check_parts=True)
        # if there are no contours, return undefined
        if len(roi_c_r) == 0:
            result.append((coords, '@'))
            continue

        # extract contoured area
        letter = get_area_from_contours(rotated, roi_c_r)

        # convert to gray, apply threshold and strip any unnecessary margins of black pixels
        letter_gray = cv2.split(letter)[2]
        _, letter_thresh = cv2.threshold(letter_gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
        letter_stripped = strip_sides(letter_thresh)
        if letter_stripped is None:
            result.append((coords, '@'))
            continue
        # resize the letter to 60x60 and apply a threshold
        letter_resized = cv2.resize(letter_stripped, (60, 60))
        _, letter_result = cv2.threshold(letter_resized, 1, 255, cv2.THRESH_BINARY)

        result.append((coords, letter_result))
    return result, square


def get_section_with_overflow_v2(img, y, x, h, w, y_o):
    y_start = y if y - y_o < 0 else y - y_o
    return img[y_start:y+h, x:x+w]


def strip_sides(img):
    while len(img) > 0 and np.sum(img[0]) == 0:
        img = img[1:]

    while len(img) > 0 and len(img[0]) > 0 and np.sum(img[:, 0]) == 0:
        img = img[:, 1:]

    while len(img) > 0 and np.sum(img[-1]) == 0:
        img = img[:-1]

    while len(img) > 0 and len(img[-1]) > 0 and np.sum(img[:, -1]) == 0:
        img = img[:, :-1]

    if len(img) == 0:
        return None
    return img


"""
Extracts the regions of interest (ROIs) of the cells contained in the board image. It has an optional overflow parameter
which controls how much excessive area is loaded above and under the cell (this is useful in a case when a part of the
letter sticks out of the supposed bounding box but we still need to capture it)
"""
def get_squares_rois_v2(img, grid_shape=(15, 15), overflow_percent=0.0):
    im_height, im_width = img.shape[:2]
    rois = []
    height, width = grid_shape

    roi_height = int(im_height / height)
    roi_width = int(im_width / width)
    # the y overflow
    y_o = round(overflow_percent * roi_height)

    for y in range(height):
        for x in range(width):
            y_start = y * roi_height
            x_start = x * roi_width
            rois.append(((y, x), get_section_with_overflow_v2(img, y_start, x_start, roi_height, roi_width, y_o)))
    return rois

def filter_letters_by_black_color(rois):
    result = []
    blanks = []
    lower = np.array([12, 52, 0], np.uint8)
    upper = np.array([54, 255, 255], np.uint8)
    for coords, roi in rois:
        # prepare a mask which contains only the bits between lower and upper thresholds
        # and apply bitwise AND with the original
        # TODO: adjust these thresholds based on the board image light level
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, lower, upper)
        hsv_roi = cv2.bitwise_and(roi, roi, mask=mask)
        h, s, v = cv2.split(hsv_roi)

        color = np.mean(v)

        # if the color of the cell is in the upper half of the black/white spectrum,
        # treat it as a non-empty cell (containing a letter)
        if color > 127:
            result.append((coords, roi))

        # get a sample from the center of the image, calculate its mean value
        # and classify as a blank if the threshold is exceeded
        h, w = v.shape[:2]
        v_center = v[int(0.25 * h):int(0.75 * h), int(0.25 * w):int(0.75 * w)]
        _, v_center = cv2.threshold(v_center, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        if np.mean(v_center) > 200:
            blanks.append(coords)
    return result, blanks


def find_letter_parts(c_main, contours, x_max_offset=1.0, y_max_offset=1.0):
    result = []
    c_main_left_x, c_main_top_y, c_main_w, c_main_h = cv2.boundingRect(c_main)
    c_main_middle_y, c_main_middle_x = get_contour_center(c_main)
    for cnt in contours:
        x, y, w, h = cv2.boundingRect(cnt)
        c_bottom_y = y + h
        c_middle_y, c_middle_x = get_contour_center(cnt)
        x_offset = abs(c_middle_x - c_main_middle_x)
        y_offset = abs(c_middle_y - c_main_middle_y)

        if c_bottom_y < c_main_top_y and x_offset < x_max_offset * c_main_w and y_offset < y_max_offset * c_main_h:
            result.append(cnt)
    return result


def process_contours(img, check_parts=False):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    lower = np.array([10, 0, 0], np.uint8)
    upper = np.array([179, 255, 170], np.uint8)
    mask = cv2.inRange(hsv, lower, upper)

    letter_only = cv2.bitwise_and(img, img, mask=mask)
    letter_only = cv2.medianBlur(letter_only, 5)
    gray = cv2.split(letter_only)[2]
    _, thresh = cv2.threshold(gray, 5, 255, cv2.THRESH_BINARY)
    kernel = np.ones((3, 3), np.uint8)
    dilate = cv2.dilate(thresh, kernel, iterations=2)
    close = cv2.morphologyEx(dilate, cv2.MORPH_CLOSE, kernel, iterations=2)

    roi_c = find_main_contours(close, number=5)
    roi_c_sized = filter_letter_contours_by_size(close, roi_c, x_upper_bound=0.7, y_upper_bound=0.7,
                                           x_lower_bound=0.08, y_lower_bound=0.1, min_area_bound=0.03)
    roi_c_sized = filter_letter_contours_by_position_from_center(close, roi_c_sized, max_x_shift=0.35, max_y_shift=0.35)
    if not len(roi_c_sized):
        return []
    c_main = roi_c_sized[0]
    if len(roi_c) > 1 and check_parts:
        roi_c_letter_parts = find_letter_parts(c_main, roi_c[1:], x_max_offset=0.125, y_max_offset=0.7)
        if len(roi_c_letter_parts):
            roi_c = [c_main] + roi_c_letter_parts
        else:
            roi_c = [c_main]
    else:
        roi_c = [c_main]
    return roi_c


def predict_board_cells(cells, knn):
    result = []
    for coords, img in cells:
        if type(img) == str:
            result.append((coords, img))
        else:
            prediction = knn.predict(img)
            result.append((coords, map_prediction(prediction[0])))
    return result


def map_prediction(letter):
    if letter in letters_with_alts:
        return letters_with_alts[letter]
    return letter


# def main():
    # global knn
    # knn = KNN.load()
    #
    # board_name = 'board7'
    # img = cv2.imread(f'{board_name}.png')
    # # show_image(img)
    # cells, board = detect_board_cells(img)
    # predictions = predict_board_cells(cells)
    # # export_predictions(predictions, f'{board_name}.json')
    # # show_image_with_predictions(board, predictions)
    # comparison = tests.compare(f'tests/{board_name}.json', predictions)
    # # print(f'Accuracy (empty fields included): {comparison.accuracy(include_empty=True)*100:.2f}%')
    # print(f'Accuracy: {comparison.accuracy()*100:.2f}%')
    #
    # failures, stats = comparison.failures()
    # pprint(failures)
    #
    # # detect_board_cells(img, debug_fields=failures)

lookup_dir = 'tests'
parameters = [('6', 0.84), ('7', 0.86), ('9', 0.93)]

@pytest.fixture(scope='session', autouse=True)
def knn_model():
    yield KNN.load("knn_model4.joblib")

@pytest.mark.parametrize("number, expected_accuracy", parameters)
def test_boards_fulfill_conditions(knn_model, number, expected_accuracy):
    board_path = f'{lookup_dir}/board{number}.json'
    img_path = f'{lookup_dir}/board{number}.png'
    assert os.path.isfile(board_path) and os.path.isfile(img_path)

    img = cv2.imread(img_path)
    cells, board = detect_board_cells(img)
    predictions = predict_board_cells(cells, knn_model)
    # export_predictions(predictions, f'{}.json')
    # show_image_with_predictions(board, predictions)
    comparison = tests.compare(board_path, predictions)
    print(f'Accuracy: {comparison.accuracy():.2f}, expected: {expected_accuracy}')
    print(f'Failures: {comparison.failures()}')
    assert comparison.accuracy() >= expected_accuracy

    failures, stats = comparison.failures()
    pprint(failures)


if __name__ == "__main__":
    pytest.main()
