import os
import pprint

import cv2
import numpy as np

from scrabble_detector import get_corner_coordinates_for_main_contour, get_corner_coordinates
from utils import show_image, show_image_with_points
from utils import find_main_contours
from utils import show_image_with_contours

img = cv2.imread('board15.png')
assert img is not None, "file could not be read, check with os.path.exists()"
gr = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
_, thresh = cv2.threshold(gr, 50, 255, cv2.THRESH_BINARY_INV)

hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
lower = np.array([24, 0, 0], np.uint8)
upper = np.array([106, 136, 170], np.uint8)
mask = cv2.inRange(hsv, lower, upper)

show_image(thresh)

masked = cv2.bitwise_and(hsv, hsv, mask=mask)

blur = cv2.medianBlur(mask, 17)

kernel = np.ones((9, 9), np.uint8)
close = cv2.morphologyEx(blur, cv2.MORPH_CLOSE, kernel, iterations=5)
show_image(close)
contours = find_main_contours(close, number=1)
show_image_with_contours(close, contours, thickness=2)

close = np.zeros_like(close)
cv2.drawContours(close, contours, -1, color=(255, 255, 255), thickness=cv2.FILLED)
show_image(close)

contours = find_main_contours(close, number=1)

epsilon = 0.08 * cv2.arcLength(contours[0], True)
approx = cv2.approxPolyDP(contours[0], epsilon, True)
points = []
for item in approx:
    y = item.flatten()[0]
    x = item.flatten()[1]
    points.append((y, x))

y_sort = sorted(points, key=lambda point: point[1])
top = y_sort[:2]
bottom = y_sort[2:4]

top.sort(key=lambda point: point[0])
bottom.sort(key=lambda point: point[0])

ul, ur = top[0], top[1]
ll, lr = bottom[0], bottom[1]

# show_image_with_points(img, points)
#
# cv2.drawContours(img, [approx], -1, (0, 255, 0), 4)

# show_image(img)


# show_image(img)

# dst = cv2.cornerHarris(close, 10, 31, 0.04)
# dst = cv2.dilate(dst, None)
# img[dst > 0.01 * dst.max()] = [0, 0, 255]

# corners = cv2.goodFeaturesToTrack(close, 25, 0.005, 10)
# corners = np.int0(corners)
# for i in corners:
#     x, y = i.ravel()
#     cv2.circle(img, (x, y), 9, 255, -1)

size = 1800
# h, w = img.shape[:2]
# ul, ur, lr, ll = get_corner_coordinates(contours[0], w, h)
#
pts1 = np.float32([ul, ur, lr, ll])

points = (ul, ur, lr, ll)
# show_image_with_points(img, points)
#
pts2 = np.float32([[0, 0], [size, 0],
                       [size, size], [0, size]])
#
matrix = cv2.getPerspectiveTransform(pts1, pts2)
result = cv2.warpPerspective(img, matrix, (size, size))

show_image(result)
