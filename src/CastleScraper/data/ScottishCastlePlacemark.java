package CastleScraper.data;

import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Class to hold information for placing a castle
 * @author al
 */
public class ScottishCastlePlacemark implements Comparable {

    String theName;
    String theRegion;
    private final String theNotes;
    private final String theType;
    private String theLatitude;
    private String theLongitude;
    private URL theLocation;
    private final String theCondition;
    private final String theDate;
    private URL theURL;
    private String theImageSource;
    private String theImageHREF;
    private final Logger theLogger;

    /**
     * 
     * @param newName 
     * @param newRegion 
     * @param newNotes 
     * @param newType 
     * @param newLocation 
     * @param newCondition 
     * @param newDate 
     * @param newURL 
     * @param newImage 
     * @param logger 
     */
    public ScottishCastlePlacemark(String newName,
            String newRegion,
            String newNotes,
            String newType,
            URL newLocation,
            String newCondition,
            String newDate,
            URL newURL,
            String newImageSource,
            String newImageHREF,
            Logger logger) {
        theName = newName;
        theRegion = newRegion;
        theNotes = newNotes;
        theType = newType;
        theLocation = newLocation;
        theCondition = newCondition;
        theDate = newDate;
        theURL = newURL;
        theImageSource = newImageSource;
        theImageHREF = newImageHREF;
        theLogger = logger;
    }

    /**
     * a copy constructor so that the object is not shred in threads
     * @param theOther 
     */
    public ScottishCastlePlacemark(ScottishCastlePlacemark theOther) {
        theName = theOther.theName;
        theRegion = theOther.theRegion;
        theNotes = theOther.theNotes;
        theType = theOther.theType;
        theLatitude = theOther.theLatitude;
        theLongitude = theOther.theLongitude;
        theLocation = theOther.theLocation;
        theCondition = theOther.theCondition;
        theDate = theOther.theDate;
        theURL = theOther.theURL;
        theImageSource = theOther.theImageSource;
        theImageHREF = theOther.theImageHREF;
        theLogger = theOther.theLogger;
    }

    /**
     * 
     * @param ps - the stream to where the data is written
     */
    public void outputAsKML(PrintStream ps) {
        ps.print("<Placemark>");
        ps.println();
        ps.print("<name>");
        ps.print(theName);
        ps.print("</name>");
        ps.println();
        ps.print("<description>");
        ps.println();
        ps.print("&lt;p&gt;Condition: ");
        ps.print(theCondition);
        ps.print("&lt;/p&gt;");
        ps.println();
        ps.print("&lt;p&gt;Date: ");
        ps.print(theDate);
        ps.print("&lt;/p&gt;");
        ps.println();

        if (theURL != null) {
            ps.print("&lt;p&gt;");
            ps.println();
            ps.print("&lt;a href=\"");
            ps.print(theURL);
            ps.print("\"&gt; more info&gt;&gt;&gt;");
            ps.print("&lt;/a&gt;");
            ps.println();
            ps.print("&lt;/p&gt;");
            ps.println();
        }

        if (!(theImageHREF.isEmpty() || theImageSource.isEmpty())) {
            ps.print("&lt;p&gt;");
            ps.println();
            ps.print("&lt;p&gt;");
            ps.println();
            ps.print("&lt;a href=\"");
            ps.print(theImageHREF);
            ps.print("\"&gt;");
            ps.print("&lt;img src=\"");
            ps.print(theImageSource);
            ps.print("\"/&gt;");
            ps.print("&lt;/a&gt;");
            ps.println();
            ps.print("&lt;/p&gt;");
            ps.println();
            ps.print("&lt;/p&gt;");
            ps.println();
        }
        
        ps.print("</description>");
        ps.println();
        ps.print("<styleUrl>#exampleStyleMap</styleUrl>");
        ps.println();
        ps.print("<Point>");
        ps.println();
        ps.print("<coordinates>");
        ps.print(theLongitude);
        ps.print(",");
        ps.print(theLatitude);
        ps.print("</coordinates>");
        ps.println();
        ps.print("</Point>");
        ps.println();
        ps.print("</Placemark>");
        ps.println();
    }

    /**
     * 
     * @return - a unique id for the placemark
     */
    public String getId() {
        String tmpId = theRegion + "-" + theName;
        String strId = tmpId.replaceAll(" ", "");

        return strId;
    }

    /*
     * @precon - theURL is valid
     * @postcon - lat long set 
     * @return
     */
    private boolean populateLatLongFromURL() {
        if (theURL == null) {
            return false;
        }

        WikipediaDetailPage thePage = new WikipediaDetailPage(theURL, theLogger);
        Position thePosition = thePage.getPosition(theLogger);

        if (thePosition != null) {
            theLatitude = thePosition.getLatitude();
            theLongitude = thePosition.getLongitude();
        }

        if (theLatitude == null
                || theLongitude == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Try and get the latitude and longitude from either the location or
     * the underlying URL
     * @return
     */
    public boolean populateLatLong() {
        if (theLocation == null) {
            return populateLatLongFromURL();
        }

        WikipediaDetailPage thePage = new WikipediaDetailPage(theLocation, theLogger);
        Position thePosition = thePage.getPositionFromLocationRef(theLogger);

        if (thePosition != null) {
            theLongitude = thePosition.getLongitude();
            theLatitude = thePosition.getLatitude();
        }

        if (theLatitude == null
                || theLongitude == null) {
            return false;
        } else {
            return true;
        }
    }

    public int compareTo(Object anotherPlacemark) throws ClassCastException {
        if (!(anotherPlacemark instanceof ScottishCastlePlacemark)) {
            throw new ClassCastException("A Placemark object expected.");
        }
        String anotherPlacemarkName = ((ScottishCastlePlacemark) anotherPlacemark).getId();
        return this.getId().compareTo(anotherPlacemarkName);
    }
}
