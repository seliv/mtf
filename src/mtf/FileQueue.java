package mtf;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Collects files from a producer (potentially from several concurrent producers) and feeds them to concurrent consumers.
 * Implements bulk processing, block size is hard-coded.
 */
public class FileQueue {
    public static final int FILE_BLOCK_SIZE = 10;

    private final LinkedList/*<List<File>>*/ queue = new LinkedList();
    private boolean noMoreFiles = false;

    public synchronized void putFiles(List/*<File>*/ fileBlock) {
        queue.addLast(fileBlock);
        notifyAll();
    }

    public synchronized void finishProcessing() {
        noMoreFiles = true;
    }

    /**
     * The method blocks to wait for a new List&lt;File&gt; to process.
     * @return List&lt;File&gt; to process (possibly an empty one) or <code>null</code> if there are no more files to process.
     */
    public synchronized List/*<File>*/ getFilesAndWait() {
        while (queue.isEmpty() && !noMoreFiles) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
            }
        }
        if (!queue.isEmpty())
            return (List) queue.removeFirst();
        else {
            if (noMoreFiles)
                return null;
            else
                return Collections.EMPTY_LIST;
        }
    }
}
