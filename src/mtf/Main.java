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

    public static void main(String[] args) {
        testSearchDirectory();
    }

    public static void testSearchPatternInBlock() {
        byte[] text = stringToBytes("Is there anybody going to listen to my story");
        byte[] sub = stringToBytes("any");

        SimpleBlockSearch blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(sub);
        System.out.println("Result = " + blockSearch.search(text, text.length));
    }

    private static final String ROOT_DIRECTORY = "/Users/alexey/Downloads";

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
            System.out.println("Pattern found in the following files:");
            for (int i = 0; i < res.size(); i++) {
                File f = (File) res.get(i);
                System.out.println(f.getPath());
            }
        }
    }
}
