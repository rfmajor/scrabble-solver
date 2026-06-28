#include "gaddag.h"
#include "unity.h"
#include "utf8_splitter.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static FILE *dictionary_f;
static FILE *output_f;
static FILE *alphabet_f;
static hashmap *mapped_alphabet;

typedef union {
    char letter[4];
    uint32_t num;
} letter;

static char *LINES = "blabla\n"
                     "asdjksj\n";
static char *alphabet = "aąbcćdeę";

static hashmap *map_alphabet_letters(uint32_t *alphabet_p, size_t chars_num) {
    hashmap *hashmap = hashmap_init(512, sizeof(uint8_t));
    for (uint8_t i = 0; i < chars_num; i++) {
        uint8_t *i_p = malloc(sizeof(uint8_t));
        memcpy(i_p, &i, sizeof(uint8_t));
        if (hashmap_put(*alphabet_p++, i_p, hashmap) == NULL) {
            fprintf(stderr, "Failed to put %u\n", *(alphabet_p - 1));
        }
    }
    return hashmap;
}

static size_t read_alphabet(uint32_t **chars_p) {
    char *line = NULL;
    size_t linecap = 0;
    if (getline(&line, &linecap, alphabet_f) == 0) {
        return -1;
    }
    return utf8_split(chars_p, line, 15);
}

void setUp(void) {
    uint32_t *chars_p = NULL;
    alphabet_f = fmemopen(alphabet, strlen(alphabet), "rb");
    size_t chars_num = read_alphabet(&chars_p);
    fclose(alphabet_f);
    mapped_alphabet = map_alphabet_letters(chars_p, chars_num);
    printf("opening file for reading\n");
    dictionary_f = fmemopen(LINES, 500, "rb");
    printf("opening file for writing\n");
    output_f = fmemopen(NULL, 512, "wb");
}

void tearDown(void) {
    fclose(dictionary_f);
    fclose(output_f);
}

void test_method() {
    printf("Starting conversion\n");
    gaddag_convert(dictionary_f, output_f, mapped_alphabet, 15, 0);
    char d = fgetc(output_f);
    printf("%c\n", d);
}

int main(void) {
    UNITY_BEGIN();
    RUN_TEST(test_method);
    UNITY_END();

    return 0;
}
