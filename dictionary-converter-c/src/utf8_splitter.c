#include "utf8_splitter.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

int __utf8_is_lead(char c) {
    if (__utf8_is_ascii(c)) {
        return 0;
    }
    int count = 0;
    while (HIGH_BIT_FLAG & (c <<= 1))
        count++;
    return count;
}

uint32_t __utf8_get_num(char *wc) {
    uint32_t result = 0;
    while (*wc) {
        result <<= sizeof(char) * 8;
        result |= ((unsigned char)*wc++);
    }
    return result;
}

static uint32_t __dump_wc_context(struct wc_context *ctx) {
    uint32_t num = __utf8_get_num(ctx->wc);
    ctx->i = 0;
    ctx->end_i = 0;
    for (size_t i = 0; i < WC_CONTEXT_SIZE; i++) {
        ctx->wc[i] = 0;
    }

    return num;
}

uint32_t *utf8_split(char *line, int max_chars) {
    char c;
    int cont_bytes_num;
    int i = 0;
    struct wc_context wc_ctx = {};
    uint32_t *split = malloc(sizeof(uint32_t) * max_chars);
    if (split == NULL) {
        return NULL;
    }
    uint32_t *result = split;
    while ((c = *line++) != '\n' && i < max_chars) {
        int is_context_nonempty = wc_ctx.i > 0;
        if ((__utf8_is_ascii(c) || __utf8_is_lead(c)) && is_context_nonempty) {
            // lead/ascii byte received and previous context is not empty -> dump context and retry
            *result++ = __dump_wc_context(&wc_ctx);
            --line;
            ++i;
            continue;
        }
        if (__utf8_is_ascii(c)) {
            *result++ = c;
            ++i;
            continue;
        }
        if ((cont_bytes_num = __utf8_is_lead(c))) {
            wc_ctx.end_i = cont_bytes_num;
            wc_ctx.wc[wc_ctx.i++] = c;
            continue;
        }
        if (__utf8_is_cont(c)) {
            if (is_context_nonempty) {
                wc_ctx.wc[wc_ctx.i++] = c;
                if (wc_ctx.i >= wc_ctx.end_i) {
                    *result++ = __dump_wc_context(&wc_ctx);
                    ++i;
                }
            } else {
                // continuation byte without leading byte -> dump char as is
                *result++ = (unsigned char)c;
                ++i;
            }
        }
    }
    if (wc_ctx.i > 0 && i < max_chars) {
        // context is not empty after finishing -> dump context
        *result++ = __dump_wc_context(&wc_ctx);
    }
    return split;
}
