package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Slf4j
public class GaddagFileReader implements GaddagReader {
    @Override
    public CompressedByteGaddag read(String gaddagFilePath) {
        Path gaddagFile = Path.of(gaddagFilePath);

        GaddagInputStreamReader gaddagInputStreamReader = new GaddagInputStreamReader();
        try {
            return gaddagInputStreamReader.readFromInputStream(
                    new GZIPInputStream(new FileInputStream(gaddagFile.toFile()))
            );
        } catch (IOException e) {
            log.error("Unable to load the gaddag files", e);
            throw new RuntimeException(e);
        }
    }
}
