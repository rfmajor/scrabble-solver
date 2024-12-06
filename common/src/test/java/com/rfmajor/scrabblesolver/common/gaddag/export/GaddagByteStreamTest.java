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
import java.util.Collections;
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
                Collections.emptyList(),
                Collections.emptyList()
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
        ByteArrayOutputStream metadataOutputStream = new ByteArrayOutputStream();

        outputStreamWriter.writeToOutputStreams(compressedByteGaddag, gaddagOutputStream, metadataOutputStream);

        byte[] gaddagBytes = gaddagOutputStream.toByteArray();
        byte[] metadataBytes = metadataOutputStream.toByteArray();

        ByteArrayInputStream gaddagInputStream = new ByteArrayInputStream(gaddagBytes);
        ByteArrayInputStream metadataInputStream = new ByteArrayInputStream(metadataBytes);

        CompressedByteGaddag actualGaddag = inputStreamReader.readFromInputStreams(gaddagInputStream, metadataInputStream);

        assertEquals(compressedByteGaddag, actualGaddag);
    }
}