package com.rfmajor.scrabblesolver.movegen.gaddag;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

@Slf4j
public class FileWordIterable implements Iterable<String>, AutoCloseable {
    private final FileWordIterator iterator;

    public FileWordIterable(InputStream inputStream) {
        this.iterator = new FileWordIterator(inputStream);
    }

    @Override
    public Iterator<String> iterator() {
        return iterator;
    }

    @Override
    public void close() throws Exception {
        iterator.reader.close();
    }


    private static class FileWordIterator implements Iterator<String> {
        private final BufferedReader reader;

        public FileWordIterator(InputStream inputStream) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            this.line = tryReadLine();
        }

        private String line;

        @Override
        public boolean hasNext() {
            return line != null;
        }

        @Override
        public String next() {
            String oldLine = line;
            line = tryReadLine();

            return oldLine;
        }

        private String tryReadLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                log.error("Unrecoverable IO error", e);
                throw new RuntimeException(e);
            }
        }
    }
}
