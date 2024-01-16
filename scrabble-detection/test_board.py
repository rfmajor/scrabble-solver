from pprint import pprint

import cv2

import dataset_converter
from scrabble_detector import get_squares_rois, filter_letters_by_color, find_main_contours, \
    filter_letter_contours_by_position_from_center, filter_letter_contours_by_size, \
    get_roi_enclosed_by_contour, add_padding
from utils import show_image, show_image_with_points, show_image_with_contours, rotate_image, KNN, image_resize, \
    show_image_with_predictions, export_predictions
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


def get_blanks(rois):
    result = []
    lower = np.array([12, 52, 0], np.uint8)
    upper = np.array([54, 255, 255], np.uint8)
    for coords, roi in rois:
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, lower, upper)
        hsv_roi = cv2.bitwise_and(roi, roi, mask=mask)
        h, s, v = cv2.split(hsv_roi)
        color = np.mean(v)
        if color > 180:
            result.append((coords, roi))
    return result


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


def detect_board_cells(img, debug_fields=[]):
    result = []
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    _, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    # show_image(thresh)
    contours = find_main_contours(thresh)

    # show_image_with_contours(thresh, contours)
    square = warp_perspective(img, contours, 1800)

    hsv = cv2.cvtColor(square, cv2.COLOR_BGR2HSV)
    lower = np.array([12, 52, 0], np.uint8)
    upper = np.array([54, 255, 255], np.uint8)
    mask = cv2.inRange(hsv, lower, upper)
    hsv_square = cv2.bitwise_and(square, square, mask=mask)
    show_image(hsv_square)

    rois = get_squares_rois_v2(square, overflow_percent=0.1)
    rois, blanks = filter_letters_by_black_color(rois)

    for coords, roi in rois:
        roi = cv2.resize(roi, (240, 240))
        debug = False
        debug_letter = None
        exp_letter = None
        for debug_coords, expected_letter, actual_letter in debug_fields:
            if coords == debug_coords:
                debug = True
                debug_letter = actual_letter
                exp_letter = expected_letter
                break
        if debug:
            print(f'Predicted letter: {debug_letter}')
            show_image(roi)
        roi_c = process_contours(roi, debug=debug)
        if debug:
            show_image_with_contours(roi, roi_c, thickness=1)

        if len(roi_c) > 0:
            rect = cv2.minAreaRect(roi_c[0])

            angle = rect[2]
            # print(angle)
            if 75 <= angle < 90 or 0 < angle <= 15:
                if 75 <= angle < 90:
                    angle = angle - 90
            else:
                angle = 0
            rotated = rotate_image(roi, angle)
            if debug:
                show_image(rotated)
            roi_c_r = process_contours(rotated, check_parts=True, debug=debug)
            if len(roi_c_r) > 0:
                if debug:
                    show_image_with_contours(rotated, roi_c_r, thickness=1)
                letter = get_area_from_contours(rotated, roi_c_r)
                # x, y, w, h = cv2.boundingRect(roi_c_r[0])
                # letter = rotated[y:y + h, x:x + w]
                letter_gray = cv2.split(letter)[2]
                _, letter_thresh = cv2.threshold(letter_gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
                if debug:
                    show_image(letter_thresh)
                letter_thresh, success = strip_sides(letter_thresh)
                if debug:
                    show_image(letter_thresh)

                letter_thresh = cv2.resize(letter_thresh, (60, 60))

                _, letter_thresh = cv2.threshold(letter_thresh, 1, 255, cv2.THRESH_BINARY)
                if debug:
                    show_image(letter_thresh)

            # prediction = knn.predict(new_r)
            # print(prediction)
            result.append((coords, letter_thresh))
        else:
            result.append((coords, '$'))
    for coords, roi in blanks:
        result.append((coords, '$'))
    return result, square


def get_section_with_overflow_v2(img, y, x, h, w, y_o):
    y_start = y if y - y_o < 0 else y - y_o
    return img[y_start:y+h, x:x+w]


def strip_sides(img, debug=False):
    original_img = img.copy()

    if debug:
        show_image(img)

    while len(img) > 0 and np.sum(img[0]) == 0:
        img = img[1:]

    while len(img) > 0 and len(img[0]) > 0 and np.sum(img[:, 0]) == 0:
        img = img[:, 1:]

    while len(img) > 0 and np.sum(img[-1]) == 0:
        img = img[:-1]

    while len(img) > 0 and len(img[-1]) > 0 and np.sum(img[:, -1]) == 0:
        img = img[:, :-1]

    if len(img) == 0:
        return original_img, True
    return img, False


def get_squares_rois_v2(img, grid_shape=(15, 15), overflow_percent=0.0):
    im_height, im_width = img.shape[:2]
    rois = []
    height, width = grid_shape

    roi_height = int(im_height / height)
    roi_width = int(im_width / width)
    y_o = round(overflow_percent * roi_height)

    for y in range(height):
        for x in range(width):
            y_start = y * roi_height
            x_start = x * roi_width
            rois.append(((y, x), get_section_with_overflow_v2(img, y_start, x_start, roi_height, roi_width, y_o)))
    return rois


def filter_cell(img, debug=False):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    lower = np.array([10, 0, 0], np.uint8)
    upper = np.array([179, 255, 170], np.uint8)
    mask = cv2.inRange(hsv, lower, upper)

    letter_only = cv2.bitwise_and(img, img, mask=mask)
    show_image(letter_only)
    letter_only = cv2.medianBlur(letter_only, 5)
    show_image(letter_only)
    gray = cv2.split(letter_only)[2]
    _, thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    show_image(thresh)
    kernel = np.ones((3, 3), np.uint8)
    dilate = cv2.dilate(thresh, kernel, iterations=2)
    show_image(dilate)
    close = cv2.morphologyEx(dilate, cv2.MORPH_CLOSE, kernel, iterations=2)
    show_image(close)
    return close

    #
    # h, s, gray = cv2.split(img)
    # if debug:
    #     show_image(gray)
    #
    # # blur = cv2.GaussianBlur(gray, (3, 3), 0)
    # blur = cv2.bitwise_not(gray)
    # if debug:
    #     show_image(blur)
    # # kernel = np.ones((3, 3), np.uint8)
    # # blur = cv2.morphologyEx(blur, cv2.MORPH_CLOSE, kernel, iterations=1)
    # blur = cv2.dilate(blur, (3, 3), iterations=1)
    # if debug:
    #     show_image(blur)
    # _, thresh = cv2.threshold(blur, 127, 255, cv2.THRESH_BINARY)
    # if debug:
    #     show_image(thresh)
    # # thresh = cv2.adaptiveThreshold(blur, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 13, -5)
    # # kernel = np.ones((3, 3), np.uint8)
    # # thresh = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)
    #
    # return thresh


def filter_letters_by_black_color(rois, debug_fields=[]):
    result = []
    blanks = []
    lower = np.array([12, 52, 0], np.uint8)
    upper = np.array([54, 255, 255], np.uint8)
    for coords, roi in rois:
        debug = False
        for debug_coords, expected_letter, actual_letter in debug_fields:
            if coords == debug_coords:
                debug = False
                break
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, lower, upper)
        hsv_roi = cv2.bitwise_and(roi, roi, mask=mask)
        h, s, v = cv2.split(hsv_roi)
        # _, v = cv2.threshold(v, 100, 255, cv2.THRESH_BINARY_INV)

        color = np.mean(v)

        if debug:
            show_image(roi)
            show_image(v)
            print(color)
        # h, w = v.shape[:2]
        # v_center = v[int(0.25 * h):int(0.75 * h), int(0.25 * w):int(0.75 * w)]
        # _, v_center = cv2.threshold(v_center, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        # if np.mean(v_center) > 240:
        #     blanks.append((coords, roi))
        if color > 127:
            result.append((coords, roi))
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


