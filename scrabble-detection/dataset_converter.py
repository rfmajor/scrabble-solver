import cv2
import numpy as np

from utils import image_resize, warp_perspective


letters_with_alt = 'acenosyz'
letters = 'abcdefghijklmnoprstuwyz'


def read(name):
    img = cv2.imread(f'dataset/{name}.jpg')
    return image_resize(image=img, width=960)


def detect_letter(img):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    lower = np.array([18, 0, 95], np.uint8)
    upper = np.array([115, 255, 255], np.uint8)
    mask = cv2.inRange(hsv, lower, upper)

    kernel = np.ones((5, 5), np.uint8)
    opening = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    contours, _ = cv2.findContours(opening, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    max_c = max(contours, key=cv2.contourArea)

    roi = warp_perspective(img, max_c, size=60)

    gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
    _, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    filtered = remove_edges_and_noise(thresh)
    return filtered


def add_1px_border(img):
    img = img.copy()
    h, w = img.shape[:2]
    # rows
    img[0, :] = 0
    img[h - 1, :] = 0
    # columns
    img[:, 0] = 0
    img[:, w - 1] = 0
    return img


def remove_edges_and_noise(img):
    img = img.copy()
    h, w = img.shape[:2]
    for points in ((0, 0), (w - 1, 0), (w - 1, h - 1), (0, h - 1)):
        cv2.floodFill(img, None, points, (0, 0, 0))
    img = add_1px_border(img)
    return img


def main():
    for c in letters:
        for i in range(10):
            name = f'{c} ({i + 1})'
            img = read(name)
            img = detect_letter(img)
            cv2.imwrite(f'preprocessed_dataset/{name}.png', img)
        if c in letters_with_alt:
            for i in range(10):
                name = f'{c}_alt ({i + 1})'
                img = read(name)
                img = detect_letter(img)
                cv2.imwrite(f'preprocessed_dataset/{name}.png', img)


if __name__ == '__main__':
    main()
