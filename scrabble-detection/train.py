import os.path

import cv2
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import accuracy_score
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import StandardScaler
import joblib
from dataset_converter import preprocess_image_for_prediction


letters = np.array(['a', 'a_alt', 'b', 'c', 'c_alt', 'd', 'e', 'e_alt', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                    'n_alt', 'o', 'o_alt', 'p', 'r', 's', 's_alt', 't', 'u', 'w', 'y', 'y_alt', 'z', 'z_alt'])


def save_model(classifier, scaler, le, filename):
    joblib.dump((classifier, scaler, le), filename)
    print(f'Model objects saved to {filename}')


def load_model(filename="knn_model.joblib"):
    classifier, scaler, le = joblib.load(filename)
    print(f"Model objects loaded from {filename}")

    return classifier, scaler, le


def load_images():
    images = []
    labels = []
    for letter in letters:
        for i in range(10):
            img = cv2.imread(f'preprocessed_dataset/{letter} ({i + 1}).png')
            img = preprocess_image_for_prediction(img)
            images.append(img)
            labels.append(letter)
    return images, labels


def train(images, labels):
    le = LabelEncoder()

    y = le.fit_transform(labels)
    X = images

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=10)

    scaler = StandardScaler()
    scaler.fit(X_train)

    X_train = scaler.transform(X_train)
    X_test = scaler.transform(X_test)

    classifier = KNeighborsClassifier(n_neighbors=5)
    classifier.fit(X_train, y_train)
    y_pred = classifier.predict(X_test)

    accuracy = accuracy_score(y_test, y_pred)
    print(f'Accuracy: {accuracy * 100:.2f}%')
    return classifier, scaler, le


# input: 60x60 image
def predict(img, classifier, scaler, le):
    img = preprocess_image_for_prediction(img)
    img = np.array([img])
    img = scaler.transform(img)
    prediction = classifier.predict(img)
    result = le.inverse_transform(prediction)
    print(result)
    return result


def main():
    filename = "knn_model.joblib"
    if os.path.isfile(filename):
        classifier, scaler, le = load_model(filename)
    else:
        images, labels = load_images()
        classifier, scaler, le = train(images, labels)
        save_model(classifier, scaler, le)

    img = cv2.imread('preprocessed_dataset/g (3).png')
    result = predict(img, classifier, scaler, le)


if __name__ == '__main__':
    main()
