import cv2
import numpy as np
import cv2 as cv
from matplotlib import pyplot as plt

from utils import show_image

img = cv.imread('n.png')
assert img is not None, "file could not be read, check with os.path.exists()"
hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
lower = np.array([10, 60, 170], np.uint8)
upper = np.array([50, 255, 255], np.uint8)
gray = cv2.inRange(hsv, lower, upper)
show_image(gray)

# blur = cv2.medianBlur(gray, 5)

# show_image(blur)

# ret, thresh = cv.threshold(blur, 0, 255, cv.THRESH_BINARY + cv.THRESH_OTSU)

# show_image(thresh)

# noise removal
kernel = np.ones((3, 3), np.uint8)
opening = cv.morphologyEx(gray, cv.MORPH_OPEN, kernel, iterations=2)

show_image(opening)

# closing = cv.morphologyEx(opening, cv.MORPH_CLOSE, kernel, iterations=2)

# show_image(closing)

# sure background area
sure_bg = cv.dilate(opening, kernel, iterations=10)

show_image(sure_bg)

# Finding sure foreground area
dist_transform = cv.distanceTransform(opening, cv.DIST_L2, 3)
dist_output = cv2.normalize(dist_transform, None, 0, 1.0, cv2.NORM_MINMAX)

show_image(dist_output)

ret, sure_fg = cv.threshold(dist_transform, 0.2 * dist_transform.max(), 255, 0)
# sure_fg = cv.erode(opening, kernel, iterations=3)

show_image(sure_fg)

# Finding unknown region
sure_fg = np.uint8(sure_fg)
unknown = cv.subtract(sure_bg, sure_fg)

# Marker labelling
ret, markers = cv.connectedComponents(sure_fg)

# Add one to all labels so that sure background is not 0, but 1
markers = markers + 1

# Now, mark the region of unknown with zero
markers[unknown == 255] = 0

markers = cv.watershed(img, markers)
img[markers == -1] = [0, 255, 0]

show_image(img)
