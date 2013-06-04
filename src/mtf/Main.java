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
    public static void main(String[] args) {
        try {
            Config config = parseArguments(args);
            doConcurrentSearch(config);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }

    public static void doConcurrentSearch(Config config) {
        FileQueue queue = new FileQueue();
        ResultAccumulator accumulator = new ResultAccumulator();
        FileProducer producer = new FileProducer(queue);

        int workerCount = config.getThreads();

        long startTime = System.currentTimeMillis();

        Thread[] workers = new Thread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            SearchWorker w = new SearchWorker(queue, accumulator, config);
            Thread t = new Thread(w, "Worker" + i);
            workers[i] = t;
            t.start();
        }

        producer.searchDirectory(config.getRootDirectory());
        queue.finishProcessing();
        for (int i = 0; i < workerCount; i++)
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " interrupted: " + e);
            }

        long stopTime = System.currentTimeMillis();
        long timeDiff = stopTime - startTime;

        if (config.isLogEnabled()) {
            List res = accumulator.getResults();
            showSearchResults(res);
        }

        System.out.println("Processing time: " + (timeDiff / 1000) + "." + timeDiff % 1000 + " seconds.");
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

        Config config = new Config();
        config.setStringPattern("apple");
        config.setLogEnabled(true);

        MultiFileSearchEngine worker = new MultiFileSearchEngine(config);
        List res = worker.searchFiles(files);
        showSearchResults(res);
    }

    private static void showSearchResults(List res) {
        if (res.size() == 0) {
            System.out.println("Pattern not found.");
        } else {
            System.out.println("Pattern found in the following " + res.size() + " files:");
            for (int i = 0; i < res.size(); i++) {
                File f = (File) res.get(i);
                System.out.println(f.getPath());
            }
        }
    }

    private static Config parseArguments(String[] args) {
        if (args.length == 0) {
            System.err.println("Command line options:");
            System.err.println("    -root - root directory to start search from (current directory by default)");
            System.err.println("    -string - text to search");
            System.err.println("    -hex - hexadecimal sequence of bytes to search");
            System.err.println("    -threads - number of finder threads to use (4 by default)");
            System.err.println("    -nolog - hide verbose output (only shows total files to process and time elapsed, useful for benchmarking)");
            System.err.println("    -nodebug - same as -nolog");
            System.err.println();
            System.err.println("Either -string or -hex must be specified.");
            System.exit(-1);
        }
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
            if (args[i].contains("nolog")) {
                config.setLogEnabled(false);
                i++;
                continue;
            }
            if (args[i].contains("nodebug")) {
                config.setLogEnabled(false);
                i++;
                continue;
            }
            throw new IllegalArgumentException("Unknown option: " + args[i]);
        }
        config.validate();
        return config;
    }
}
