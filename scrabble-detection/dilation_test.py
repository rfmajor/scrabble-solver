import os

import cv2
import numpy as np

from utils import show_image
from utils import find_main_contours
from utils import show_image_with_contours


for filename in os.listdir('failed'):
    f = os.path.join('failed', filename)
    img = cv2.imread(f)
    assert img is not None, "file could not be read, check with os.path.exists()"
    img = cv2.resize(img, (60, 60))
    img = cv2.resize(img, (240, 240))
    show_image(img)
    blur = cv2.medianBlur(img, 5)
    show_image(blur)
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

    con = find_main_contours(close, 5)
    show_image_with_contours(img, con, thickness=2)

# orb = cv2.ORB.create()
# # find the keypoints with ORB
# kp = orb.detect(letter_only, None)
# # compute the descriptors with ORB
# kp, des = orb.compute(img, kp)
# # draw only keypoints location,not size and orientation
# img2 = cv2.drawKeypoints(img, kp, None, color=(0, 255, 0), flags=0)
# show_image(img2)


# opening = cv2.morphologyEx(gray, cv2.MORPH_OPEN, kernel, iterations=4)
# show_image(opening)
#

#
# erosion = cv2.erode(blur, kernel, iterations=6)
# show_image(erosion)
