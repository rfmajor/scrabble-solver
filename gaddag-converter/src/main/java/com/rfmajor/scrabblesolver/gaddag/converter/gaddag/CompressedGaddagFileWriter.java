package com.rfmajor.scrabblesolver.gaddag.converter.gaddag;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class CompressedGaddagFileWriter {
    public void write(CompressedByteGaddag compressedByteGaddag) throws IOException {
        Path newFile = Paths.get("gaddag.bin");

        try (
                GZIPOutputStream gzipOutputStream =
                        new GZIPOutputStream(new FileOutputStream(new File(newFile.toUri())));
        ) {
            byte[] buffer = compressedByteGaddag.getArcsAndStates();
            gzipOutputStream.write(buffer, 0, buffer.length);
        }
    }
}
