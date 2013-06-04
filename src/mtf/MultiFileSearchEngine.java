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
    private boolean logEnabled;

    private final SimpleBlockSearch blockSearch;

    public MultiFileSearchEngine(Config config) {
        patternLength = config.getPattern().length;
        blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(config.getPattern());
        paddedBuffer = new byte[CHUNK_SIZE + patternLength];
        logEnabled = config.isLogEnabled();
    }

    public List searchFiles(List files) {
        List res = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            File f = (File) files.get(i);
            if (logEnabled) {
                String bar = getFileBarString(f);
                System.out.println("[" + Thread.currentThread().getName() + "] [" + bar + "] Processing " + f.getPath());
            }
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

    private static final String FULL_BAR = "------~~~~~~====xxxxXXXXXX";

    private String getFileBarString(File f) {
        int size = String.valueOf(f.length()).length(); // Number of digits in file size
        String bar = FULL_BAR.substring(0, Math.min(size * 2, FULL_BAR.length()));
        while (bar.length() < FULL_BAR.length())
            bar += " ";
        return bar;
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
