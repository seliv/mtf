package mtf;

import java.util.List;

/**
 * Represents a single thread of concurrent search processing.
 * Takes a list of files to processes from the queue and returns a list of matched files.
 */
public class SearchConsumer implements Runnable {
    private final FileQueue queue;
    private final ResultAccumulator accumulator;
    private final MultiFileSearchEngine engine;

    public SearchConsumer(FileQueue queue, ResultAccumulator accumulator, byte[] pattern) {
        this.queue = queue;
        this.accumulator = accumulator;
        engine = new MultiFileSearchEngine(pattern);
    }

    public void run() {
        while (queue.hasMoreFiles()) {
            List files;
            while ((files = queue.getFilesIfExist()) == null) {
                if (!queue.hasMoreFiles())
                    return;
                try {
                    synchronized (queue) {
                        queue.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
                }
            }
            List res = engine.searchFiles(files);
            accumulator.addResults(res);
        }
    }
}
