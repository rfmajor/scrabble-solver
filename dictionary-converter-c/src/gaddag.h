#include "hashmap.h"
#include <stdio.h>
#ifndef GADDAG
#define GADDAG

void gaddag_convert(FILE *dictionary_f, FILE *output_f, hashmap *mapped_alphabet, const int max_word, const int gzip);

#endif // !GADDAG
