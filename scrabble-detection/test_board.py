from pprint import pprint
import pytest
import os
import json
import cv2
import numpy as np
from scrabble_detector import KNN, detect_board_cells, predict_board_cells


class Comparison:

    def __init__(self, expected, actual, result) -> None:
        self.expected = expected
        self.actual = actual
        self.result = result

    def accuracy(self, include_empty=False):
        if include_empty:
            return np.sum(self.result == 1) / self.result.size
        sum_non_empty = np.sum(self.actual != '#')
        correct_non_empty = np.sum(
            [[val if self.actual[y][x] != '#' else 0 for x, val in enumerate(row)] for y, row in
             enumerate(self.result)])
        return correct_non_empty / sum_non_empty

    def failures(self, include_empty=False):
        failures = []
        stats = {}
        for y, row in enumerate(self.result):
            for x, res in enumerate(row):
                if res != 1:
                    if self.expected[y][x] == '#' and not include_empty:
                        continue
                    failures.append(((y, x), self.expected[y][x], self.actual[y][x]))
                    stats[self.expected[y][x]] = stats.get(self.expected[y][x], 0) + 1
        return failures, stats


def compare(expected_filename, predictions):
    expected = _load_board(expected_filename)
    if expected is None:
        raise FileNotFoundError('Expected file not found')
    actual = _convert_predictions(predictions)
    result = _compare(expected, actual)
    return Comparison(expected, actual, result)


def _load_board(filename):
    if not os.path.isfile(filename):
        raise FileNotFoundError('Expected file not found')
    result = []
    with open(filename, 'r', encoding='utf-8') as file:
        for line in file:
            row = []
            for char in line.strip():
                row.append(char)
            result.append(row)
    return np.array(result)


def _convert_predictions(predictions):
    board = [['#' for _ in range(15)] for _ in range(15)]
    for coords, letter in predictions:
        y, x = coords
        board[y][x] = letter
    return np.array(board)


def _compare(expected, actual):
    if len(expected) != len(actual) or len(expected[0]) != len(actual[0]):
        raise ValueError(f'Incompatible sizes. Expected: ({len(expected)}, {len(expected[0])}), got ({len(actual)}, {len(actual[0])})')
    return np.array([[int(letter == actual[y][x]) for x, letter in enumerate(row)] for y, row in enumerate(expected)])
# def main():
    # global knn
    # knn = KNN.load()
    #
    # board_name = 'board7'
    # img = cv2.imread(f'{board_name}.png')
    # # show_image(img)
    # cells, board = detect_board_cells(img)
    # predictions = predict_board_cells(cells)
    # # export_predictions(predictions, f'{board_name}.json')
    # # show_image_with_predictions(board, predictions)
    # comparison = tests.compare(f'tests/{board_name}.json', predictions)
    # # print(f'Accuracy (empty fields included): {comparison.accuracy(include_empty=True)*100:.2f}%')
    # print(f'Accuracy: {comparison.accuracy()*100:.2f}%')
    #
    # failures, stats = comparison.failures()
    # pprint(failures)
    #
    # # detect_board_cells(img, debug_fields=failures)


lookup_dir = 'tests'
parameters = [('6', 0.84), ('7', 0.86), ('8', 0.84), ('9', 0.93)]


@pytest.fixture(scope='session', autouse=True)
def knn_model():
    yield KNN.load("knn_model4.joblib")


@pytest.mark.parametrize("number, expected_accuracy", parameters)
def test_boards_fulfill_conditions(knn_model, number, expected_accuracy):
    board_path = f'{lookup_dir}/board{number}.txt'
    img_path = f'{lookup_dir}/board{number}.png'
    assert os.path.isfile(board_path) and os.path.isfile(img_path)

    img = cv2.imread(img_path)
    cells, board = detect_board_cells(img)
    predictions = predict_board_cells(cells, knn_model)
    # export_predictions(predictions, f'{}.json')
    # show_image_with_predictions(board, predictions)
    comparison = compare(board_path, predictions)
    print(f'Accuracy: {comparison.accuracy():.2f}, expected: {expected_accuracy}')
    print(f'Failures: {comparison.failures()}')
    assert comparison.accuracy() >= expected_accuracy

    failures, stats = comparison.failures()
    pprint(failures)


if __name__ == "__main__":
    pytest.main()
