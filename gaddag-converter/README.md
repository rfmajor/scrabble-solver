1. Build (from the root dir):

```bash
 ./gradlew :gaddag-converter:fatJar
```

2. Run:

```bash
java -Xmx6g -jar gaddag-converter/build/libs/all-in-one-jar-0.0.1-SNAPSHOT.jar 16
```