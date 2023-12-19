import math

import cv2
import numpy as np
from scipy import ndimage
from scipy.spatial import distance


def show_image(img):
    img = image_resize(img, width=600)
    cv2.imshow('', img)
    cv2.waitKey(0)


def show_image_with_written_text(img, *texts):
    font = cv2.FONT_HERSHEY_SIMPLEX
    font_scale = 0.5
    thickness = 1

    img = img.copy()
    h, w = img.shape[:2]
    text_h = int((h - 20) / len(texts))
    for i in range(len(texts)):
        text, font_color = texts[i]
        cv2.putText(img, text, (0, 20 + text_h * i), font, font_scale, font_color, thickness, cv2.LINE_AA)
    show_image(img)


def show_image_with_contours(img, contours, thickness=3):
    img = img.copy()
    if img.ndim <= 2:
        img = cv2.cvtColor(img, cv2.COLOR_GRAY2RGB)
    if len(contours) > 0:
        cv2.drawContours(img, contours, -1, (0, 255, 0), thickness)
    show_image(img)


def show_image_with_contour_rectangles(img, contours, thickness=3):
    img = img.copy()
    if img.ndim <= 2:
        img = cv2.cvtColor(img, cv2.COLOR_GRAY2RGB)
    if len(contours) > 0:
        for contour in contours:
            x, y, w, h = cv2.boundingRect(contour)
            cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), thickness=thickness)
    show_image(img)


def show_image_with_points(img, points):
    img = img.copy()
    for x, y in points:
        img = cv2.circle(img, (x, y), radius=4, color=(0, 0, 255), thickness=-1)
    show_image(img)


def show_image_with_bounding_rect(img, contour):
    img = img.copy()
    img = cv2.cvtColor(img, cv2.COLOR_GRAY2RGB)
    x, y, w, h = cv2.boundingRect(contour)
    cv2.rectangle(img, (x, y), (x + w, y + h), (0, 0, 255), 3)
    show_image(img)


def image_resize(image, width=None, height=None, inter=cv2.INTER_AREA):
    dim = None
    (h, w) = image.shape[:2]

    if width is None and height is None:
        return image

    if width is None:
        r = height / float(h)
        dim = (int(w * r), height)
    else:
        r = width / float(w)
        dim = (width, int(h * r))

    resized = cv2.resize(image, dim, interpolation=inter)

    return resized


def find_main_contours(img, number=2):
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, reverse=True, key=lambda c: cv2.contourArea(c))
    return contours[:number]


def get_contour_closest_to_center(img, contours):
    h, w = img.shape[:2]
    img_center = (h / 2, w / 2)

    return get_contour_closest_to_point(contours, img_center)[0]


def get_contour_closest_to_point(contours, point):
    best_contour = None
    best_result = None
    best_i = -1
    for i in range(len(contours)):
        contour = contours[i]
        contour_center = get_contour_center(contour)
        if contour_center is None:
            continue
        distance_to_point = (distance.euclidean(point, contour_center))
        if best_result is None or distance_to_point < best_result:
            best_contour = contour
            best_result = distance_to_point
            best_i = i
    if best_contour is None:
        return []
    return [best_contour], best_result, best_i


def get_contour_center(contour):
    m = cv2.moments(contour)

    if m["m00"] == 0.0:
        return None
    c_x_center = int(m["m10"] / m["m00"])
    c_y_center = int(m["m01"] / m["m00"])
    return c_y_center, c_x_center


def warp_perspective(img, contours, size):
    h, w = img.shape[:2]
    ul, ur, lr, ll = get_corner_coordinates_for_main_contour(contours, w, h)
    pts1 = np.float32([ul, ur, lr, ll])

    points = (ul, ur, lr, ll)
    show_image_with_points(img, points)

    pts2 = np.float32([[0, 0], [size, 0],
                       [size, size], [0, size]])

    matrix = cv2.getPerspectiveTransform(pts1, pts2)
    result = cv2.warpPerspective(img, matrix, (size, size))
    return result


