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

#if defined(NO_FNV_GCC_OPTIMIZATION)
        hval *= _FNV_32bit_prime;
#else
        hval += (hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24);
#endif
        hval ^= (uint32_t)*bp++;
    }

    return hval;
}

hashmap_t *hashmap_init(int cap) {
    hashmap_t *hashmap = malloc(sizeof(struct hashmap_t));
    if (hashmap == NULL) {
        fprintf(stderr, "Failed to allocate hashmap\n");
        return NULL;
    }
    int table_size = get_table_size(cap);
    node *node_p = malloc(sizeof(struct node) * table_size);
    if (node_p == NULL) {
        fprintf(stderr, "Failed to allocate node\n");
        return NULL;
    }
    hashmap->buckets = node_p;
    hashmap->size = table_size;

    return hashmap;
}

int hashmap_add(uint32_t key, uint8_t val, hashmap_t *hashmap) {
    uint32_t hash = fnv_32_hash(&key, sizeof(key), 0);
    int bucket_num = hash % hashmap->size;
    node *bucket = hashmap->buckets + bucket_num;

    while (bucket->key != _UNDEFINED_KEY) {
        if (bucket->next == NULL) {
            bucket->next = malloc(sizeof(struct node));
            if (bucket->next == NULL) {
                fprintf(stderr, "Failed to allocate node\n");
                return 0;
            }
        }
        bucket = bucket->next;
    }
    bucket->key = key;
    bucket->val = val;
    bucket->next = NULL;

    return 1;
}

int main(int argc, char *argv[]) {
    hashmap_t *hashmap = hashmap_init(13);
    if (hashmap == NULL) {
        return EXIT_FAILURE;
    }
    hashmap_add(120, 18, hashmap);
    hashmap_add(10, 32, hashmap);
    hashmap_add(214, 76, hashmap);

    return EXIT_SUCCESS;
}
