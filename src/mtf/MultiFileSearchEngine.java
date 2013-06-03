package mtf;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the logic for searching the pattern in a list of files (not bothering of multi-threading issues).
 */
public class MultiFileSearchEngine {
    private static final int CHUNK_SIZE = 1024 * 1024;
    private final int patternLength;
    private byte[] buffer = new byte[CHUNK_SIZE];
    /**
     * A buffer copy that additionally holds the tail of previous chunk to find the pattern if it is broken by a chunk edge.
     * Implemented as a separate variable for clarity, can be combined with file reading buffer.
     */
    private byte[] paddedBuffer;

    private final SimpleBlockSearch blockSearch;

    public MultiFileSearchEngine(byte[] pattern) {
        patternLength = pattern.length;
        blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(pattern);
        paddedBuffer = new byte[CHUNK_SIZE + patternLength];
    }

    public List searchFiles(List files) {
        List res = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            File f = (File) files.get(i);
            System.out.println("[" + Thread.currentThread().getName() + "] Processing " + f.getPath());
            try {
                if (searchFile(f)) {
                    res.add(f);
                }
            } catch (IOException e) {
                System.err.println("IOException for " + f.getPath() + ": " + e);
            }
        }
        return res;
    }

    /**
     * Ensures that either buffer gets filled (possibly with multiple read operations) or end of file is reached.
     * @return Number of bytes actually received, can be less than buffer length only if EOF is reached.
     */
    private int fillBuffer(InputStream is) throws IOException {
        int len = buffer.length;
        int pos = 0;
        while (pos < buffer.length) {
            int read = is.read(buffer, pos, len);
            if (read == -1)
                return pos;
            pos += read;
            len -= read;
        }
        return pos;
    }

    private boolean searchFile(File file) throws IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
        int len;
        int paddedLen = 0;
        do {
            len = fillBuffer(is);
            System.arraycopy(buffer, 0, paddedBuffer, paddedLen, len);
            if (blockSearch.search(paddedBuffer, len + paddedLen))
                return true;

            // Attaching the tail of this chunk to the beginning of the next chunk
            System.arraycopy(paddedBuffer, paddedBuffer.length - patternLength, paddedBuffer, 0, patternLength);
            paddedLen = patternLength;
        } while (len == buffer.length);

        return false;
    }
}
