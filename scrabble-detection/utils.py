import cv2
import joblib
import numpy as np
import json


class KNN:
    def __init__(self, classifier, scaler, le) -> None:
        self.classifier = classifier
        self.scaler = scaler
        self.le = le
        super().__init__()

    @staticmethod
    def load(model_name="knn_model4.joblib"):
        classifier, scaler, le = load_model(model_name)
        return KNN(classifier, scaler, le)

    def predict(self, img):
        return predict(img, self.classifier, self.scaler, self.le)

    def predict_3_labels(self, img):
        img = preprocess_image_for_prediction(img)
        img = np.array([img])
        img = self.scaler.transform(img)
        distances, values = self.classifier.kneighbors(img, n_neighbors=3)
        result = []
        for i in range(min(len(distances), len(values))):
            result.append((distances[0][i], self.le.inverse_transform([values[0][i]])))
        return result


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


def show_image(img, name=''):
    img = image_resize(img, width=600)
    cv2.imshow(name, img)
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


def show_image_with_predictions(img, predictions, grid_shape=(15, 15)):
    img_mask = np.ones_like(img) * 255
    img = draw_grid(img, grid_shape=grid_shape)
    img_mask = draw_grid(img_mask, grid_shape=grid_shape)
    h, w = img.shape[:2]
    cell_shape = (int(h / grid_shape[0]), int(w / grid_shape[1]))

    for coords, letter in predictions:
        draw_letter_at_grid_coords(img, letter.upper(), coords, cell_shape=cell_shape, thickness=2)
        draw_letter_at_grid_coords(img_mask, letter.upper(), coords, cell_shape=cell_shape, thickness=2)
    cv2.destroyAllWindows()
    show_image(img, 'original with mask')
    show_image(img_mask, 'mask')
    cv2.destroyAllWindows()


def draw_letter_at_grid_coords(img, letter, coords, cell_shape, color=(255, 0, 0), thickness=1):
    cell_h, cell_w = cell_shape
    grid_y, grid_x = coords
    x = cell_w * grid_x + int(0.25 * cell_w)
    y = cell_h * grid_y + int(0.7 * cell_h)

    font = cv2.FONT_HERSHEY_SIMPLEX
    font_scale = 1.5

    cv2.putText(img, letter, (x, y), font, font_scale, color, thickness, cv2.LINE_AA)


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


def warp_perspective_for_preprocessing(img, max_c, size=60):
    rect = cv2.minAreaRect(max_c)
    box = cv2.boxPoints(rect)
    angle = rect[2]
    # ul, ur, lr, ll for left tilt and no tilt (45 < angle <= 90)
    # ll, ul, ur, lr for right tilt (0 <= angle <= 45)

    # determine rotation:
    if 45 < angle <= 90:
        pts = np.float32([(0, 0), (size, 0), (size, size), (0, size)])
    elif 0 <= angle <= 45:
        pts = np.float32([(0, size), (0, 0), (size, 0), (size, size)])
    else:
        raise Exception("Bad tilt!")
    box = np.float32(box)
    matrix = cv2.getPerspectiveTransform(box, pts)
    result = cv2.warpPerspective(img, matrix, (size, size))
    return result


def preprocess_image_for_prediction(img, train=False, filename=None):
    if train:
        if img.ndim > 2:
            img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        contours = find_main_contours(img, 1)
        x, y, w, h = cv2.boundingRect(contours[0])
        img = img[0:y + h, x:x + w]
        while len(img) > 0 and np.sum(img[0]) == 0:
            img = img[1:]

        img = cv2.resize(img, (60, 60))
        _, img = cv2.threshold(img, 1, 255, cv2.THRESH_BINARY)
        if filename is not None:
            cv2.imwrite(filename, img)

    img = img.flatten()
    img = (img / 255).astype(int)
    return img


# input: 60x60 image
def predict(img, classifier, scaler, le):
    img = preprocess_image_for_prediction(img)
    img = np.array([img])
    img = scaler.transform(img)
    prediction = classifier.predict(img)
    result = le.inverse_transform(prediction)
    return result


def load_model(filename="knn_model.joblib"):
    classifier, scaler, le = joblib.load(filename)
    print(f"Model objects loaded from {filename}")

    return classifier, scaler, le


def rotate_image(img, angle):
    image_center = tuple(np.array(img.shape[1::-1]) / 2)
    rot_mat = cv2.getRotationMatrix2D(image_center, angle, 1.0)
    result = cv2.warpAffine(img, rot_mat, img.shape[1::-1], flags=cv2.INTER_LINEAR)
    return result


def find_main_contours(img, number=2):
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, reverse=True, key=lambda c: cv2.contourArea(c))
    return contours[:number]


def add_padding(img, target_size):
    h, w = img.shape[:2]
    w_padding = target_size - w
    h_padding = target_size - h

    p_left = int(w_padding / 2)
    p_right = p_left if w_padding % 2 == 0 else p_left + 1
    p_top = int(h_padding / 2)
    p_bottom = p_top if h_padding % 2 == 0 else p_top + 1

    return cv2.copyMakeBorder(img, p_top, p_bottom, p_left, p_right, cv2.BORDER_CONSTANT, 0)


def export_predictions(predictions, filename, grid_size=(15, 15)):
    board = [['#' for _ in range(grid_size[1])] for _ in range(grid_size[0])]
    for coords, letter in predictions:
        y, x = coords
        board[y][x] = letter
    json_str = json.dumps(board, ensure_ascii=False)
    json_str = json_str[1:-1]
    json_str = json_str.replace('],', '],\n')
    json_str = '[\n ' + json_str + '\n]'
    with open(f'tests/{filename}', 'w', encoding='utf-8') as f:
        f.write(json_str)
