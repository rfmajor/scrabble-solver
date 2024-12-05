package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;

public interface CompressedGaddagExporter {
    void export(CompressedByteGaddag gaddag, String filePath);
}
