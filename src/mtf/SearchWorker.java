package mtf;

import java.util.List;

/**
 * Represents a single thread of concurrent search processing.
 * Takes a list of files to processes from the queue and returns a list of matched files.
 */
public class SearchWorker implements Runnable {
    private final FileQueue queue;
    private final ResultAccumulator accumulator;
    private final MultiFileSearchEngine engine;

    public SearchWorker(FileQueue queue, ResultAccumulator accumulator, byte[] pattern) {
        this.queue = queue;
        this.accumulator = accumulator;
        engine = new MultiFileSearchEngine(pattern);
    }

    public void run() {
        List files;
        try {
            while ((files = queue.getFilesAndWait()) != null) {
                System.out.println();
                List res = engine.searchFiles(files);
                accumulator.addResults(res);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
        }
    }
}
