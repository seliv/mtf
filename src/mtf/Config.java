package mtf;

/**
 * Contains parameters provided as command line arguments.
 */
public class Config {
    private byte[] pattern;
    private boolean logEnabled = false;
    private String rootDirectory;
    private int threads;

    private static byte[] stringToBytes(String s) {
        if (s == null)
            throw new IllegalArgumentException("Can't convert null string to bytes");
        return s.getBytes();
    }

    public static byte[] hexStringToByteArray(String s) {
        if ((s.length() % 2) != 0)
            s = "0" + s;
        int len = s.length();
        byte[] res = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            res[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return res;
    }

    public void validate() {
        if (pattern == null)
            throw new IllegalArgumentException("No search pattern provided.");
        if (pattern.length == 0)
            throw new IllegalArgumentException("Search pattern is empty.");
        if ((rootDirectory == null) || (rootDirectory.length() == 0)) {
            System.err.println("No root directory provided; using current directory as a root");
            rootDirectory = ".";
        }
        if (threads == 0) {
            System.err.println("No thread count specified; using default value: 4");
            threads = 4;
        }
    }

    public void setStringPattern(String pattern) {
        this.pattern = stringToBytes(pattern);
    }

    public void setHexPattern(String hexPattern) {
        this.pattern = hexStringToByteArray(hexPattern);
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public byte[] getPattern() {
        return pattern;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public int getThreads() {
        return threads;
    }
}
