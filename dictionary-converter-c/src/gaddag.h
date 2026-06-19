#include "hashmap.h"
#ifndef GADDAG
#define GADDAG

void gaddag_convert(const char *dictionary_f, const char *output_f, hashmap *mapped_alphabet, const int max_word,
                    const int gzip);

#endif // !GADDAG
