package CastleScraper;

/**
 * The LocationController class is the main organizational class for
 * managing spidering.  It delegates work to the Worker classes.
 *
 * @author al
 */
import CastleScraper.data.ScottishCastlePlacemark;
import CastleScraper.data.ScottishCastles;
import CastleScraper.ui.ITaskComplete;
import CastleScraper.ui.ScrapeProgressDisplay;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationController extends Thread {

    private ExecutorService threadExecutor;
    private ITaskComplete manager;
    private boolean halted = false;
    private ArrayList<Future<ScottishCastlePlacemark>> theWorkers = new ArrayList<Future<ScottishCastlePlacemark>>();
    private int thePoolSize;
    private final Logger theLogger;
    private final ScottishCastles theCastles;
    private final String theURL;
    private ScrapeProgressDisplay theProgressDisplay;

    /**
     *
     * @param url - the url of the start page
     * @param poolSize - number of threads to start in the thread pool
     * @param logger - valid logger
     * 
     * @precon - the url is valid and non null
     * @precon - the pool size > 0 and < 10
     * @postcon - controller constructed and ready to run
     */
    public LocationController(String url,
            int poolSize,
            Logger logger) {
        thePoolSize = poolSize;
        theLogger = logger;
        theCastles = new ScottishCastles(theLogger);
        theURL = url;
    }

    /**
     * Sets the object to be called when controller is finished working.
     * @param theManager
     */
    public void setManager(ITaskComplete theManager) {
        manager = theManager;
    }

    /**
     * Sets the object to be called when the progress is updated
     * @param theDisplay 
     */
    public void setProgressDisplay(ScrapeProgressDisplay theDisplay) {
        theProgressDisplay = theDisplay;
    }

    /**
     * The main loop of the LocationController. 
     * Returns when all the controllers work is done or on exception.
     */
    @Override
    public void run() {
        theLogger.log(Level.INFO, "LocationController run start.");
        threadExecutor = Executors.newFixedThreadPool(thePoolSize);
        ConstructionWorker constructionWorker = new ConstructionWorker(this, theURL, theLogger);
        Future<String> constructorResult = threadExecutor.submit(constructionWorker);

        try {
            String constructorComplete = constructorResult.get();
            int workersSize = theWorkers.size();
            theLogger.log(Level.INFO, "Controller - construction complete workers no = {0}", workersSize);
            int prevPctProgress = 0;

            if (constructorComplete.equals("Complete")) {
                if (!isHalted()) {
                    for (int j = 0; j < workersSize; ++j) {
                        try {
                            theLogger.log(Level.INFO, "Controller - waiting for worker no = {0}", j);
                            ScottishCastlePlacemark theTaskResult = theWorkers.get(j).get();

                            if (theTaskResult != null) {
                                theCastles.addCastle(theTaskResult.getId(), theTaskResult);
                            }

                            int pctProgress = ((j + 1) * 100) / workersSize;
                            if ((pctProgress - prevPctProgress) >= 1) {
                                theProgressDisplay.setProgress(pctProgress);
                                prevPctProgress = pctProgress;
                            }
                        } catch (ExecutionException ex) {
                            theLogger.log(Level.SEVERE, "LocationController get results Execution exception on:" + j, ex);
                        } catch (NullPointerException npe) {
                            theLogger.log(Level.SEVERE, "LocationController get results NullPointerExecution exception on:" + j, npe);
                        }
                    }
                }
            }
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "LocationController run Interrupted exception", e);
        }

        theLogger.log(Level.INFO, "LocationController has no work.");
        threadExecutor.shutdownNow();
        threadExecutor = null;
        manager.notifyComplete();
    }

    /**
     * Called to add an item to the controller work queue.
     * Does nothing if the Controller has been halted.
     *
     * @param theWorkItem 
     */
    synchronized public void addWorkload(ScottishCastlePlacemark theWorkItem) {
        if (isHalted()) {
            return;
        }
        LocationWorker theLocation = new LocationWorker(this, theWorkItem, theLogger);
        Future<ScottishCastlePlacemark> locatorResult = threadExecutor.submit(theLocation);
        theWorkers.add(locatorResult);
    }

    /**
     * Called to cause the LocationController to halt. The LocationController 
     * will not halt immediately. Once the LocationController is halted the 
     * run method will return.
     */
    synchronized public void halt() {
        halted = true;
    }

    /**
     * Determines if the LocationController has been halted.
     *
     * @return Returns true if the LocationController has been halted.
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Outputs results.
     *
     * @param outputDir 
     */
    public void outputResults(String outputDir) {
        theCastles.outputAsKML(outputDir);
    }
}
