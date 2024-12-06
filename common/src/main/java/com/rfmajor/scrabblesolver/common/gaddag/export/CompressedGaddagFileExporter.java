package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.GADDAG_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.METADATA_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ROOT_ARC;

@Slf4j
public class CompressedGaddagFileExporter implements CompressedGaddagExporter {
    @Override
    public void export(CompressedByteGaddag compressedByteGaddag, String directoryPath) {
        Path gaddagFile = Paths.get(directoryPath, GADDAG_FILENAME);
        Path metadataFile = Paths.get(directoryPath, METADATA_FILENAME);

        Map<String, Integer> lengthsMap = new LinkedHashMap<>();
        Map<String, byte[]> byteData = new LinkedHashMap<>();

        byteData.put(ROOT_ARC, ExportingUtils.longToBytes(compressedByteGaddag.getRootArc()));
        byteData.put(ALPHABET, compressedByteGaddag.getAlphabet().asByteArray());
        byteData.put(DELIMITER, ExportingUtils.charToBytes(compressedByteGaddag.getDelimiter()));
        byteData.put(LETTER_SETS, ExportingUtils.intArrayToBytes(compressedByteGaddag.getLetterSets()));
        byteData.put(ARCS_AND_STATES, compressedByteGaddag.getArcsAndStates());

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(new File(gaddagFile.toUri())))) {
            for (String name : byteData.keySet()) {
                byte[] bytes = byteData.get(name);
                lengthsMap.put(name, bytes.length);
                gzipOutputStream.write(bytes, 0, bytes.length);
            }
        } catch (IOException e) {
            log.error("Unable to save the binary file", e);
            throw new RuntimeException(e);
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(metadataFile.toFile()))) {
            for (String name : lengthsMap.keySet()) {
                bufferedWriter.write(String.format("%s=%d\n", name, lengthsMap.get(name)));
            }
        } catch (IOException e) {
            log.error("Unable to save the metadata file", e);
            throw new RuntimeException(e);
        }
    }
}
