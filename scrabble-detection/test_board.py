import cv2

import dataset_converter
from scrabble_detector import get_squares_rois, filter_letters_by_color, find_main_contours, \
    filter_letter_contours_by_position_from_center, filter_letter_contours_by_size, filter_cell, \
    get_roi_enclosed_by_contour, add_padding
from utils import show_image, show_image_with_points, show_image_with_contours, rotate_image, KNN, image_resize
from scrabble_detector import get_contour_center, warp_perspective
import numpy as np


knn = None


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
        roi_c = process_contours(filtered)

        if len(roi_c) > 0:
            rect = cv2.minAreaRect(roi_c[0])
            box = cv2.boxPoints(rect)
            box = np.int0(box)
            img_c = filtered.copy()
            img_c = cv2.cvtColor(img_c, cv2.COLOR_GRAY2BGR)
            cv2.drawContours(img_c, [box], 0, (0, 0, 255), 1)
            show_image(img_c)
            angle = rect[2]
            print(angle)
            if angle == 90 or angle == 45:
                angle = 0
            else:
                if 45 < angle < 90:
                    # o or ó
                    if 20 < angle < 70:
                        angle = 0
                    else:
                        angle = angle - 90
            rotated = rotate_image(filtered, angle)
            roi_c_r = process_contours(rotated)
            x, y, w, h = cv2.boundingRect(roi_c_r[0])
            new_r = rotated[max(y-13, 0):y + h, x:x + w]
            _, new_r = cv2.threshold(new_r, 1, 255, cv2.THRESH_BINARY)
            for points in ((0, 0), (int(w / 4), 0), (int(3*w / 4), 0), (w - 1, 0)):
                cv2.floodFill(new_r, None, points, (0, 0, 0))
            while len(new_r) > 0 and np.sum(new_r[0]) == 0:
                new_r = new_r[1:]
            new_r = cv2.resize(new_r, (60, 60))

            _, new_r = cv2.threshold(new_r, 1, 255, cv2.THRESH_BINARY)

            prediction = knn.predict(new_r)
            print(prediction)
            result.append((coords, prediction))
        else:
            result.append((coords, '#'))
    return result


def process_contours(img):
    roi_c = find_main_contours(img, number=3)
    roi_c = filter_letter_contours_by_size(img, roi_c, x_upper_bound=0.7, y_upper_bound=0.7,
                                           x_lower_bound=0.1, y_lower_bound=0.1, min_area_bound=0.03)
    roi_c = filter_letter_contours_by_position_from_center(img, roi_c, max_x_shift=0.3, max_y_shift=0.3)
    roi_c = roi_c[:1]
    return roi_c


def main():
    global knn
    knn = KNN.load()
    img = cv2.imread('board9.png')
    # img = image_resize(image=img, width=960)
    show_image(img)
    detect_board_cells(img)


if __name__ == '__main__':
    main()
