package mtf;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates found files from several concurrent search workers.
 */
public class ResultAccumulator {
    private final ArrayList/*<File>*/ results = new ArrayList();

    public void addResults(List/*<File>*/ newResults) {
        if  (newResults.size() > 0)
            synchronized (results) {
                results.addAll(newResults);
            }
    }

    public List/*<File>*/ getResults() {
        synchronized (results) {
            return new ArrayList(results);
        }
    }
}
