package com.rfmajor.scrabblesolver.movegen.gaddag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompressedGaddagFileWriter {
    public void write(CompressedByteGaddag compressedByteGaddag) throws IOException {
        Path newFile = Paths.get("gaddag.dat");

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(newFile.toUri()))) {
            fileOutputStream.write(compressedByteGaddag.arcsAndStates);
        }
    }
}
