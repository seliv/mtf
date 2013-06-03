package mtf;

import java.io.File;
import java.util.ArrayList;

/**
 * Lists the directory and schedules files for search, runs in the main thread.
 */
public class FileProducer {
    private final FileQueue queue;

    public FileProducer(FileQueue queue) {
        this.queue = queue;
    }

    public void searchDirectory(String root) {
        File file = new File(root);
        if (!file.isDirectory()) {
            System.out.println(root + " is not a directory");
            System.exit(1);
        }
        ArrayList files = new ArrayList();
        File[] allFiles = file.listFiles();
        for (int i = 0; i < allFiles.length; i++) {
            File f = allFiles[i];
            if (!f.isDirectory())
                files.add(f);
        }
        System.out.println("Total files to process: " + files.size());

        for (int i = 0; i < files.size(); i += FileQueue.FILE_BLOCK_SIZE) {
            ArrayList/*<File>*/ chunk = new ArrayList();
            for (int j = 0; j <= FileQueue.FILE_BLOCK_SIZE; j++) {
                if ((i + j) < files.size()) {
                    chunk.add(files.get(i + j));
                }
            }
            queue.putFiles(chunk);
        }
    }
}
