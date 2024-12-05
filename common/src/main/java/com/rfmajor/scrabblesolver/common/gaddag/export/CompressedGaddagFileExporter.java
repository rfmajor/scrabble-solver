package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class CompressedGaddagFileExporter implements CompressedGaddagExporter {
    @Override
    public void export(CompressedByteGaddag compressedByteGaddag, String filePath) {
        Path newFile = Paths.get(filePath);

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(new File(newFile.toUri())))) {
            byte[] buffer = compressedByteGaddag.getArcsAndStates();
            gzipOutputStream.write(buffer, 0, buffer.length);
        } catch (IOException e) {
            log.error("Unable to save the gaddag model", e);
            throw new RuntimeException(e);
        }
    }
}