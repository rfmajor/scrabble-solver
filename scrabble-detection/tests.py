import os
import json
import numpy as np


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


def load_board(filename):
    if not os.path.isfile(filename):
        raise FileNotFoundError('Expected file not found')
    with open(filename, 'r', encoding='utf-8') as file:
        json_str = file.read()
        board = json.loads(json_str)
        return np.array(board)


def compare(expected_filename, predictions):
    expected = load_board(expected_filename)
    if expected is None:
        raise FileNotFoundError('Expected file not found')
    actual = _convert_predictions(predictions)
    result = _compare(expected, actual)
    return Comparison(expected, actual, result)


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






