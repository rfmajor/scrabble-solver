package com.rfmajor.scrabblesolver.common.gaddag.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class CompressedGaddagFileReader {
    public byte[] read() throws IOException {
        Path newFile = Paths.get("gaddag.bin");

        try (
                GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(new File(newFile.toUri())));
        ) {
            return gzipInputStream.readAllBytes();
        }
    }
}
