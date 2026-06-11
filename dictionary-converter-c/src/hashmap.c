#include "hashmap.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

static int num_of_leading_zeros(int num) { return __builtin_clz(num); }

static int get_table_size(int cap) {
    unsigned int n = ((unsigned)-1) >> num_of_leading_zeros(cap - 1);
    return n >= _MAX_CAP ? _MAX_CAP : n + 1;
}

static uint32_t fnv_32_hash(void *buf, size_t len, uint32_t hval) {
    unsigned char *bp = (unsigned char *)buf;
    unsigned char *be = bp + len;

    while (bp < be) {
        hval *= _FNV_32bit_prime;
        hval ^= (uint32_t)*bp++;
    }

    return hval;
}

hashmap_t *hashmap_init(int cap) {
    hashmap_t *hashmap = malloc(sizeof(struct hashmap_t));
    if (hashmap == NULL) {
        return NULL;
    }
    int table_size = get_table_size(cap);
    node *node_p = malloc(sizeof(struct node) * table_size);
    if (node_p == NULL) {
        return NULL;
    }
    hashmap->buckets = node_p;
    hashmap->cap = table_size;
    hashmap->size = 0;

    return hashmap;
}

int hashmap_put(uint32_t key, uint8_t val, hashmap_t *hashmap) {
    uint32_t hash = fnv_32_hash(&key, sizeof(key), _FNV_32bit_offset_basis);
    printf("[PUT] hash of %u: %u\n", key, hash);
    int bucket_num = hash % hashmap->cap;
    printf("[PUT] bucket num for %u: %d\n", key, bucket_num);
    node *bucket = hashmap->buckets + bucket_num;
    printf("[PUT] bucket for %u: %p\n", key, bucket);

    while (bucket->key != _UNDEFINED_KEY && bucket->key != key) {
        printf("[PUT] node is occupied\n");
        if (bucket->next == NULL) {
            bucket->next = malloc(sizeof(struct node));
            if (bucket->next == NULL) {
                return 0;
            }
        }
        bucket = bucket->next;
    }
    bucket->key = key;
    bucket->val = val;
    hashmap->size++;

    return 1;
}

uint8_t hashmap_get(uint32_t key, hashmap_t *hashmap) {
    uint32_t hash = fnv_32_hash(&key, sizeof(key), _FNV_32bit_offset_basis);
    printf("[GET] hash of %u: %u\n", key, hash);
    int bucket_num = hash % hashmap->cap;
    printf("[GET] bucket num for %u: %d\n", key, bucket_num);
    node *bucket = hashmap->buckets + bucket_num;
    printf("[GET] bucket for %u: %p\n", key, bucket);

    printf("[GET] bucket for %u: {key: %u, val: %u, next: %p}\n", key, bucket->key, bucket->val, bucket->next);

    while (bucket->key != key && bucket->key != _UNDEFINED_KEY && bucket->next != NULL) {
        bucket = bucket->next;
        printf("[GET] bucket occupied, next bucket for %u: {key: %u, val: %u, next: %p}\n", key, bucket->key,
               bucket->val, bucket->next);
    }
    if (bucket->key == _UNDEFINED_KEY || bucket->key != key) {
        return 0;
    }
    return bucket->val;
}

int main(int argc, char *argv[]) {
    int cap = 100;
    hashmap_t *hashmap = hashmap_init(cap);
    if (hashmap == NULL) {
        fprintf(stderr, "Failed to allocate hashmap\n");
        return EXIT_FAILURE;
    }

    uint32_t record[1000];

    for (int i = 1; i < cap * 10; i++) {
        uint32_t key = rand() % 1000;
        uint8_t val = rand() % 100;
        record[key] = val;
        hashmap_put(key, val, hashmap);
    }

    int err_count = 0, success_count = 0;
    for (int i = 1; i < cap * 10; i++) {
        uint8_t val = hashmap_get(i, hashmap);
        if (record[i] == val) {
            success_count++;
        } else {
            err_count++;
        }
    }

    printf("Success count: %d\n", success_count);
    printf("Err count: %d\n", err_count);

    return EXIT_SUCCESS;
}
