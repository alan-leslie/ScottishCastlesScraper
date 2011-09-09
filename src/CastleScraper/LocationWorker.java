package CastleScraper;

import CastleScraper.data.ScottishCastlePlacemark;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LocationWorker class performs the actual work of
 * looking at sub pages to find locations.  
 *
 * @author al
 */
public class LocationWorker implements Callable<ScottishCastlePlacemark> {

   /**
     * The controller which drives this worker.
     */
    private final LocationController owner;
    
    /**
     * The data used to find the location.
     */
    private final ScottishCastlePlacemark _placemark;
    
    private final Logger theLogger;

    /**
     * Constructs a worker object.
     *
     * @param owner The owner of this object.
     * @param thePlacemark
     * @param logger  
     */
    public LocationWorker(LocationController owner,
            ScottishCastlePlacemark thePlacemark,
            Logger logger) {
        this.owner = owner;
        this._placemark = thePlacemark;
        theLogger = logger;
    }

    /**
     * The call method - copy placemark to result to ensure original is not
     * stuck in other threads.
     * @return - a fully populated placemark if successful otherwise null
     */
    public ScottishCastlePlacemark call() {
        theLogger.log(Level.FINEST, "LocationWorker call - Getting location for: {0}", _placemark.getId());
        ScottishCastlePlacemark theResult = new ScottishCastlePlacemark(_placemark);
        boolean isError = !(theResult.populateLatLong());

        if (isError) {
            theLogger.log(Level.INFO, "LocationWorker call unsuccessful for: {0}", _placemark.getId());
            return null;
        } else {
            theLogger.log(Level.INFO, "LocationWorker call successful for {0}", _placemark.getId()");
            return theResult;
        }
    }
}