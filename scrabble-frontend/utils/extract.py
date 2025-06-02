import cv2 as cv
import numpy as np

filename = '../letters_all_2.png'
im = cv.imread(filename)
assert im is not None, "file could not be read, check with os.path.exists()"
dim = (900, 600)
# cv.imshow('letters', cv.resize(im, dim))
# cv.waitKey(0)
# cv.destroyAllWindows()

hsv = cv.cvtColor(im, cv.COLOR_BGR2HSV)
# (hMin = 0 , sMin = 0, vMin = 255), (hMax = 0 , sMax = 0, vMax = 255)
lower = np.array([0,0,255])
upper = np.array([0,0,255])

mask = cv.inRange(hsv, lower, upper)
# cv.imshow('letters', cv.resize(mask, dim))
# cv.waitKey(0)
# cv.destroyAllWindows()

res = cv.bitwise_and(im, im, mask=mask)
# cv.imshow('letters', cv.resize(res, dim))
# cv.waitKey(0)
# cv.destroyAllWindows()

gray = cv.cvtColor(res, cv.COLOR_BGR2GRAY)

contours, hierarchy = cv.findContours(gray, cv.RETR_LIST, cv.CHAIN_APPROX_SIMPLE)
# cv.drawContours(im, contours, -1, (0,255,0), 3)

letters = ['blank','ż_blank','ź_blank','z_blank','y_blank','w_blank','u_blank','t_blank','ś_blank','s_blank','r_blank','p_blank','ó_blank','o_blank','ń_blank','n_blank','m_blank','ł_blank','l_blank','k_blank','j_blank','i_blank','h_blank','g_blank','f_blank','ę_blank','e_blank','d_blank','ć_blank','c_blank','b_blank','ą_blank','a_blank',
           'ż','ź','z','y','w','u','t','ś','s','r','p','ó','o','ń','n','m','ł','l','k','j','i','h','g','f','ę','e','d','ć','c','b','ą','a']
for i, c in enumerate(contours[1:]):
    if i >= len(letters):
        break
    rect = cv.boundingRect(c)
    x,y,w,h = rect
    cropped = im[y: y+h, x: x+w]
    print(letters[i])
    # cv.imshow("letter", cropped)
    # cv.waitKey(0)
    # cv.destroyAllWindows()
    resized = cv.resize(cropped, (100,100))
    # get the image dimensions (height, width and channels)
    h, w, c = resized.shape
    # append Alpha channel -- required for BGRA (Blue, Green, Red, Alpha)
    image_bgra = np.concatenate([resized, np.full((h, w, 1), 255, dtype=np.uint8)], axis=-1)
    # create a mask where white pixels ([255, 255, 255]) are True
    white = np.all(resized == [255, 255, 255], axis=-1)
    # change the values of Alpha to 0 for all the white pixels
    image_bgra[white, -1] = 0
    # save the image
    cv.imwrite(letters[i]+".png", image_bgra)
