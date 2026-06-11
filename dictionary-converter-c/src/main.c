#include "alphabet.h"
#include <argp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static char *SUPPORTED_DATA_STRUCTURES[] = {"gaddag", 0};

static char *HELP_JSON = "{\n"
                         "  \"delimiter\": {\n"
                         "    \"char\": \"-\",\n"
                         "    \"index\": 0\n"
                         "  },\n"
                         "  \"alphabet\": [\n"
                         "    { \"char\": \"A\", \"index\": 1, \"quantity\": 9, \"value\": 1 },\n"
                         "    { \"char\": \"B\", \"index\": 2, \"quantity\": 2, \"value\": 3 },\n"
                         "    { \"char\": \"C\", \"index\": 3, \"quantity\": 2, \"value\": 3 }\n"
                         "  ]\n"
                         "}\n";

static char doc[] = "A program for converting a dictionary of words into a traversable data structure used for word "
                    "lookup in the scrabble game.";
static char args_doc[] = "DICTIONARY_FILE OUTPUT_FILE";
static struct argp_option options[] = {
    {"data-structure", 'd', "DATA_STRUCTURE", 0, "Data structure to use (default: gaddag)"},
    {"gzip", 'g', 0, 0, "Enable gzip compression (default: false)"},
    {"alphabet-config", 'c', "CONFIG_FILE", 0, "Path to alphabet config .json file (default: ./alphabet.json)"},
    {"max-word-length", 'm', "MAX_LENGTH", 0,
     "Maximum word length, words with longer length are skipped (default: none)"},
    {0}};

struct arguments {
    char *args[2];
    char *data_structure;
    char *alphabet_config;
    int max_word_length;
    int gzip;
};

static error_t parse_opt(int key, char *arg, struct argp_state *state) {
    struct arguments *arguments = state->input;

    switch (key) {
    case 'd':
        arguments->data_structure = arg;
        break;
    case 'g':
        arguments->gzip = 1;
        break;
    case 'c':
        arguments->alphabet_config = arg;
        break;
    case 'm':
        sscanf(arg, "%d", &(arguments->max_word_length));
        break;

    case ARGP_KEY_ARG:
        if (state->arg_num > 1) {
            fprintf(stderr, "Too many arguments provided, expected: 2\n");
            argp_usage(state);
        }
        arguments->args[state->arg_num] = arg;
        break;

    case ARGP_KEY_END:
        if (state->arg_num < 2) {
            fprintf(stderr, "Too few arguments provided, expected: 2\n");
            argp_usage(state);
        }
        break;

    default:
        return ARGP_ERR_UNKNOWN;
    }

    return 0;
}

static struct argp argp = {options, parse_opt, args_doc, doc};

int validate_args(struct arguments *arguments) {
    size_t i = 0;
    char *data_struct;
    int data_struct_valid = 0;

    while ((data_struct = SUPPORTED_DATA_STRUCTURES[i++])) {
        if (!strcmp(arguments->data_structure, data_struct)) {
            data_struct_valid = 1;
        }
    }
    if (!data_struct_valid) {
        fprintf(stderr, "Data structure not supported: %s\n", arguments->data_structure);
        return 1;
    }

    if (access(arguments->alphabet_config, F_OK) != 0) {
        fprintf(stderr, "%s: file not found\n", arguments->alphabet_config);
        return 1;
    }

    if (access(arguments->args[0], F_OK) != 0) {
        fprintf(stderr, "%s: file not found\n", arguments->args[0]);
        return 1;
    }

    return 0;
}

int main(int argc, char *argv[]) {
    struct arguments arguments;

    arguments.data_structure = "gaddag";
    arguments.gzip = 0;
    arguments.alphabet_config = "./alphabet.json";
    arguments.max_word_length = 100000;

    argp_parse(&argp, argc, argv, 0, 0, &arguments);
    printf("data_structure=%s, gzip=%d, alphabet_config=%s, max_word_length=%d\n", arguments.data_structure,
           arguments.gzip, arguments.alphabet_config, arguments.max_word_length);
    if (validate_args(&arguments) != 0) {
        return EXIT_FAILURE;
    }
    read_alphabet_config(arguments.alphabet_config);
}
