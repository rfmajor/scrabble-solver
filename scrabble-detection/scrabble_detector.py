import cv2
import numpy as np
from utils import load_model, preprocess_image_for_prediction, rotate_image, dump_image, get_image_with_contours


alt_letters = {
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


class KNN:
    def __init__(self, classifier, scaler, le) -> None:
        self.classifier = classifier
        self.scaler = scaler
        self.le = le
        super().__init__()

    @staticmethod
    def load(model_name="knn_model4.joblib"):
        classifier, scaler, le = load_model(model_name)
        return KNN(classifier, scaler, le)

    def do_predict(self, img, report_dir=None):
        img = preprocess_image_for_prediction(img, report_dir=report_dir)
        img = np.array([img])
        img = self.scaler.transform(img)
        prediction = self.classifier.predict(img)
        result = self.le.inverse_transform(prediction)
        return result

    def predict_3_labels(self, img):
        img = preprocess_image_for_prediction(img)
        img = np.array([img])
        img = self.scaler.transform(img)
        distances, values = self.classifier.kneighbors(img, n_neighbors=3)
        result = []
        for i in range(min(len(distances), len(values))):
            result.append((distances[0][i], self.le.inverse_transform([values[0][i]])))
        return result


def detect_board_cells(img, report_dir=None):
    dump_image(img, '0_original', report_dir)
    result = []
    # convert the image to greyscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    dump_image(gray, '1_grey-board', report_dir)
    # get the threshold
    _, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    dump_image(thresh, '2_thresh', report_dir)

    # calculate the main contour which encloses the board area
    contours = find_main_contours(thresh)
    dump_image(get_image_with_contours(thresh, contours), '3_contoured', report_dir)

    # transform the image to a 1800x1800 square
    square = warp_perspective(img, contours, 1800)
    dump_image(square, '4_warped', report_dir)

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


def get_section_with_overflow_v2(img, y, x, h, w, y_o):
    y_start = y if y - y_o < 0 else y - y_o
    return img[y_start:y+h, x:x+w]


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


def find_main_contours(img, number=2):
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, reverse=True, key=lambda c: cv2.contourArea(c))
    return contours[:number]


def get_contour_center(contour):
    x, y, w, h = cv2.boundingRect(contour)
    return int(y + h / 2), int(x + w / 2)


def warp_perspective(img, contours, size):
    h, w = img.shape[:2]
    ul, ur, lr, ll = get_corner_coordinates_for_main_contour(contours, w, h)
    pts1 = np.float32([ul, ur, lr, ll])

    # points = (ul, ur, lr, ll)
    # show_image_with_points(img, points)

    pts2 = np.float32([[0, 0], [size, 0],
                       [size, size], [0, size]])

    matrix = cv2.getPerspectiveTransform(pts1, pts2)
    result = cv2.warpPerspective(img, matrix, (size, size))
    return result


def get_corner_coordinates_for_main_contour(contours, max_width, max_height):
    ul, ur, lr, ll = get_corner_coordinates(contours[0], max_width, max_height)

    upper_left = get_closest_point(ul[0], ul[1], contours[1])
    upper_right = get_closest_point(ur[0], ur[1], contours[1])
    lower_right = get_closest_point(lr[0], lr[1], contours[1])
    lower_left = get_closest_point(ll[0], ll[1], contours[1])
    return upper_left, upper_right, lower_right, lower_left


def get_corner_coordinates(contour, max_width, max_height):
    # TODO: implement a better way for finding corners
    upper_left = get_closest_point(0, 0, contour)
    upper_right = get_closest_point(max_width, 0, contour)
    lower_right = get_closest_point(max_width, max_height, contour)
    lower_left = get_closest_point(0, max_height, contour)
    return upper_left, upper_right, lower_right, lower_left


def get_closest_point(x, y, contour):
    return min(contour, key=lambda c: (c[0][0] - x) ** 2 + (c[0][1] - y) ** 2)[0]


def filter_letter_contours_by_position_from_center(img, contours, max_x_shift=1.0, max_y_shift=1.0):
    h, w = img.shape[:2]
    img_center = (h / 2, w / 2)
    result = []
    for c in contours:
        c_center = get_contour_center(c)
        if abs(c_center[0] - img_center[0]) < abs(max_y_shift * h) and \
            abs(c_center[1] - img_center[1]) < abs(max_x_shift * w):
            result.append(c)
    return result


def filter_letter_contours_by_size(img, contours, x_upper_bound, y_upper_bound, x_lower_bound=1.0, y_lower_bound=1.0,
                                   min_area_bound=1.0):
    height, width = img.shape[:2]
    # min_area = x_lower_bound * y_lower_bound * height * width
    # max_area = x_upper_bound * y_upper_bound * height * width
    result = []
    for c in contours:
        _, _, w, h = cv2.boundingRect(c)
        area = cv2.contourArea(c)
        if x_lower_bound * width < w < x_upper_bound * width and y_lower_bound * height < h < y_upper_bound * height \
                and min_area_bound * height * width < area:
            result.append(c)
    return result


def predict_board_cells(cells, knn, report_dir=None):
    result = []
    for coords, img in cells:
        if type(img) == str:
            result.append((coords, img))
        else:
            prediction = knn.do_predict(img, report_dir=report_dir)
            result.append((coords, map_prediction(prediction[0])))
    return result


def map_prediction(letter):
    if letter in alt_letters:
        return alt_letters[letter]
    return letter
