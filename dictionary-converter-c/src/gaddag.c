#include "gaddag.h"
#include "utf8_splitter.h"
#include <math.h>
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

struct char_bitmap_storage {
    uint32_t *idx_to_bitmap;
    hashmap_t *bitmap_to_idx;
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
    hashmap_t *mapped_alphabet;
    uint8_t delimiter_idx;
    struct char_bitmap_storage *cbs;
};

static uint64_t get_bit_value(int start, int end, uint64_t *bit_set) {
    return (*bit_set & (((1L << (end - start)) - 1) << start)) >> start;
}

static void set_bit_value(int start, int end, uint64_t value, uint64_t *target) {
    // set zeros in the range
    *target &= ~(((1L << (end - start)) - 1) << start);
    // set value in the range
    *target |= (value << start);
}

static void add_final_char(uint8_t final_char, uint64_t *arc, struct gaddag_context *ctx) {
    uint64_t char_bitmap_idx = get_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, arc);
    uint32_t char_bitmap = ctx->cbs->idx_to_bitmap[char_bitmap_idx];
    uint32_t new_char_bitmap = add_to_bitmap(char_bitmap, final_char);
    uint8_t new_char_bitmap_idx;
    if ((new_char_bitmap_idx = hashmap_get(new_char_bitmap, ctx->cbs->bitmap_to_idx)) != ((uint8_t)-1)) {
        char_bitmap_idx = new_char_bitmap_idx;
    } else {
        char_bitmap_idx = ctx->next_char_bitmap_idx;
        ++ctx->next_char_bitmap_idx;
        // todo: realloc if idx_to_bitmap runs out of space
        ctx->cbs->idx_to_bitmap[char_bitmap_idx] = new_char_bitmap;
    }
    set_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, char_bitmap_idx, arc);
}

static int realloc_states(struct gaddag_context *ctx) {
    ctx->states_cap *= 2;
    printf("Reallocating states to %d\n", ctx->states_cap);
    ctx->arcs = realloc(ctx->arcs, ctx->states_cap);
    if (ctx->arcs == NULL) {
        fprintf(stderr, "Could not reallocate %d states to %d\n", ctx->states_cap / 2, ctx->states_cap);
        return 0;
    }
    return 1;
}

// add final arc
static void force_arc(uint8_t arc_c, struct gaddag_context *ctx) {
}

// add final arc if one does not exist yet
static void ensure_final_arc(uint8_t arc_char, uint8_t final_char, struct gaddag_context *ctx) {
    uint64_t arc;
    if ((arc = ARCS(arc_char, ctx)) == 0) {
        if (ctx->next_state_idx >= ctx->states_cap) {
            if (!realloc_states(ctx)) {
                return;
            }
        }
        set_bit_value(DEST_ID_START, DEST_ID_END, ctx->next_state_idx, &arc);
        ++ctx->next_state_idx;
    }
    add_final_char(final_char, &arc, ctx);
    ctx->last_char_bitmap_idx = get_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, &arc);
    ctx->current_state_idx = get_bit_value(DEST_ID_START, DEST_ID_END, &arc);
}

// add arc if one does not exist yet
static void ensure_arc(uint8_t arc_char, struct gaddag_context *ctx) {
    uint64_t arc;
    if ((arc = ARCS(arc_char, ctx)) == 0) {
        if (ctx->next_state_idx >= ctx->states_cap) {
            if (!realloc_states(ctx)) {
                return;
            }
        }
        set_bit_value(DEST_ID_START, DEST_ID_END, ctx->next_state_idx, &arc);
        ++ctx->next_state_idx;
    }
    ctx->last_char_bitmap_idx = get_bit_value(CHAR_BITMAP_ID_START, CHAR_BITMAP_ID_END, &arc);
    ctx->current_state_idx = get_bit_value(DEST_ID_START, DEST_ID_END, &arc);
}

static size_t translate(uint8_t **tokens, char *word, hashmap_t *alphabet) {
    uint32_t *split = NULL;
    size_t len = utf8_split(&split, word, MAX_CHARS);
    *tokens = malloc(sizeof(uint8_t) * len + 1);
    for (size_t i = 0; i < len; i++) {
        *tokens[i] = hashmap_get(split[i], alphabet);
    }
    free(split);
    return len;
}

static uint32_t *process_word(char *word, struct gaddag_context *ctx) {
    uint8_t *t_word = NULL;
    size_t len = translate(&t_word, word, ctx->mapped_alphabet);

    ctx->current_state_idx = 1;
    for (size_t i = len - 1; i >= 2; i--) {
        ensure_arc(t_word[i], ctx);
    }
    ensure_final_arc(t_word[1], t_word[0], ctx);

    for (size_t i = len - 2; i >= 0; i--) {
        ensure_arc(t_word[i], ctx);
    }
    ensure_final_arc(ctx->delimiter_idx, t_word[0], ctx);

    for (size_t m = len - 3; m >= 0; m--) {
        ctx->force_state_idx = ctx->current_state_idx;
        ctx->force_char_bitmap_idx = ctx->last_char_bitmap_idx;
        ctx->current_state_idx = 1;
        ctx->last_char_bitmap_idx = 0;
        for (size_t i = m; i >= 0; i--) {
            ensure_arc(t_word[i], ctx);
        }
        ensure_arc(ctx->delimiter_idx, ctx);
        force_arc(t_word[m + 1], ctx);
    }
    int max_bitmap_id = (int)pow(2, CHAR_BITMAP_ID_END - CHAR_BITMAP_ID_START - 1);
}

void mem_init(uint32_t initial_states, uint32_t alphabet_size, hashmap_t *mapped_alphabet, struct gaddag_context *ctx) {
    ctx = malloc(sizeof(struct gaddag_context));
    ctx->next_state_idx = 2;
    ctx->next_char_bitmap_idx = 1;
    ctx->delimiter_idx = mapped_alphabet->size - 1;
    ctx->states_cap = initial_states;
    ctx->cbs = malloc(sizeof(struct char_bitmap_storage));
    ctx->cbs->bitmap_to_idx = malloc(sizeof(hashmap_t));
    ctx->cbs->idx_to_bitmap = malloc(sizeof(uint32_t) * 512);
    ctx->arcs = malloc(sizeof(uint64_t) * initial_states * alphabet_size);
}

void gaddag_convert(const char *dictionary_f, const char *output_f, hashmap_t *mapped_alphabet, const int max_word,
                    const int gzip) {
    FILE *fp = fopen(dictionary_f, "rb");
    char *line = NULL;
    size_t linecap = 0;
    ssize_t linelen = 0;

    struct gaddag_context *ctx = NULL;
    mem_init(512, 33, mapped_alphabet, ctx);

    while ((linelen = getline(&line, &linecap, fp)) > 0) {
        process_word(line, ctx);
    }

    fclose(fp);
}
