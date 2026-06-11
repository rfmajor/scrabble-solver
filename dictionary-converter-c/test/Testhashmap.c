#include "hashmap.h"
#include "unity.h"

static hashmap_t *hashmap;

void setUp() { hashmap = hashmap_init(120); }

void tearDown(void) {}

void test_put(void) {
    int result = hashmap_put(13, 124, hashmap);
    TEST_ASSERT_EQUAL(1, output);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_put);
    UNITY_END();

    return 0;
}
