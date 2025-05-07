package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.GADDAG_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ROOT_ARC;

@Slf4j
public class GaddagFileExporter implements GaddagExporter {
    public static final List<String> METADATA_ENTRIES = Arrays.asList(
            ROOT_ARC,
            ALPHABET,
            DELIMITER,
            LETTER_SETS,
            ARCS_AND_STATES
    );

    @Override
    public void export(CompressedByteGaddag compressedByteGaddag, String directoryPath) {
        Path gaddagFile = Paths.get(directoryPath, GADDAG_FILENAME);

        GaddagOutputStreamWriter writer = new GaddagOutputStreamWriter();
        try {
            writer.writeToOutputStream(compressedByteGaddag,
                    new GZIPOutputStream(new FileOutputStream(new File(gaddagFile.toUri()))));
        } catch (IOException e) {
            log.error("Unable to save the gaddag files", e);
            throw new RuntimeException(e);
        }
    }
}
