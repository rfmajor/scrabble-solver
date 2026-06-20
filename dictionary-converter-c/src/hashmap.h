#include <stdint.h>

#ifndef HASHMAP
#define HASHMAP

#define _MAX_CAP ((unsigned)-1)
#define _FNV_32bit_offset_basis (2166136261)
#define _FNV_32bit_prime (16777619)

typedef struct node {
    uint32_t *key;
    void *val;
    struct node *next;
} node;

typedef struct hashmap {
    int cap;
    int size;
    node *buckets;
} hashmap;

hashmap *hashmap_init(int cap);

void *hashmap_put(uint32_t key, void *val, hashmap *hashmap);

void *hashmap_get(uint32_t key, hashmap *hashmap);

#endif // !HASHMAP
