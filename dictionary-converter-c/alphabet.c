#include "alphabet.h"
#include "cJSON.h"
#include <stdio.h>
#include <stdlib.h>

static int check_existing(cJSON *json, char *property, char *container_name, int required) {
    int present = 0;

    if (!cJSON_IsObject(json)) {
        fprintf(stderr, "%s is not an object\n", container_name);
        return 1;
    }
    if (cJSON_HasObjectItem(json, property)) {
        present = 1;
    }
    if (required && !present) {
        fprintf(stderr, "%s is a required property of %s\n", property, container_name);
        return 1;
    }
    return present;
}

static Letter *read_letter_info(cJSON *letter_info) {
    Letter *letter;
    if (!cJSON_HasObjectItem(letter_info, "char")) {
        fprintf(stderr, ""); return 0;
    }
    letter->letter = cJSON_GetObjectItem(letter_info, "char")->valuestring[0];
    letter->index = cJSON_GetObjectItem(letter_info, "index")->valueint;
    letter->quantity = cJSON_GetObjectItem(letter_info, "quantity")->valueint;
    letter->value = cJSON_GetObjectItem(letter_info, "value")->valueint;

    return letter;
}

AlphabetConfig *read_alphabet_config(const char *config_f) {
    FILE *fp = fopen(config_f, "rb");
    fseek(fp, 0, SEEK_END);
    long file_size = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    char *buf = malloc(file_size + 1);
    fread(buf, file_size, 1, fp);
    fclose(fp);
    buf[file_size] = 0;

    printf("[1] File output: %s\n", buf);
    cJSON *json = cJSON_Parse(buf);
    cJSON *j_delimiter = cJSON_GetObjectItem(json, "delimiter");
    cJSON *j_alphabet = cJSON_GetObjectItem(json, "alphabet");
    int j_alphabet_size = cJSON_GetArraySize(j_alphabet);
    for (int i = 0; i < j_alphabet_size; i++) {
        Letter *letter = read_letter_info(cJSON_GetArrayItem(j_alphabet, i));
        if (!letter) {
            return 0;
        }
    }

    printf("[2] Delimiter: %s\n", cJSON_Print(j_delimiter));

    return 0;
}
