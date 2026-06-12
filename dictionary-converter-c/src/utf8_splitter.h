#include <stdint.h>
#include <stdlib.h>

#ifndef UTF8_SPLITTER
#define UTF8_SPLITTER

#define HIGH_BIT_FLAG (0b1 << 7)
#define HIGH_2ND_BIT_F (0b1 << 6)
#define LEAD_BIT_FLAG (0b11 << 6)

#define __utf8_is_cont(c) ((c) & HIGH_BIT_FLAG) && (((c) & HIGH_2ND_BIT_F) == 0)
#define __utf8_is_ascii(c) (((c) & HIGH_BIT_FLAG) == 0)

int __utf8_is_lead(char c);

uint32_t __utf8_get_num(char *wc);

uint32_t *utf8_split(char *line, int max_bytes);

#endif // !UTF8_SPLITTER
