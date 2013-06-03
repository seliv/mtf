package mtf;

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

    public synchronized boolean hasMoreFiles() {
        return !(queue.isEmpty() && noMoreFiles);
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Atomic "check if not empty" and "get".
     * @return List&lt;File&gt; or <code>null</code> if the queue is empty.
     */
    public synchronized List/*<File>*/ getFilesIfExist() {
        if (queue.isEmpty())
            return null;
        else
            return (List) queue.removeFirst();
    }
}
