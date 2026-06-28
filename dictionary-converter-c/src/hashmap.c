#include "hashmap.h"
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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

static void re_hash(hashmap *map) {
    printf("REHASH\n");
    int previous_cap = map->cap;
    map->cap *= 2;
    map->size = 0;
    int table_size = get_table_size(map->cap);
    printf("table size = %d\n", table_size);
    node *buckets_src = map->buckets;
    map->buckets = calloc(sizeof(node), table_size);
    printf("calloc\n");
    for (size_t i = 0; i < previous_cap; i++) {
        node *bucket_src = buckets_src + i;
        if (bucket_src->key != NULL) {
            printf("putting key: %u\n", *bucket_src->key);
            hashmap_put(*bucket_src->key, bucket_src->val, map);
        }
        free(bucket_src->key);
        free(bucket_src->val);
        bucket_src = bucket_src->next;
        while (bucket_src != NULL) {
            node *bucket_tmp = bucket_src;
            bucket_src = bucket_src->next;
            if (bucket_src != NULL && bucket_src->key != NULL) {
                hashmap_put(*bucket_src->key, bucket_src->val, map);
            }
            free(bucket_tmp->key);
            free(bucket_tmp->val);
            free(bucket_tmp);
        }
    }
}

static void ensure_space(hashmap *map) {
    double load = (double)map->size / map->cap;
    if (load >= map->load_factor) {
        re_hash(map);
    }
}

hashmap *hashmap_init(int cap, size_t sizeof_val) {
    hashmap *hashmap = malloc(sizeof(struct hashmap));
    if (hashmap == NULL) {
        return NULL;
    }
    int table_size = get_table_size(cap);

    hashmap->buckets = calloc(sizeof(struct node), table_size);
    if (hashmap->buckets == NULL) {
        return NULL;
    }
    hashmap->cap = table_size;
    hashmap->load_factor = 0.75;
    hashmap->size = 0;
    hashmap->sizeof_val = sizeof_val;

    return hashmap;
}

void *hashmap_put(uint32_t key, void *val, hashmap *hashmap) {
    ensure_space(hashmap);
    uint32_t hash = fnv_32_hash(&key, sizeof(key), _FNV_32bit_offset_basis);
    int bucket_num = hash % hashmap->cap;
    node *bucket = hashmap->buckets + bucket_num;

    while (bucket->key != NULL && *bucket->key != key) {
        if (bucket->next == NULL) {
            bucket->next = calloc(sizeof(struct node), 1);
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
    bucket->val = malloc(sizeof(hashmap->sizeof_val));
    *bucket->key = key;
    memcpy(bucket->val, val, hashmap->sizeof_val);

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

void hashmap_destroy(hashmap *map) {
    for (size_t i = 0; i < map->cap; i++) {
        node *bucket = map->buckets + i;
        free(bucket->key);
        free(bucket->val);
        bucket = bucket->next;
        while (bucket != NULL) {
            node *bucket_tmp = bucket;
            bucket = bucket->next;
            free(bucket_tmp->key);
            free(bucket_tmp->val);
            free(bucket_tmp);
        }
    }
    free(map->buckets);
    free(map);
}

hashmap *hashmap_of(uint32_t *keys, void *vals, size_t size, size_t val_size) {
    hashmap *map = hashmap_init(16, val_size);
    for (size_t i = 0; i < size; i++) {
        hashmap_put(*keys++, (vals + i * val_size), map);
    }
    return map;
}
