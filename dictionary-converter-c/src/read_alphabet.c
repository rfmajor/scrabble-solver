#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>

#define HIGH_BIT_FLAG (0b1 << 7)
#define HIGH_2ND_BIT_F (0b1 << 6)
#define LEAD_BIT_FLAG (0b11 << 6)

#define is_cont(c) ((c) & HIGH_BIT_FLAG) && (((c) & HIGH_2ND_BIT_F) == 0)
#define is_ascii(c) (((c) & HIGH_BIT_FLAG) == 0)

int is_lead(char c) {
    if (is_ascii(c)) {
        return 0;
    }
    int count = 0;
    while (HIGH_BIT_FLAG & (c <<= 1))
        count++;
    return count;
}

uint32_t get_num(char *wc) {
    uint32_t result = 0;
    while (*wc) {
        result <<= sizeof(char) * 8;
        result |= ((unsigned char)*wc++);
    }
    return result;
}

char *get_bits(uint32_t num) {
    // 8 chars per bit in int + (num of int bytes - 1) spaces
    size_t size = 8 * sizeof(char) * sizeof(uint32_t) + (sizeof(uint32_t) - 1);
    char *bits_s = (char *)malloc(size + 1);
    char *bits_s_start = bits_s;
    bits_s[size] = 0;
    // point the pointer to the last place
    bits_s += size;
    int i = 0;
    while (bits_s-- != bits_s_start) {
        if (num % 2 == 1) {
            *bits_s = '1';
        } else {
            *bits_s = '0';
        }
        num >>= 1;
        // add space between bytes
        if (++i % 8 == 0 && bits_s - 1 >= bits_s_start) {
            *--bits_s = ' ';
        }
    }
    return ++bits_s;
}

int main(int argc, char *argv[]) {
    FILE *fp = fopen("testfile.txt", "rb");
    int c;
    char wc[4];
    int wc_i = 0;
    int last_lead = 0;
    int type;
    while ((c = getc(fp)) != EOF) {
        if (!(type = is_lead(c))) {
            if (is_cont(c)) {
                wc[wc_i++] = c;
                if (wc_i > last_lead) {
                    wc[wc_i] = 0;
                    wc_i = 0;
                    type = 0;
                    // printf("Symbol: %s, number: %u\n", wc, get_num(wc));
                    uint32_t num = get_num(wc);
                    char *bits = get_bits(num);
                    printf("Symbol: %s, number: %u, bits: %s\n", wc, num, bits);
                }
            } else {
                char *bits = get_bits(c);
                printf("Symbol: %c, number: %u, bits: %s\n", c, c, bits);
                // printf("Symbol: %c, number: %u\n", c, c);
            }
        } else {
            last_lead = type;
            wc[wc_i++] = c;
        }
    }
    fclose(fp);
    return EXIT_SUCCESS;
}
