package mtf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: alexey
 * Date: 6/2/13
 * Time: 5:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    private static byte[] stringToBytes(String s) {
        if (s == null)
            throw new IllegalArgumentException("Can't convert null string to bytes");
        return s.getBytes();
    }

    public static final int CONSUMERS_COUNT = 4;
    
    public static void main(String[] args) {
        byte[] pattern = stringToBytes("apple");

        FileQueue queue = new FileQueue();
        ResultAccumulator accumulator = new ResultAccumulator();
        
        FileProducer producer = new FileProducer(queue);
        
        Thread[] consumers = new Thread[CONSUMERS_COUNT];
        for (int i = 0; i < CONSUMERS_COUNT; i++) {
            SearchConsumer consumer = new SearchConsumer(queue, accumulator, pattern);
            Thread t = new Thread(consumer, "Consumer" + i);
            consumers[i] = t;
            t.start();
        }

        producer.searchDirectory(ROOT_DIRECTORY);
        queue.finishProcessing();
        for (int i = 0; i < CONSUMERS_COUNT; i++)
            try {
                consumers[i].join();
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
            }

        List res = accumulator.getResults();
        if (res.size() == 0) {
            System.out.println("Pattern not found in " + ROOT_DIRECTORY + ".");
        } else {
            System.out.println("Pattern found in the following " + res.size() + " files:");
            for (int i = 0; i < res.size(); i++) {
                File f = (File) res.get(i);
                System.out.println(f.getPath());
            }
        }
    }

    public static void testSearchPatternInBlock() {
        byte[] text = stringToBytes("Is there anybody going to listen to my story");
        byte[] sub = stringToBytes("any");

        SimpleBlockSearch blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(sub);
        System.out.println("Result = " + blockSearch.search(text, text.length));
    }

    private static final String ROOT_DIRECTORY = "/Users/aselivanov/Downloads";

    public static void testSearchDirectory() {
        byte[] pattern = stringToBytes("apple");

        File file = new File(ROOT_DIRECTORY);
        if (!file.isDirectory()) {
            System.out.println(ROOT_DIRECTORY + " is not a directory");
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

        MultiFileSearchEngine worker = new MultiFileSearchEngine(pattern);
        List res = worker.searchFiles(files);
        if (res.size() == 0) {
            System.out.println("Pattern not found in " + ROOT_DIRECTORY + ".");
        } else {
            System.out.println("Pattern found in the following " + res.size() + " files:");
            for (int i = 0; i < res.size(); i++) {
                File f = (File) res.get(i);
                System.out.println(f.getPath());
            }
        }
    }
}
