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
        printf("loop\n");
        result <<= sizeof(char) * 8;
        result |= ((unsigned char)*wc++);
    }
    return result;
}

static uint32_t get_num_and_reset_widechar_context(char *wc, int *wc_i, int *type) {
    wc[*wc_i] = 0;
    *wc_i = 0;
    *type = 0;
    return __utf8_get_num(wc);
}

uint32_t *utf8_split(char *line, int max_chars) {
    char c;
    char wc[4];
    int type, wc_i = 0, last_lead = 0, i = 0;
    uint32_t *split = malloc(sizeof(uint32_t) * max_chars);
    if (split == NULL) {
        return NULL;
    }
    uint32_t *split_i = split;
    while ((c = *line++) != '\n' && i < max_chars) {
        if (!(type = __utf8_is_lead(c))) {
            if (__utf8_is_cont(c)) {
                wc[wc_i++] = c;
                if (wc_i > last_lead) {
                    uint32_t num = get_num_and_reset_widechar_context(wc, &wc_i, &type);
                    // wc[wc_i] = 0;
                    // wc_i = 0;
                    // type = 0;
                    // uint32_t num = __utf8_get_num(wc);
                    *split_i++ = num;
                    i++;
                }
            } else {
                *split_i++ = c;
                i++;
            }
        } else {
            if (wc_i > 0) {
                // last sequence has not been processed fully so the char is malformed -> just keep it as is
                wc[wc_i++] = c;
                // wc[wc_i] = 0;
                // wc_i = 0;
                // type = 0;
                // uint32_t num = __utf8_get_num(wc);
                uint32_t num = get_num_and_reset_widechar_context(wc, &wc_i, &type);
                *split_i++ = num;
                i++;
            }
            last_lead = type;
            wc[wc_i++] = c;
        }
    }
    return split;
}
