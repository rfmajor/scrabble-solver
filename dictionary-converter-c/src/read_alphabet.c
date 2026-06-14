#include "utf8_splitter.h"
#include <stdint.h>
#include <stdio.h>

int main(int argc, char *argv[]) {
    FILE *fp = fopen("testfile.txt", "rb");

    printf("Calling getline\n");
    char *line = NULL;
    size_t linecap = 0;
    ssize_t linelen;
    uint32_t *split;
    while ((linelen = getline(&line, &linecap, fp)) > 0) {
        printf("Getline completed with %zd length, line: (%s)\n", linelen, line);
        split = utf8_split(line, 40);
    }
    printf("FINISHED: getline\n");
    int i = 0;
    while (split) {
        printf("[%d] %u\n", i, *split++);
    }
}
