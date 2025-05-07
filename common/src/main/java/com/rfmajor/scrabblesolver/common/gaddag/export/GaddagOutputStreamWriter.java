package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.rfmajor.scrabblesolver.common.gaddag.export.GaddagFileExporter.METADATA_ENTRIES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ROOT_ARC;

@Slf4j
public class GaddagOutputStreamWriter {
    public void writeToOutputStream(CompressedByteGaddag compressedByteGaddag,
                                    OutputStream gaddagOutputStream) throws IOException {
        Map<String, byte[]> byteData = new LinkedHashMap<>();
        Map<String, Integer> byteMetadata = new LinkedHashMap<>();

        byteData.put(ROOT_ARC, ByteStreamUtils.longToBytes(compressedByteGaddag.getRootArc()));
        byteData.put(ALPHABET, compressedByteGaddag.getAlphabet().asByteArray());
        byteData.put(DELIMITER, ByteStreamUtils.charToBytes(compressedByteGaddag.getDelimiter()));
        byteData.put(LETTER_SETS, ByteStreamUtils.intArrayToBytes(compressedByteGaddag.getLetterSets()));
        byteData.put(ARCS_AND_STATES, compressedByteGaddag.getArcsAndStates());

        // header of the file, size: 5 * 4 bytes = 20 bytes
        for (String name : METADATA_ENTRIES) {
            byteMetadata.put(name, byteData.get(name).length);
        }

        try (gaddagOutputStream) {
            byteMetadata.forEach((name, metadata) -> {
                byte[] metadataBytes = ByteStreamUtils.intToBytes(metadata);
                try {
                    gaddagOutputStream.write(metadataBytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            byteData.forEach((name, bytes) -> {
                try {
                    gaddagOutputStream.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
