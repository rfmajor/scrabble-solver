import cv2


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


def preprocess_image_for_prediction(img):
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    img = img.flatten()
    img = (img / 255).astype(int)
    return img
