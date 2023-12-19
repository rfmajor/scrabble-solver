import cv2

import dataset_converter
from scrabble_detector import *


def main():
    img = cv2.imread('board_warped2.png')
    # img = image_resize(image=img, width=960)
    show_image(img)
    rois = get_squares_rois(img, overflow_percent=0.15)
    rois = filter_letters_by_color(rois, multiplier=1.25, r_g_min_ratio=0.7, r_g_max_ratio=1.3)
    for _, roi in rois:
        show_image(roi)
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        lower = np.array([12, 0, 205], np.uint8)
        upper = np.array([179, 255, 255], np.uint8)
        mask = cv2.inRange(hsv, lower, upper)
        show_image(mask)


        kernel = np.ones((2, 2), np.uint8)
        erode = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=6)
        show_image(erode)
        blur = cv2.GaussianBlur(mask, (3, 3), 0)
        show_image(blur)
        edges = cv2.Canny(erode, 100, 200)
        show_image(edges)
        contours = find_main_contours(edges, number=2)
        show_image_with_contours(edges, contours, thickness=1)
        if len(contours) > 0:
            roi = dataset_converter.warp_perspective(roi, contours[0])
            show_image(roi)

    # show_image_with_contours(mask, contours)



if __name__ == '__main__':
    main()
