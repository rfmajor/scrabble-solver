package com.rfmajor.scrabblesolver.common.gaddag.export;

import com.rfmajor.scrabblesolver.common.gaddag.model.CompressedByteGaddag;

public interface GaddagReader {
    CompressedByteGaddag read(String directoryPath);
}
