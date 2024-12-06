package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ROOT_ARC;

@Slf4j
public class GaddagOutputStreamWriter {
    public void writeToOutputStreams(CompressedByteGaddag compressedByteGaddag,
                                     OutputStream gaddagOutputStream,
                                     OutputStream metadataOutputStream) throws IOException {
        Map<String, Integer> lengthsMap = new LinkedHashMap<>();
        Map<String, byte[]> byteData = new LinkedHashMap<>();

        byteData.put(ROOT_ARC, ExportingUtils.longToBytes(compressedByteGaddag.getRootArc()));
        byteData.put(ALPHABET, compressedByteGaddag.getAlphabet().asByteArray());
        byteData.put(DELIMITER, ExportingUtils.charToBytes(compressedByteGaddag.getDelimiter()));
        byteData.put(LETTER_SETS, ExportingUtils.intArrayToBytes(compressedByteGaddag.getLetterSets()));
        byteData.put(ARCS_AND_STATES, compressedByteGaddag.getArcsAndStates());

        try (gaddagOutputStream) {
            for (String name : byteData.keySet()) {
                byte[] bytes = byteData.get(name);
                lengthsMap.put(name, bytes.length);
                gaddagOutputStream.write(bytes, 0, bytes.length);
            }
        }

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(metadataOutputStream, StandardCharsets.UTF_8)) {
            for (String name : lengthsMap.keySet()) {
                outputStreamWriter.write(String.format("%s=%d\n", name, lengthsMap.get(name)));
            }
        }
    }
}
