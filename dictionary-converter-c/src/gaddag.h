#include "hashmap.h"
#ifndef GADDAG
#define GADDAG

void gaddag_convert(const char *dictionary_f, const char *output_f, hashmap_t *mapped_alphabet, const int max_word,
                    const int gzip);

#endif // !GADDAG
