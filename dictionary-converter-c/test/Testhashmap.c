#include "hashmap.h"
#include "unity.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

hashmap *map;

int __get_cap_and_destroy(int cap) {
    hashmap *map = hashmap_init(cap, sizeof(uint16_t));
    int actual_cap = map->cap;
    hashmap_destroy(map);
    return actual_cap;
}

void setUp(void) {
    map = hashmap_init(4, sizeof(uint16_t));
}

void tearDown(void) {
    hashmap_destroy(map);
}

void test_cap_should_beAlignedToNearestPowerOfTwo(void) {
    TEST_ASSERT_EQUAL(16, __get_cap_and_destroy(13));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(120));
    TEST_ASSERT_EQUAL(128, __get_cap_and_destroy(128));
    TEST_ASSERT_EQUAL(256, __get_cap_and_destroy(129));
}

void test_put_should_succeed(void) {
    uint16_t val = 124;
    TEST_ASSERT_NOT_NULL(hashmap_put(13, &val, map));
    TEST_ASSERT_EQUAL(val, *(uint16_t *)hashmap_get(13, map));
    TEST_ASSERT_EQUAL(1, map->size);
    val = 79;
    TEST_ASSERT_NOT_NULL(hashmap_put(3, &val, map));
    TEST_ASSERT_EQUAL(val, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(2, map->size);
    val = 0;
    TEST_ASSERT_NOT_NULL(hashmap_put(4, &val, map));
    TEST_ASSERT_EQUAL(val, *(uint16_t *)hashmap_get(4, map));
    TEST_ASSERT_EQUAL(3, map->size);
}

void test_put_should_overwrite(void) {
    uint16_t val = 46;
    TEST_ASSERT_NOT_NULL(hashmap_put(3, &val, map));
    TEST_ASSERT_EQUAL(val, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(1, map->size);
    val = 76;
    TEST_ASSERT_NOT_NULL(hashmap_put(3, &val, map));
    TEST_ASSERT_EQUAL(val, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(1, map->size);
}

void test_put_should_rehashWhenLoadFactorExceeded(void) {
    hashmap *map = hashmap_init(4, sizeof(uint16_t));
    for (size_t i = 0; i < map->cap - 1; i++) {
        uint16_t val = (i + 1) * 2;
        hashmap_put(i, &val, map);
        TEST_ASSERT_EQUAL(4, map->cap);
    }
    uint16_t val = 46;
    hashmap_put(3, &val, map);
    TEST_ASSERT_EQUAL(8, map->cap);
    TEST_ASSERT_EQUAL(2, *(uint16_t *)hashmap_get(0, map));
    TEST_ASSERT_EQUAL(4, *(uint16_t *)hashmap_get(1, map));
    TEST_ASSERT_EQUAL(6, *(uint16_t *)hashmap_get(2, map));
    TEST_ASSERT_EQUAL(46, *(uint16_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(4, map->size);
}

void test_put_should_trim_value_of_larger_type(void) {
    uint16_t val1 = 0xDEAD;
    TEST_ASSERT_NOT_NULL(hashmap_put(13, &val1, map));
    TEST_ASSERT_EQUAL(val1, *(uint16_t *)hashmap_get(13, map));
    TEST_ASSERT_EQUAL(1, map->size);
    uint64_t val2 = 0xDEADBEEFDEADCAFE;
    TEST_ASSERT_NOT_NULL(hashmap_put(3, &val2, map));
    TEST_ASSERT_EQUAL(0xCAFE, *(uint64_t *)hashmap_get(3, map));
    TEST_ASSERT_EQUAL(2, map->size);
}

void test_of_should_return_hashmap_with_values(void) {
    uint32_t keys[] = {1, 2, 3};
    uint16_t vals[] = {10, 20, 30};
    hashmap *map = hashmap_of(keys, vals, 3, sizeof(uint16_t));
    TEST_ASSERT_EQUAL(10, *(uint16_t *)hashmap_get(1, map));
    TEST_ASSERT_EQUAL(20, *(uint16_t *)hashmap_get(2, map));
    TEST_ASSERT_EQUAL(30, *(uint16_t *)hashmap_get(3, map));
    free(map);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_cap_should_beAlignedToNearestPowerOfTwo);
    RUN_TEST(test_put_should_succeed);
    RUN_TEST(test_put_should_overwrite);
    RUN_TEST(test_put_should_rehashWhenLoadFactorExceeded);
    RUN_TEST(test_put_should_trim_value_of_larger_type);
    RUN_TEST(test_of_should_return_hashmap_with_values);
    UNITY_END();

    return 0;
}
