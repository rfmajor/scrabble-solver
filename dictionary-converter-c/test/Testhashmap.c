#include "hashmap.h"
#include "unity.h"
#include <stdio.h>
#include <stdlib.h>

hashmap *map;
uint16_t *val;

void __free_hashmap(hashmap *map) {
    free(map->buckets);
    free(map);
}

int __get_cap_and_destroy(int cap) {
    hashmap *map = hashmap_init(cap);
    int actual_cap = map->cap;
    __free_hashmap(map);
    return actual_cap;
}

void setUp(void) {
    map = hashmap_init(4);
    val = malloc(sizeof(uint16_t));
}

void tearDown(void) {
    __free_hashmap(map);
    free(val);
}

void test_cap_should_beAlignedToNearestPowerOfTwo(void) {
    TEST_ASSERT_EQUAL(16, __get_cap_and_destroy(13));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(120));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(128));
    TEST_ASSERT_EQUAL(256, __get_cap_and_destroy(129));
}

void test_put_should_succeed(void) {
    *val = 124;
    TEST_ASSERT_EQUAL(val, hashmap_put(13, val, map));
    TEST_ASSERT_EQUAL(124, *(uint16_t *)hashmap_get(13, map));
    TEST_ASSERT_EQUAL(1, map->size);
    *val = 79;
    TEST_ASSERT_EQUAL(val, hashmap_put(3, val, map));
    TEST_ASSERT_EQUAL(79, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(2, map->size);
    *val = 0;
    TEST_ASSERT_EQUAL(val, hashmap_put(4, val, map));
    TEST_ASSERT_EQUAL(0, *(uint16_t *)hashmap_get(4, map));
    TEST_ASSERT_EQUAL(3, map->size);
}

void test_put_should_overwrite(void) {
    *val = 46;
    TEST_ASSERT_EQUAL(val, hashmap_put(3, val, map));
    TEST_ASSERT_EQUAL(46, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(1, map->size);
    *val = 76;
    TEST_ASSERT_EQUAL(val, hashmap_put(3, val, map));
    TEST_ASSERT_EQUAL(76, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(1, map->size);
}

void test_put_should_fail(void) {
    // key 0 means undefined (null) key
    // TEST_ASSERT_EQUAL(_PUT_FAILURE, hashmap_put(0, 24, map));
}

void test_get_should_failWhenKeyAbsent(void) {
    // key 0 means undefined (null) key
    // TEST_ASSERT_EQUAL(_GET_FAILURE(), hashmap_get(0, map));
    // TEST_ASSERT_EQUAL(_GET_FAILURE(), hashmap_get(1, map));
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_cap_should_beAlignedToNearestPowerOfTwo);
    RUN_TEST(test_put_should_succeed);
    // RUN_TEST(test_put_should_fail);
    RUN_TEST(test_put_should_overwrite);
    // RUN_TEST(test_get_should_failWhenKeyAbsent);
    UNITY_END();

    return 0;
}
