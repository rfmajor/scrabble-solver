#include "hashmap.h"
#include "unity.h"
#include <stdio.h>
#include <stdlib.h>

hashmap *map;
uint16_t *val;

int __get_cap_and_destroy(int cap) {
    hashmap *map = hashmap_init(cap, sizeof(uint16_t));
    int actual_cap = map->cap;
    hashmap_destroy(map);
    return actual_cap;
}

void setUp(void) {
    map = hashmap_init(4, sizeof(uint16_t));
    val = malloc(sizeof(uint16_t));
}

void tearDown(void) {
    hashmap_destroy(map);
    free(val);
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

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_cap_should_beAlignedToNearestPowerOfTwo);
    RUN_TEST(test_put_should_succeed);
    RUN_TEST(test_put_should_overwrite);
    RUN_TEST(test_put_should_rehashWhenLoadFactorExceeded);
    RUN_TEST(test_put_should_trim_value_of_larger_type);
    UNITY_END();

    return 0;
}
