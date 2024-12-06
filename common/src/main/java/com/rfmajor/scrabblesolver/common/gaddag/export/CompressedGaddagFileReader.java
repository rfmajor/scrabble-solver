package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.GADDAG_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.METADATA_FILENAME;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ExportingUtils.ROOT_ARC;

public class CompressedGaddagFileReader {

    public CompressedByteGaddag read(String directoryPath) {
        Path gaddagFile = Paths.get(directoryPath, GADDAG_FILENAME);
        Path metadataFile = Paths.get(directoryPath, METADATA_FILENAME);

        Map<String, Integer> metadata = null;
        try {
            metadata = readMetadataFile(metadataFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Needed metadata file couldn't be read", e);
        }
        Map<String, byte[]> dataInBytes = new LinkedHashMap<>();


        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(gaddagFile.toFile()))) {
            for (String name : metadata.keySet()) {
                byte[] data = gzipInputStream.readNBytes(metadata.get(name));
                dataInBytes.put(name, data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Long rootArc = ExportingUtils.bytesToLong(dataInBytes.get(ROOT_ARC));
        Alphabet alphabet = readAlphabet(dataInBytes.get(ALPHABET));
        char delimiter = ExportingUtils.bytesToChar(dataInBytes.get(DELIMITER));
        int[] letterSets = ExportingUtils.bytesToIntArray(dataInBytes.get(LETTER_SETS));
        byte[] arcsAndStatesBytes = dataInBytes.get(ARCS_AND_STATES);

        return new CompressedByteGaddag(rootArc, alphabet, delimiter, arcsAndStatesBytes, letterSets);
    }

    private Alphabet readAlphabet(byte[] alphabetBytes) {
        int length = alphabetBytes.length / Alphabet.BYTE_ARRAY_DIVISOR;

        byte[] indicesToLettersBytes = Arrays.copyOfRange(alphabetBytes, 0, length * 2);
        byte[] indicesToPointsBytes = Arrays.copyOfRange(alphabetBytes, length * 2, length * 6);
        byte[] indicesToQuantitiesBytes = Arrays.copyOfRange(alphabetBytes, length * 6, length * 10);

        char[] indicesToLetters = ExportingUtils.bytesToCharArray(indicesToLettersBytes);
        int[] indicesToPoints = ExportingUtils.bytesToIntArray(indicesToPointsBytes);
        int[] indicesToQuantities = ExportingUtils.bytesToIntArray(indicesToQuantitiesBytes);

        return new Alphabet(indicesToLetters, indicesToPoints, indicesToQuantities);
    }

    private Map<String, Integer> readMetadataFile(Path metadataFile) throws IOException {
        Map<String, Integer> metadata = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                metadata.put(parts[0], Integer.parseInt(parts[1]));
            }
        }

        return metadata;
    }
}
