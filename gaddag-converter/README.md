1. Build (from the root dir):

```bash
 ./gradlew :gaddag-converter:fatJar
```

2. Run:

```bash
java -Xmx6g -jar gaddag-converter/build/libs/gaddag-converter.jar output/alphabet.json output/slowa.txt output/gaddag
```