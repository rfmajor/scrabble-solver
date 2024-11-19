import cv2
import numpy as np


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

