package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ALPHABET;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ARCS_AND_STATES;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.DELIMITER;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.LETTER_SETS;
import static com.rfmajor.scrabblesolver.common.gaddag.utils.ByteStreamUtils.ROOT_ARC;

public class GaddagInputStreamReader {
    public CompressedByteGaddag readFromInputStreams(InputStream gaddagInputStream, InputStream metadataInputStream)
            throws IOException {
        Map<String, Integer> metadata = readMetadataStream(metadataInputStream);
        Map<String, byte[]> dataInBytes = new LinkedHashMap<>();

        try (gaddagInputStream) {
            for (String name : metadata.keySet()) {
                byte[] data = gaddagInputStream.readNBytes(metadata.get(name));
                dataInBytes.put(name, data);
            }
        }

        Long rootArc = ByteStreamUtils.bytesToLong(dataInBytes.get(ROOT_ARC));
        Alphabet alphabet = readAlphabet(dataInBytes.get(ALPHABET));
        char delimiter = ByteStreamUtils.bytesToChar(dataInBytes.get(DELIMITER));
        int[] letterSets = ByteStreamUtils.bytesToIntArray(dataInBytes.get(LETTER_SETS));
        byte[] arcsAndStatesBytes = dataInBytes.get(ARCS_AND_STATES);

        return new CompressedByteGaddag(rootArc, alphabet, delimiter, arcsAndStatesBytes, letterSets);
    }

    private Map<String, Integer> readMetadataStream(InputStream metadataStream) throws IOException {
        Map<String, Integer> metadata = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(metadataStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                metadata.put(parts[0], Integer.parseInt(parts[1]));
            }
        }

        return metadata;
    }

    private Alphabet readAlphabet(byte[] alphabetBytes) {
        int length = alphabetBytes.length / Alphabet.BYTE_ARRAY_DIVISOR;

        byte[] indicesToLettersBytes = Arrays.copyOfRange(alphabetBytes, 0, length * 2);
        byte[] indicesToPointsBytes = Arrays.copyOfRange(alphabetBytes, length * 2, length * 6);
        byte[] indicesToQuantitiesBytes = Arrays.copyOfRange(alphabetBytes, length * 6, length * 10);

        char[] indicesToLetters = ByteStreamUtils.bytesToCharArray(indicesToLettersBytes);
        int[] indicesToPoints = ByteStreamUtils.bytesToIntArray(indicesToPointsBytes);
        int[] indicesToQuantities = ByteStreamUtils.bytesToIntArray(indicesToQuantitiesBytes);

        return new Alphabet(indicesToLetters, indicesToPoints, indicesToQuantities);
    }
}