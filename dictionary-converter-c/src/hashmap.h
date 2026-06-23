#include <stddef.h>
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
    size_t sizeof_val;
    int cap;
    int size;
    double load_factor;
    node *buckets;
} hashmap;

hashmap *hashmap_init(int cap, size_t sizeof_val);

void *hashmap_put(uint32_t key, void *val, hashmap *map);

void *hashmap_get(uint32_t key, hashmap *map);

void hashmap_destroy(hashmap *map);

#endif // !HASHMAP
