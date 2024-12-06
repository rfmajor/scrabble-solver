package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;

public interface GaddagExporter {
    void export(CompressedByteGaddag gaddag, String filePath);
}
