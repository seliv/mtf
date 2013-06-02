package mtf;

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
        byte[] text = stringToBytes("Is there anybody going to listen to my story");
        byte[] sub = stringToBytes("any");

        SimpleBlockSearch blockSearch = new SimpleBlockSearch();
        blockSearch.setPattern(sub);
        System.out.println("Result = " + blockSearch.search(text));
    }
}
