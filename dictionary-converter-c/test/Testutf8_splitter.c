#include "unity.h"
#include "unity_internals.h"
#include "utf8_splitter.h"
#include <stdio.h>

#define A_CHAR (0x61)
#define B_CHAR (0x62)
#define C_CHAR (0x63)
#define A_TAIL_CHAR_LEAD (0xC4)
#define A_TAIL_CHAR_CONT (0x85)
#define C_TAIL_CHAR_LEAD (0xC4)
#define C_TAIL_CHAR_CONT (0x87)
#define A_TAIL_UINT (0xC485)
#define C_TAIL_UINT (0xC487)

uint32_t *split;

void setUp(void) {
}

void tearDown(void) {
    free(split);
}

void test_islead_should_returnTrueForLead() {
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11000000));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11100000));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11110000));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11111000));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11111100));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11111110));
    TEST_ASSERT_TRUE(__utf8_is_lead(0b11111111));
}

void test_islead_should_returnFalseForNotLead() {
    TEST_ASSERT_FALSE(__utf8_is_lead(0b01000100));
    TEST_ASSERT_FALSE(__utf8_is_lead(0b10100000));
    TEST_ASSERT_FALSE(__utf8_is_lead(0b10110000));
}

void test_iscont_should_returnTrueForCont() {
    TEST_ASSERT_TRUE(__utf8_is_cont(0b10111111));
    TEST_ASSERT_TRUE(__utf8_is_cont(0b10011111));
    TEST_ASSERT_TRUE(__utf8_is_cont(0b10001111));
}

void test_iscont_should_returnFalseForNotCont() {
    TEST_ASSERT_FALSE(__utf8_is_cont(0b00111111));
    TEST_ASSERT_FALSE(__utf8_is_cont(0b11011111));
    TEST_ASSERT_FALSE(__utf8_is_cont(0b11101111));
}

void test_isascii_should_returnTrueForAscii() {
    TEST_ASSERT_TRUE(__utf8_is_ascii(0b00111111));
    TEST_ASSERT_TRUE(__utf8_is_ascii(0b01011111));
    TEST_ASSERT_TRUE(__utf8_is_ascii(0b00011111));
}

void test_isascii_should_returnFalseForNotAscii() {
    TEST_ASSERT_FALSE(__utf8_is_ascii(0b10111111));
    TEST_ASSERT_FALSE(__utf8_is_ascii(0b11011111));
    TEST_ASSERT_FALSE(__utf8_is_ascii(0b11101111));
}

void test_getnum_should_returnCorrectNum() {
    TEST_ASSERT_EQUAL(0xA1, __utf8_get_num((char[]){0xA1, 0, 0, 0, 0}));
    TEST_ASSERT_EQUAL(0xA1B2, __utf8_get_num((char[]){0xA1, 0xB2, 0, 0, 0}));
    TEST_ASSERT_EQUAL(0xA1B2C3, __utf8_get_num((char[]){0xA1, 0xB2, 0xC3, 0, 0}));
    TEST_ASSERT_EQUAL(0xA1B2C3D4, __utf8_get_num((char[]){0xA1, 0xB2, 0xC3, 0xD4, 0}));
}

void test_utf8split_should_returnCorrectValuesForAscii() {
    uint32_t expected[] = {A_CHAR, B_CHAR, C_CHAR};
    uint32_t *split = utf8_split("abc\n", 15);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 3);
}

void test_utf8split_should_returnCorrectValuesForUtf8() {
    uint32_t expected[] = {A_TAIL_UINT, C_TAIL_UINT};
    uint32_t *split =
        utf8_split((char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, C_TAIL_CHAR_LEAD, C_TAIL_CHAR_CONT, '\n'}, 15);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 2);
}

void test_utf8split_should_returnCorrectValuesForMixed() {
    uint32_t expected[] = {A_TAIL_UINT, A_CHAR, C_TAIL_UINT, B_CHAR};
    uint32_t *split = utf8_split(
        (char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, A_CHAR, C_TAIL_CHAR_LEAD, C_TAIL_CHAR_CONT, B_CHAR, '\n'}, 15);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 4);
}

void test_utf8split_should_skipCharsWhenLengthIsReached_1() {
    uint32_t expected[] = {A_TAIL_UINT, A_CHAR, 0};
    uint32_t *split = utf8_split(
        (char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, A_CHAR, C_TAIL_CHAR_LEAD, C_TAIL_CHAR_CONT, B_CHAR, '\n'}, 2);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 3);
}

void test_utf8split_should_skipCharsWhenLengthIsReached_2() {
    uint32_t expected[] = {A_TAIL_UINT, A_CHAR, C_TAIL_UINT, 0};
    uint32_t *split = utf8_split(
        (char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, A_CHAR, C_TAIL_CHAR_LEAD, C_TAIL_CHAR_CONT, B_CHAR, '\n'}, 3);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 4);
}

void test_utf8split_should_skipLeadByteAtTheEnd() {
    uint32_t expected[] = {A_TAIL_UINT, A_CHAR, 0};
    uint32_t *split = utf8_split((char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, A_CHAR, C_TAIL_CHAR_LEAD, '\n'}, 4);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 3);
}

void test_utf8split_should_skipLeadByteInTheMiddle() {
    uint32_t expected[] = {A_TAIL_UINT, A_CHAR, 0};
    uint32_t *split = utf8_split((char[]){A_TAIL_CHAR_LEAD, A_TAIL_CHAR_CONT, A_CHAR, C_TAIL_CHAR_LEAD, '\n'}, 4);
    TEST_ASSERT_EQUAL_UINT_ARRAY(expected, split, 3);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_islead_should_returnTrueForLead);
    RUN_TEST(test_islead_should_returnFalseForNotLead);
    RUN_TEST(test_iscont_should_returnTrueForCont);
    RUN_TEST(test_iscont_should_returnFalseForNotCont);
    RUN_TEST(test_isascii_should_returnTrueForAscii);
    RUN_TEST(test_isascii_should_returnFalseForNotAscii);
    RUN_TEST(test_getnum_should_returnCorrectNum);
    RUN_TEST(test_utf8split_should_returnCorrectValuesForAscii);
    RUN_TEST(test_utf8split_should_returnCorrectValuesForUtf8);
    RUN_TEST(test_utf8split_should_returnCorrectValuesForMixed);
    RUN_TEST(test_utf8split_should_skipCharsWhenLengthIsReached_1);
    RUN_TEST(test_utf8split_should_skipCharsWhenLengthIsReached_2);
    RUN_TEST(test_utf8split_should_skipLeadByteAtTheEnd);
    printf("exit: %d\n", UNITY_END());

    return 0;
}
