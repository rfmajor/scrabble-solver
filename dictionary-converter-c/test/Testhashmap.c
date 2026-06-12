#include "hashmap.h"
#include "unity.h"
#include <stdio.h>
#include <stdlib.h>

hashmap_t *hashmap;

void __free_hashmap(hashmap_t *map) {
    free(map->buckets);
    free(map);
}

int __get_cap_and_destroy(int cap) {
    hashmap_t *map = hashmap_init(cap);
    int actual_cap = map->cap;
    __free_hashmap(map);
    return actual_cap;
}

void setUp(void) {
    hashmap = hashmap_init(4);
}

void tearDown(void) {
    __free_hashmap(hashmap);
}

void test_cap_should_beAlignedToNearestPowerOfTwo(void) {
    TEST_ASSERT_EQUAL(16, __get_cap_and_destroy(13));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(120));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(128));
    TEST_ASSERT_EQUAL(256, __get_cap_and_destroy(129));
}

void test_put_should_succeed(void) {
    TEST_ASSERT_EQUAL(_PUT_SUCCESS, hashmap_put(13, 124, hashmap));
    TEST_ASSERT_EQUAL(124, hashmap_get(13, hashmap));
    TEST_ASSERT_EQUAL(1, hashmap->size);
    TEST_ASSERT_EQUAL(_PUT_SUCCESS, hashmap_put(3, 79, hashmap));
    TEST_ASSERT_EQUAL(79, hashmap_get(3, hashmap));
    TEST_ASSERT_EQUAL(2, hashmap->size);
    TEST_ASSERT_EQUAL(_PUT_SUCCESS, hashmap_put(4, 0, hashmap));
    TEST_ASSERT_EQUAL(0, hashmap_get(4, hashmap));
    TEST_ASSERT_EQUAL(3, hashmap->size);
}

void test_put_should_overwrite(void) {
    TEST_ASSERT_EQUAL(_PUT_SUCCESS, hashmap_put(3, 46, hashmap));
    TEST_ASSERT_EQUAL(46, hashmap_get(3, hashmap));
    TEST_ASSERT_EQUAL(1, hashmap->size);
    TEST_ASSERT_EQUAL(_PUT_SUCCESS, hashmap_put(3, 76, hashmap));
    TEST_ASSERT_EQUAL(76, hashmap_get(3, hashmap));
    TEST_ASSERT_EQUAL(1, hashmap->size);
}

void test_put_should_fail(void) {
    // key 0 means undefined (null) key
    TEST_ASSERT_EQUAL(_PUT_FAILURE, hashmap_put(0, 24, hashmap));
}

void test_get_should_failWhenKeyAbsent(void) {
    // key 0 means undefined (null) key
    TEST_ASSERT_EQUAL(_GET_FAILURE(), hashmap_get(0, hashmap));
    TEST_ASSERT_EQUAL(_GET_FAILURE(), hashmap_get(1, hashmap));
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_cap_should_beAlignedToNearestPowerOfTwo);
    RUN_TEST(test_put_should_succeed);
    RUN_TEST(test_put_should_fail);
    RUN_TEST(test_put_should_overwrite);
    RUN_TEST(test_get_should_failWhenKeyAbsent);
    printf("exit: %d\n", UNITY_END());

    return 0;
}
