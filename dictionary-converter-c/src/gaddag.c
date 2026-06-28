#include "gaddag.h"
#include "utf8_splitter.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_CHARS (15)
#define DEST_ID_START (0)
#define DEST_ID_END (32)
#define CHAR_BITMAP_ID_START (32)
#define CHAR_BITMAP_ID_END (64)
#define ARCS(A, CTX) (CTX->arcs[(CTX->current_state_idx) * (CTX->states_cap) + (A)])
// #define SET_ZEROS(START, END, TARGET) (&(TARGET) = )

typedef struct {
    char *RED;
    char *GREEN;
    char *ENDC;
} colors;

const colors COLORS = {.RED = "\033[1;91m", .GREEN = "\033[1;92m", .ENDC = "\033[0;22m"};

struct chars_storage {
    uint32_t *idx_to_bitmap;
    hashmap *bitmap_to_idx;
};

struct gaddag_context {
    uint64_t *arcs;
    uint32_t next_state_idx;
    uint32_t current_state_idx;
    uint32_t force_state_idx;
    uint32_t next_char_bitmap_idx;
    uint32_t last_char_bitmap_idx;
    uint32_t force_char_bitmap_idx;
    uint32_t states_cap;
    uint32_t alphabet_size;
    uint8_t delimiter_idx;
    hashmap *mapped_alphabet;
    struct chars_storage *chars;
};

static uint64_t get_bit_value(int start, int end, uint64_t *bit_set) {
    return (*bit_set & (((1L << (end - start)) - 1) << start)) >> start;
}

static uint64_t get_dest_state(uint64_t *target) {
    return get_bit_value(DEST_ID_START, DEST_ID_END, target);
}

static uint64_t get_char_bitmap(uint64_t *target) {
    return get_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, target);
}

static void set_bit_value(int start, int end, uint64_t value, uint64_t *target) {
    // set zeros in the range
    *target &= ~(((1L << (end - start)) - 1) << start);
    // set value in the range
    *target |= (value << start);
}

static void set_dest_state(uint64_t value, uint64_t *target) {
    set_bit_value(DEST_ID_START, DEST_ID_END, value, target);
}

static void set_char_bitmap(uint64_t value, uint64_t *target) {
    set_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, value, target);
}

static uint32_t add_to_bitmap(uint32_t bitmap, uint8_t index) {
    return bitmap | (1 << index);
}

static void add_final_char(uint8_t final_char, uint64_t *arc, struct gaddag_context *ctx) {
    printf("[add_final_char] \n");
    uint64_t char_bitmap_idx = get_char_bitmap(arc);
    uint32_t char_bitmap = ctx->chars->idx_to_bitmap[char_bitmap_idx];
    uint32_t new_char_bitmap = add_to_bitmap(char_bitmap, final_char);
    uint8_t *new_char_bitmap_idx;
    if ((new_char_bitmap_idx = (uint8_t *)hashmap_get(new_char_bitmap, ctx->chars->bitmap_to_idx)) != NULL) {
        char_bitmap_idx = *new_char_bitmap_idx;
    } else {
        char_bitmap_idx = ctx->next_char_bitmap_idx;
        ++ctx->next_char_bitmap_idx;
        // todo: realloc if idx_to_bitmap runs out of space
        ctx->chars->idx_to_bitmap[char_bitmap_idx] = new_char_bitmap;
    }
    set_char_bitmap(char_bitmap_idx, arc);
}

static int realloc_states(struct gaddag_context *ctx) {
    printf("[realloc_states] \n");
    uint32_t old_states_cap = ctx->states_cap;
    ctx->states_cap *= 2;
    uint32_t previous_size = sizeof(uint64_t) * old_states_cap * ctx->alphabet_size;
    printf("Reallocating states to %d\n", ctx->states_cap);
    printf("[before realloc] \n");
    uint64_t *arcs = realloc(ctx->arcs, sizeof(uint64_t) * ctx->states_cap * ctx->alphabet_size);
    printf("[after realloc] \n");
    ctx->arcs = arcs;
    memset(ctx->arcs + previous_size, 0, previous_size);
    printf("[after memset] \n");
    if (ctx->arcs == NULL) {
        fprintf(stderr, "Could not reallocate %d states to %d\n", ctx->states_cap / 2, ctx->states_cap);
        return 0;
    }
    return 1;
}

// add final arc
static void force_arc(uint8_t arc_char, struct gaddag_context *ctx) {
    printf("%s[force_arc] arc char: %u%s\n", COLORS.GREEN, arc_char, COLORS.ENDC);
    uint64_t arc = ARCS(arc_char, ctx);
    int curr_dest_state_idx = get_dest_state(&arc);
    if (curr_dest_state_idx == 0) {
        set_dest_state(ctx->force_state_idx, &arc);
    }
    set_char_bitmap(ctx->force_char_bitmap_idx, &arc);
}

