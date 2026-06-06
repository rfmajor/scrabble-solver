#ifndef ALPHABET
#define ALPHABET

typedef struct {
    char letter;
    int index;
    int quantity;
    int value;
} Letter;

typedef struct {
    Letter delimiter;
    Letter *letters;
} AlphabetConfig;

AlphabetConfig* read_alphabet_config(const char *config_f);

#endif // !ALPHABET

