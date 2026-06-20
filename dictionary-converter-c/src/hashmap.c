#include "hashmap.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

static int num_of_leading_zeros(int num) {
    return __builtin_clz(num);
}

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

hashmap *hashmap_init(int cap) {
    hashmap *hashmap = malloc(sizeof(struct hashmap));
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

void *hashmap_put(uint32_t key, void *val, hashmap *hashmap) {
    uint32_t hash = fnv_32_hash(&key, sizeof(key), _FNV_32bit_offset_basis);
    int bucket_num = hash % hashmap->cap;
    node *bucket = hashmap->buckets + bucket_num;

    while (bucket->key != NULL && *bucket->key != key) {
        if (bucket->next == NULL) {
            bucket->next = malloc(sizeof(struct node));
            if (bucket->next == NULL) {
                return NULL;
            }
        }
        bucket = bucket->next;
    }
    if (bucket->key == NULL || *bucket->key != key) {
        hashmap->size++;
    }
    bucket->key = malloc(sizeof(uint32_t));
    *bucket->key = key;
    bucket->val = val;

    return bucket->val;
}

void *hashmap_get(uint32_t key, hashmap *hashmap) {
    uint32_t hash = fnv_32_hash(&key, sizeof(key), _FNV_32bit_offset_basis);
    int bucket_num = hash % hashmap->cap;
    node *bucket = hashmap->buckets + bucket_num;

    while (bucket->key != NULL && *bucket->key != key && bucket->next != NULL) {
        bucket = bucket->next;
    }
    if (bucket->key == NULL || *bucket->key != key) {
        return NULL;
    }
    return bucket->val;
}