// add final arc if one does not exist yet
static void ensure_final_arc(uint8_t arc_char, uint8_t final_char, struct gaddag_context *ctx) {
    printf("%s[ensure_final_arc] arc char: %u, final char: %u%s\n", COLORS.GREEN, arc_char, final_char, COLORS.ENDC);
    uint64_t arc;
    if ((arc = ARCS(arc_char, ctx)) == 0) {
        if (ctx->next_state_idx >= ctx->states_cap) {
            if (!realloc_states(ctx)) {
                return;
            }
        }
        printf("[ensure_final_arc] creating arc\n");
        set_dest_state(ctx->next_state_idx, &arc);
        printf("[ensure_final_arc] dest state: %llu\n", get_dest_state(&arc));
        ++ctx->next_state_idx;
    }
    printf("[ensure_final_arc] adding final char: %u\n", final_char);
    add_final_char(final_char, &arc, ctx);
    ctx->last_char_bitmap_idx = get_char_bitmap(&arc);
    ctx->current_state_idx = get_dest_state(&arc);
}

// add arc if one does not exist yet
static void ensure_arc(uint8_t arc_char, struct gaddag_context *ctx) {
    printf("%s[ensure_arc] arc char: %u%s\n", COLORS.GREEN, arc_char, COLORS.ENDC);
    uint64_t arc;
    if ((arc = ARCS(arc_char, ctx)) == 0) {
        if (ctx->next_state_idx >= ctx->states_cap) {
            if (!realloc_states(ctx)) {
                return;
            }
        }
        printf("[ensure_arc] creating arc\n");
        set_dest_state(ctx->next_state_idx, &arc);
        printf("[ensure_arc] dest state: %llu\n", get_dest_state(&arc));
        ++ctx->next_state_idx;
    }
    ctx->last_char_bitmap_idx = get_char_bitmap(&arc);
    ctx->current_state_idx = get_dest_state(&arc);
}

static size_t translate(uint8_t **tokens, char *word, struct gaddag_context *ctx) {
    printf("[translate] word: %s\n", word);
    uint32_t *split = NULL;
    size_t len = utf8_split(&split, word, MAX_CHARS);
    *tokens = calloc(sizeof(uint8_t), len + 1);
    printf("%s[translate] translated: ", COLORS.RED);
    for (size_t i = 0; i < len; i++) {
        (*tokens)[i] = *(uint8_t *)hashmap_get(split[i], ctx->mapped_alphabet);
        printf("%u ", (*tokens)[i]);
    }
    printf("\n%s", COLORS.ENDC);
    free(split);
    return len;
}

static void process_word(char *word, struct gaddag_context *ctx) {
    uint8_t *t_word = NULL;
    size_t len = translate(&t_word, word, ctx);

    printf("[process_word] first step\n");
    ctx->current_state_idx = 1;
    for (int i = len - 1; i >= 2; i--) {
        ensure_arc(t_word[i], ctx);
    }
    ensure_final_arc(t_word[1], t_word[0], ctx);

    printf("[process_word] second step\n");
    ctx->current_state_idx = 1;
    for (int i = len - 2; i >= 0; i--) {
        ensure_arc(t_word[i], ctx);
    }
    ensure_final_arc(ctx->delimiter_idx, t_word[0], ctx);

    printf("[process_word] third step\n");
    for (int m = len - 3; m >= 0; m--) {
        ctx->force_state_idx = ctx->current_state_idx;
        ctx->force_char_bitmap_idx = ctx->last_char_bitmap_idx;
        ctx->current_state_idx = 1;
        ctx->last_char_bitmap_idx = 0;
        printf("[process_word] third step - iteration %d\n", m);
        for (int i = m; i >= 0; i--) {
            ensure_arc(t_word[i], ctx);
        }
        ensure_arc(ctx->delimiter_idx, ctx);
        force_arc(t_word[m + 1], ctx);
    }
}

struct gaddag_context *mem_init(uint32_t initial_states, uint32_t alphabet_size, hashmap *mapped_alphabet) {
    struct gaddag_context *ctx = calloc(sizeof(struct gaddag_context), 1);
    ctx->next_state_idx = 2;
    ctx->next_char_bitmap_idx = 1;
    ctx->delimiter_idx = mapped_alphabet->size;
    ctx->states_cap = initial_states;
    ctx->chars = calloc(sizeof(struct chars_storage), 1);
    ctx->chars->bitmap_to_idx = hashmap_init(512, sizeof(uint8_t));
    ctx->chars->idx_to_bitmap = calloc(sizeof(uint32_t), 512);
    ctx->arcs = calloc(sizeof(uint64_t), initial_states * alphabet_size);
    ctx->mapped_alphabet = mapped_alphabet;
    ctx->alphabet_size = alphabet_size;
    printf("Malloced arcs pointer: %p\n", ctx->arcs);
    printf("Malloced alphabet pointer: %p\n", ctx->mapped_alphabet);
    return ctx;
}

void gaddag_convert(const char *dictionary_f, const char *output_f, hashmap *mapped_alphabet, const int max_word,
                    const int gzip) {
    FILE *fp = fopen(dictionary_f, "rb");
    char *line = NULL;
    size_t linecap = 0;
    ssize_t linelen = 0;

    struct gaddag_context *ctx = mem_init(512, 33, mapped_alphabet);

    while ((linelen = getline(&line, &linecap, fp)) > 0) {
        process_word(line, ctx);
    }
    uint64_t root_arc = 0;
    // any value for char bitmap for the first arc
    set_char_bitmap(123, &root_arc);
    set_dest_state(1, &root_arc);

    fclose(fp);

    // FILE *dest_fp = fopen(output_f, "wb");
    // if (dest_fp == NULL) {
    //     fprintf(stderr, "Failed to open file for writing\n");
    //     return;
    // }
    // fclose(dest_fp);
}
