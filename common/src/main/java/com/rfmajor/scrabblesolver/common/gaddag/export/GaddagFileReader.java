package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.GADDAG_FILENAME;

@Slf4j
public class GaddagFileReader implements GaddagReader {
    @Override
    public CompressedByteGaddag read(String directoryPath) {
        Path gaddagFile = Paths.get(directoryPath, GADDAG_FILENAME);

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
