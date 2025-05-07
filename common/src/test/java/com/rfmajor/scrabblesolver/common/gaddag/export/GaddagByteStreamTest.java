package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.TestUtils;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagByteArrayCompressor;
import com.rfmajor.scrabblesolver.common.gaddag.convert.ExpandedGaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.convert.GaddagConverter;
import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;
import com.rfmajor.scrabblesolver.common.gaddag.model.ExpandedGaddag;
import com.rfmajor.scrabblesolver.common.scrabble.Alphabet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GaddagByteStreamTest {
    private GaddagOutputStreamWriter outputStreamWriter;
    private GaddagInputStreamReader inputStreamReader;
    private CompressedByteGaddag compressedByteGaddag;

    @BeforeEach
    void setUp() {
        outputStreamWriter = new GaddagOutputStreamWriter();
        inputStreamReader = new GaddagInputStreamReader();

        Alphabet alphabet = new Alphabet(
                TestUtils.mapStringToLettersList("abcdefghijklmnopqrstuvwxyz#"),
                List.of(1, 2, 3, 4, 1, 5, 1, 3, 5, 6, 2, 5, 1, 1, 2, 3, 4, 1, 5, 1, 3, 5, 6, 2, 5, 1),
                List.of(1, 2, 1, 3, 1, 6, 1, 3, 3, 6, 2, 3, 1, 2, 2, 2, 2, 1, 3, 1, 3, 6, 2, 2, 2, 1)
        );
        List<String> words = List.of("pa", "pi", "op", "able", "payable", "parable", "pay", "par", "part", "park");

        GaddagConverter<Long> expandedGaddagConverter = new ExpandedGaddagConverter();
        ExpandedGaddagByteArrayCompressor expandedGaddagByteArrayCompressor = new ExpandedGaddagByteArrayCompressor();

        ExpandedGaddag expandedGaddag = (ExpandedGaddag) expandedGaddagConverter.convert(words, alphabet);
        compressedByteGaddag = expandedGaddagByteArrayCompressor.minimize(expandedGaddag);
    }

    @Test
    void testGaddagIsUnchangedAfterExportingAndImporting() throws IOException {
        ByteArrayOutputStream gaddagOutputStream = new ByteArrayOutputStream();
        outputStreamWriter.writeToOutputStream(compressedByteGaddag, gaddagOutputStream);
        byte[] gaddagBytes = gaddagOutputStream.toByteArray();

        ByteArrayInputStream gaddagInputStream = new ByteArrayInputStream(gaddagBytes);
        CompressedByteGaddag actualGaddag = inputStreamReader.readFromInputStream(gaddagInputStream);

        assertEquals(compressedByteGaddag, actualGaddag);
    }
}