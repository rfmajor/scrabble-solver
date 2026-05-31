#include <stdio.h>
#include <argp.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static char* SUPPORTED_DATA_STRUCTURES[] = { "gaddag", 0 };

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

static char doc[] = "A program for converting a dictionary of words into a traversable data structure used for word lookup in the scrabble game.";
static char args_doc[] = "DICTIONARY_FILE";
static struct argp_option options[] = {
    {"data-structure", 'd', "DATA_STRUCTURE", 0, "Data structure to use (default: gaddag)"},
    {"gzip", 'g', 0, 0, "Enable gzip compression (default: false)"},
    {"output", 'o', "OUTPUT", 0, "Output file path"},
    {"alphabet-config", 'c', "CONFIG_FILE", 0, "Path to alphabet config .json file (default: ./alphabet.json)"},
    {"max-word-length", 'm', "MAX_LENGTH", 0, "Maximum word length, words with longer length are skipped (default: none)"},
    {0}};

struct arguments
{
  char *args[1];
  char *data_structure;
  char *output;
  char *alphabet_config;
  int max_word_length;
  int gzip;
};

static error_t parse_opt (int key, char *arg, struct argp_state *state) {
  struct arguments *arguments = state->input;

  switch (key) {
    case 'd':
      arguments->data_structure = arg;
      break;
    case 'g':
      arguments->gzip = 1;
      break;
    case 'o':
      arguments->output = arg;
      break;
    case 'c':
      arguments->alphabet_config = arg;
      break;
    case 'm':
      sscanf(arg, "%d", &(arguments->max_word_length));
      break;

    case ARGP_KEY_ARG:
      if (state->arg_num > 0)
        argp_usage(state);

      arguments->args[state->arg_num] = arg;

      break;

    case ARGP_KEY_END:
      if (state->arg_num < 1)
        argp_usage(state);
      break;

    default:
      return ARGP_ERR_UNKNOWN;
    }

  return 0;
}

static struct argp argp = { options, parse_opt, args_doc, doc};

void print_usage(char *name) {
    printf("Usage: %s [OPTIONS]... DICTIONARY_FILE\n", name);
    printf("Options: \n");
    printf("  %-30s %s\n", "-h, --help", "Print this message and exit");
    printf("  %-30s %s\n", "-d, --data-structure", "Data structure to use (default: gaddag)");
    printf("  %-30s %s\n", "-c, --enable-compression", "Enable gzip compression (default: false)");
    printf("  %-30s %s\n", "-o, --output", "Output file path");
    printf("  %-30s %s\n", "-a, --alphabet-config", "Path to alphabet config .json file (default: ./alphabet.json)");
    printf("  %-30s %s\n", "-m, --max-word-length", "Maximum word length, words with longer length are skipped (default: none)");
    printf("Arguments: \n");
    printf("  %-30s %s\n\n", "DICTIONARY_FILE", "Plaintext file with one word per line");
    printf("The alphabet config should be a JSON file in the following format:\n\n");
    printf("%s\n", HELP_JSON);
}

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
        printf("Data structure not supported: %s\n", arguments->data_structure);
        return 1;
    }

    if (!arguments->output) {
        printf("Output file argument is mandatory\n");
        return 1;
    }

    if (access(arguments->alphabet_config, F_OK) != 0) {
        printf("Alphabet config file not found under path: %s\n", arguments->alphabet_config);
        return 1;
    }

    return 0;
}

int main(int argc, char *argv[]) {
    struct arguments arguments;

    arguments.data_structure = "gaddag";
    arguments.gzip = 0;
    arguments.output = 0;
    arguments.alphabet_config = "./alphabet.json";
    arguments.max_word_length = 100000;

    argp_parse(&argp, argc, argv, 0, 0, &arguments);
    printf("data_structure=%s, gzip=%d, output=%s, alphabet_config=%s, max_word_length=%d\n", arguments.data_structure, arguments.gzip, arguments.output, arguments.alphabet_config, arguments.max_word_length);
    if (validate_args(&arguments) != 0) {
        return EXIT_FAILURE;
    }
}
