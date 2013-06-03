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
    public static final int WORKER_COUNT = 4;
    
    public static void main(String[] args) {
        try {
            Config config = parseArguments(args);
            doConcurrentSearch(config.getPattern());
        } catch (IllegalArgumentException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }

    public static void doConcurrentSearch(byte[] pattern) {
        FileQueue queue = new FileQueue();
        ResultAccumulator accumulator = new ResultAccumulator();

        FileProducer producer = new FileProducer(queue);

        Thread[] workers = new Thread[WORKER_COUNT];
        for (int i = 0; i < WORKER_COUNT; i++) {
            SearchWorker w = new SearchWorker(queue, accumulator, pattern);
            Thread t = new Thread(w, "Worker" + i);
            workers[i] = t;
            t.start();
        }

        producer.searchDirectory(ROOT_DIRECTORY);
        queue.finishProcessing();
        for (int i = 0; i < WORKER_COUNT; i++)
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
            }

        List res = accumulator.getResults();
        showSearchResults(res);
    }

    public static void testSearchPatternInBlock() {
        byte[] text = "Is there anybody going to listen to my story".getBytes();
        byte[] sub = "any".getBytes();

        SimpleBlockSearch blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(sub);
        System.out.println("Result = " + blockSearch.search(text, text.length));
    }

    private static final String ROOT_DIRECTORY = "/Users/aselivanov/Downloads";

    public static void testSearchDirectory() {
        byte[] pattern = "apple".getBytes();

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
        showSearchResults(res);
    }

    private static void showSearchResults(List res) {
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

    private static Config parseArguments(String[] args) {
        Config config = new Config();
        int i = 0;
        while (i < args.length) {
            if (args[i].contains("root")) {
                if (i + 1 < args.length) {
                    config.setRootDirectory(args[i + 1]);
                } else {
                    throw new IllegalArgumentException("No value provided for -root option.");
                }
                i += 2;
                continue;
            }
            if (args[i].contains("string")) {
                if (i + 1 < args.length) {
                    config.setStringPattern(args[i + 1]);
                } else {
                    throw new IllegalArgumentException("No value provided for -string option.");
                }
                i += 2;
                continue;
            }
            if (args[i].contains("hex")) {
                if (i + 1 < args.length) {
                    config.setHexPattern(args[i + 1]);
                } else {
                    throw new IllegalArgumentException("No value provided for -hex option.");
                }
                i += 2;
                continue;
            }
            if (args[i].contains("threads")) {
                if (i + 1 < args.length) {
                    try {
                        config.setThreads(Integer.parseInt(args[i + 1]));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(ex.getMessage());
                    }
                } else {
                    throw new IllegalArgumentException("No value provided for -threads option.");
                }
                i += 2;
                continue;
            }
            if (args[i].contains("debug")) {
                config.setLogEnabled(true);
                i++;
            }
        }
        config.validate();
        return config;
    }
}