def draw_grid(img, grid_shape=(15, 15), color=(0, 255, 0), thickness=1):
    img = img.copy()
    h, w = img.shape[:2]
    rows, cols = grid_shape
    dy, dx = h / rows, w / cols

    for x in np.linspace(start=dx, stop=w-dx, num=cols-1):
        x = int(round(x))
        cv2.line(img, (x, 0), (x, h), color=color, thickness=thickness)

    for y in np.linspace(start=dy, stop=h-dy, num=rows-1):
        y = int(round(y))
        cv2.line(img, (0, y), (w, y), color=color, thickness=thickness)

    return img


def get_squares_rois(img, grid_shape=(15, 15), overflow_percent=0.0):
    im_height, im_width = img.shape[:2]
    rois = []
    height, width = grid_shape

    roi_height = int(im_height / height)
    roi_width = int(im_width / width)
    x_o = round(overflow_percent * roi_width)
    y_o = round(overflow_percent * roi_height)

    for y in range(height):
        for x in range(width):
            y_start = y * roi_height
            x_start = x * roi_width
            rois.append(((y, x), get_section_with_overflow(img, y_start, x_start, roi_height, roi_width, y_o, x_o)))
    return rois


def get_section_with_overflow(img, y, x, h, w, y_o, x_o):
    img_h, img_w = img.shape[:2]
    y_start = y if y - y_o < 0 else y - y_o
    x_start = x if x - x_o < 0 else x - x_o
    y_end = y + h if y + h + y_o >= img_h else y + h + y_o
    x_end = x + w if x + w + x_o >= img_w else x + w + x_o
    return img[y_start:y_end, x_start:x_end]


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


def get_roi_enclosed_by_contour(img, contour, top_pad=0.0, rescale=None):
    x, y, w, h = cv2.boundingRect(contour)
    pad_h = int(top_pad * h)
    pad_y = max(y - pad_h, 0)
    img = img[pad_y:y + h, x:x + w]
    if rescale is None:
        return img
    return image_resize(img, width=rescale, height=rescale)


def add_padding(img, target_size):
    h, w = img.shape[:2]
    w_padding = target_size - w
    h_padding = target_size - h

    p_left = int(w_padding / 2)
    p_right = p_left if w_padding % 2 == 0 else p_left + 1
    p_top = int(h_padding / 2)
    p_bottom = p_top if h_padding % 2 == 0 else p_top + 1

    return cv2.copyMakeBorder(img, p_top, p_bottom, p_left, p_right, cv2.BORDER_CONSTANT, 0)


