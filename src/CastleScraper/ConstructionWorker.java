package CastleScraper;

import CastleScraper.data.ScottishCastlePlacemark;
import CastleScraper.data.WikipediaListPage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * The ConstructionWorker class is responsible for constructing the workloads.
 * So it connects to the initial page and looks for links of interest.
 *
 * @author al
 */
public class ConstructionWorker implements Callable<String> {

    /**
     * the URL that this worker uses to get data
     */
    private URL target;

    /**
     * 
     * @return - the URL that this worker uses to get data
     */
    public String getTarget() {
        return target.toString();
    }
    /**
     * The controller which drives this worker.
     */
    private final LocationController owner;
    private final Logger theLogger;

    /**
     * Constructs a worker.
     *
     * @param owner The owner of this object
     * @param theTarget
     * @param logger  
     */
    public ConstructionWorker(LocationController owner,
            String theTarget,
            Logger logger) {
        this.owner = owner;

        try {
            this.target = new URL(theTarget);
        } catch (MalformedURLException ex) {
            this.target = null;
        }

        theLogger = logger;
    }

    /**
     * @return - "Complete"
     */
    public String call() {
        WikipediaListPage thePage = new WikipediaListPage(target, theLogger);
        processFile(thePage);
        return "Complete";
    }

    /**
     * Processes the wikipedia list page to produce location workers
     * @param thePage - valid parsed wikipedia list page
     */
    private void processFile(WikipediaListPage thePage) {
        List<ScottishCastlePlacemark> thePlacemarks = thePage.extractLinks();
        int placemarksLength = thePlacemarks.size();

         for (int i = 0; i < placemarksLength; ++i) {
             owner.addWorkload(thePlacemarks.get(i));
         }
    }
}
