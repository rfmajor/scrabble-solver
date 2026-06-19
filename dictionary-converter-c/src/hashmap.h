#include <stdint.h>

#ifndef HASHMAP
#define HASHMAP

#define _MAX_CAP ((unsigned)-1)
#define _UNDEFINED_KEY (0)
#define _FNV_32bit_offset_basis (2166136261)
#define _FNV_32bit_prime (16777619)
#define _PUT_SUCCESS (1)
#define _PUT_FAILURE (-1)
#define _GET_FAILURE() ((uint8_t)-1)

typedef struct node {
    uint32_t key;
    uint8_t val;
    struct node *next;
} node;

typedef struct hashmap {
    int cap;
    int size;
    node *buckets;
} hashmap;

hashmap *hashmap_init(int cap);

int hashmap_put(uint32_t key, uint8_t val, hashmap *hashmap);

uint8_t hashmap_get(uint32_t key, hashmap *hashmap);

#endif // !HASHMAP
