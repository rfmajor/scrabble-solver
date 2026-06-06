CC=gcc
DEPS = alphabet.h cJSON.h

convertDictionary: main.c alphabet.c cJSON.c
	$(CC) main.c alphabet.c cJSON.c -largp -L/opt/homebrew/Cellar/argp-standalone/1.5.0/lib/ -I/opt/homebrew/Cellar/argp-standalone/1.5.0/include/ -o convertDictionary