def has_upper_letter_extensions(img, contour, width_percent, height_percent):
    x, y, w, h = cv2.boundingRect(contour)
    roi_w = int(width_percent * w)
    roi_h = int(height_percent * h)
    roi_h = roi_h if y - roi_h > 0 else y

    roi_x_1 = x + int((w - roi_w) / 2)
    roi_x_2 = roi_x_1 + roi_w
    roi_y_1 = y - roi_h
    roi_y_2 = y
    roi = img[roi_y_1:roi_y_2, roi_x_1:roi_x_2]

    contours, _ = cv2.findContours(roi, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if len(contours) == 0:
        return False
    max_cnt = max(contours, key=cv2.contourArea)
    _, c_y, c_w, c_h = cv2.boundingRect(max_cnt)
    if c_w * c_h < 0.3 * (roi_w * roi_h):
        return False

    return True


def detect_board_cells(img):
    result = []
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    _, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    show_image(thresh)
    contours = find_main_contours(thresh)

    show_image_with_contours(thresh, contours)
    square = warp_perspective(img, contours, 900)
    show_image(square)
    rois = get_squares_rois(square, overflow_percent=0.1)
    rois = filter_letters_by_color(rois, multiplier=1.25, r_g_min_ratio=0.7, r_g_max_ratio=1.3)
    for coords, roi in rois:
        filtered = filter_cell(roi)
        # show_image(filtered)
        roi_c = find_main_contours(filtered, number=3)
        roi_c = filter_letter_contours_by_size(filtered, roi_c, x_upper_bound=0.7, y_upper_bound=0.7,
                                               x_lower_bound=0.1, y_lower_bound=0.1, min_area_bound=0.03)
        roi_c = filter_letter_contours_by_position_from_center(filtered, roi_c, max_x_shift=0.3, max_y_shift=0.3)
        roi_c = roi_c[:1]
        if len(roi_c) > 0:
            show_image(roi)
            hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
            lower = np.array([0, 0, 0], np.uint8)
            upper = np.array([180, 255, 170], np.uint8)
            mask = cv2.inRange(hsv, lower, upper)
            show_image(mask)
            letter = get_roi_enclosed_by_contour(filtered, roi_c[0])
            padding = add_padding(letter, target_size=60)
            result.append((coords, padding))
            # show_image(padding)
        else:
            result.append((coords, '#'))
    return result


def filter_letters_by_color(rois, multiplier=1.25, r_g_min_ratio=0.0, r_g_max_ratio=5.0):
    result = []
    for coords, roi in rois:
        avg_r = int(np.mean(roi[:, :, 2]))
        avg_g = int(np.mean(roi[:, :, 1]))
        avg_b = int(np.mean(roi[:, :, 0]))
        avg_rg = (avg_r + avg_g) / 2
        r_g_ratio = avg_r / avg_g

        if avg_rg > avg_b * multiplier and r_g_min_ratio < r_g_ratio < r_g_max_ratio:
            result.append((coords, roi))
    return result


def filter_letter_contours_by_position_from_center(img, contours, max_x_shift=1.0, max_y_shift=1.0):
    h, w = img.shape[:2]
    img_center = (h / 2, w / 2)
    result = []
    for c in contours:
        c_center = get_contour_center(c)
        if c_center[0] - img_center[0] < max_x_shift * img_center[0] and \
            c_center[1] - img_center[1] < max_y_shift * img_center[1]:
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


def get_best_shift(img):
    cy, cx = ndimage.measurements.center_of_mass(img)

    rows, cols = img.shape
    shiftx = np.round(cols/2.0-cx).astype(int)
    shifty = np.round(rows/2.0-cy).astype(int)

    return shiftx, shifty


def shift(img, sx, sy):
    rows, cols = img.shape
    m = np.float32([[1, 0, sx], [0, 1, sy]])
    shifted = cv2.warpAffine(img, m, (cols, rows))
    return shifted


def filter_cell(img):
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (3, 3), 0)
    _, thresh = cv2.threshold(blur, 127, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    thresh = cv2.bitwise_not(thresh)
    return thresh


def trim(img):
    imgray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(imgray, (5, 5), 0)
    _, thresh = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    contour = max(contours, key=cv2.contourArea)
    x, y, w, h = cv2.boundingRect(contour)
    img = thresh[y:y+h, x:x+w]

    img = cv2.bitwise_not(img)
    img = cv2.GaussianBlur(img, (13, 13), 0)
    img = cv2.threshold(img, 100, 255, cv2.THRESH_BINARY)[1]

    while len(img) > 0 and np.sum(img[0]) == 0:
        img = img[1:]

    if len(img) == 0:
        return None

    while np.sum(img[:, 0]) == 0:
        img = np.delete(img, 0, 1)

    while np.sum(img[-1]) == 0:
        img = img[:-1]

    while np.sum(img[:, -1]) == 0:
        img = np.delete(img, -1, 1)

    rows, cols = img.shape

    if rows > cols:
        factor = 20.0 / rows
        rows = 20
        cols = int(round(cols * factor))
        img = cv2.resize(img, (cols, rows))
    else:
        factor = 20.0 / cols
        cols = 20
        rows = int(round(rows * factor))
        img = cv2.resize(img, (cols, rows))

    cols_padding = (int(math.ceil((28 - cols) / 2.0)), int(math.floor((28 - cols) / 2.0)))
    rows_padding = (int(math.ceil((28 - rows) / 2.0)), int(math.floor((28 - rows) / 2.0)))
    img = np.lib.pad(img, (rows_padding, cols_padding), 'constant')

    shiftx, shifty = get_best_shift(img)
    shifted = shift(img, shiftx, shifty)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    dilate = cv2.dilate(shifted, kernel, iterations=1)

    dilate = cv2.threshold(dilate, 100, 255, cv2.THRESH_BINARY)[1]

    img = dilate

    return img


def main():
    img = cv2.imread('board7.jpg')
    img = image_resize(image=img, width=960)
    show_image(img)

    detect_board_cells(img)


if __name__ == '__main__':
    main()
