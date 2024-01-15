import numpy as np
import cv2 as cv

from utils import show_image_with_contours

roi = cv.imread('tile_background.png')
assert roi is not None, "file could not be read, check with os.path.exists()"
hsv = cv.cvtColor(roi, cv.COLOR_BGR2HSV)
target = cv.imread('k.png')
assert target is not None, "file could not be read, check with os.path.exists()"

hsvt = cv.cvtColor(target, cv.COLOR_BGR2HSV)
# calculating object histogram
roihist = cv.calcHist([hsv], [0, 1], None, [240, 256], [0, 256, 0, 256])

# normalize histogram and apply backprojection
cv.normalize(roihist, roihist, 0, 255, cv.NORM_MINMAX)
dst = cv.calcBackProject([hsvt], [0, 1], roihist, [0, 2, 0, 256], 1)

# Now convolute with circular disc
disc = cv.getStructuringElement(cv.MORPH_ELLIPSE, (11, 11))
cv.filter2D(dst, -1, disc, dst)

# threshold and binary AND
ret, thresh = cv.threshold(dst, 50, 255, 0)
thresh = cv.merge((thresh, thresh, thresh))
res = cv.bitwise_and(target, thresh)

cv.imshow('res', res)
cv.waitKey(0)

contours, _ = cv.findContours(thresh, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
show_image_with_contours(res, contours)
