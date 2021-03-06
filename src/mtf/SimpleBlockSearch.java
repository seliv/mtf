package mtf;

/**
 * Created with IntelliJ IDEA.
 * User: alexey
 * Date: 6/2/13
 * Time: 6:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleBlockSearch {
    private byte[] pattern;

    public void setPattern(byte[] pattern) {
        if ((pattern == null) || pattern.length == 0)
            throw new IllegalArgumentException("Empty pattern provided");
        this.pattern = pattern;
    }

    public boolean search(byte[] block, int len) {
        if (pattern == null)
            throw new IllegalStateException("Search pattern is not initialized");
        if (len > block.length)
            throw new IllegalArgumentException("Given block length is larger than actual block size");
        outer:
        for (int i = 0; i <= len - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (block[i + j] != pattern[j])
                    continue outer;
            }
            return true;
        }
        return false;
    }
}
