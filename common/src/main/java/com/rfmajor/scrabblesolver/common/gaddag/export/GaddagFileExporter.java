package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.GADDAG_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.METADATA_FILENAME;

@Slf4j
public class GaddagFileExporter implements GaddagExporter {
    @Override
    public void export(CompressedByteGaddag compressedByteGaddag, String directoryPath) {
        Path gaddagFile = Paths.get(directoryPath, GADDAG_FILENAME);
        Path metadataFile = Paths.get(directoryPath, METADATA_FILENAME);

        GaddagOutputStreamWriter writer = new GaddagOutputStreamWriter();
        try {
            writer.writeToOutputStreams(compressedByteGaddag,
                    new GZIPOutputStream(new FileOutputStream(new File(gaddagFile.toUri()))),
                    new FileOutputStream(new File(metadataFile.toUri()))
            );
        } catch (IOException e) {
            log.error("Unable to save the gaddag files", e);
            throw new RuntimeException(e);
        }
    }
}