def process_contours(img, check_parts=False, debug=False):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    lower = np.array([10, 0, 0], np.uint8)
    upper = np.array([179, 255, 170], np.uint8)
    mask = cv2.inRange(hsv, lower, upper)

    letter_only = cv2.bitwise_and(img, img, mask=mask)
    if debug:
        show_image(letter_only)
    letter_only = cv2.medianBlur(letter_only, 5)
    if debug:
        show_image(letter_only)
    gray = cv2.split(letter_only)[2]
    if debug:
        show_image(gray)
    _, thresh = cv2.threshold(gray, 5, 255, cv2.THRESH_BINARY)
    if debug:
        show_image(thresh)
    kernel = np.ones((3, 3), np.uint8)
    dilate = cv2.dilate(thresh, kernel, iterations=2)
    if debug:
        show_image(dilate)
    close = cv2.morphologyEx(dilate, cv2.MORPH_CLOSE, kernel, iterations=2)
    if debug:
        show_image(close)

    roi_c = find_main_contours(close, number=5)
    if debug:
        show_image_with_contours(close, roi_c, thickness=1)
    roi_c_sized = filter_letter_contours_by_size(close, roi_c, x_upper_bound=0.7, y_upper_bound=0.7,
                                           x_lower_bound=0.08, y_lower_bound=0.1, min_area_bound=0.03)
    if debug:
        show_image_with_contours(close, roi_c_sized, thickness=1)
    roi_c_sized = filter_letter_contours_by_position_from_center(close, roi_c_sized, max_x_shift=0.35, max_y_shift=0.35)
    if debug:
        show_image_with_contours(close, roi_c_sized, thickness=1)
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
    if debug:
        show_image_with_contours(close, roi_c, thickness=1)
    return roi_c


def predict_board_cells(cells):
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


def main():
    global knn
    knn = KNN.load()
    board_name = 'board14'
    img = cv2.imread(f'{board_name}.png')
    show_image(img)
    cells, board = detect_board_cells(img)
    predictions = predict_board_cells(cells)
    # export_predictions(predictions, f'{board_name}.json')
    show_image_with_predictions(board, predictions)
    comparison = tests.compare(f'tests/{board_name}.json', predictions)
    # print(f'Accuracy (empty fields included): {comparison.accuracy(include_empty=True)*100:.2f}%')
    print(f'Accuracy: {comparison.accuracy()*100:.2f}%')

    failures, stats = comparison.failures(include_empty=True)
    pprint(failures)

    detect_board_cells(img, debug_fields=failures)


if __name__ == '__main__':
    main()
