package CastleScraper.data;

import java.net.URL;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Model of wikipedia page
 * @author al
 */
public class WikipediaDetailPage {

    private final URL theURL;
    private final Document theDocument;
    private final Logger theLogger;
    private static String theBaseURL = "http://en.wikipedia.org";

    /**
     * Constructs model of wikipedia page.
     * @param newURL 
     * @param logger  
     */
    public WikipediaDetailPage(URL newURL,
            Logger logger) {
        theURL = newURL;
        theLogger = logger;
        HTMLPageParser theParser = new HTMLPageParser(theLogger);
        theDocument = theParser.getParsedPage(theURL);
    }

    /**
     * Finds the period from the page.
     * @return -valid period or null if unobtainable
     */
    public static String getBaseURL() {
        return theBaseURL;
    }

    /**
     * Finds the period from the page.
     * @return -valid period or null if unobtainable
     */
    public URL getURL() {
        return theURL;
    }

    /**
     * 
     * @param theLogger
     * @return
     */
    public Position getPosition(Logger theLogger) {
        Position retVal = null;
        String theLatitude = "";
        String theLongitude = "";

        try {
            XPath lonXpath = XPathFactory.newInstance().newXPath();
            NodeList lonNodeList = (NodeList) lonXpath.evaluate("html//span[@class='geo']", theDocument, XPathConstants.NODESET);
            int lonLength = lonNodeList.getLength();
            if (lonLength > 0) {
                Element element = (Element) lonNodeList.item(0);
                String[] strArr = element.getTextContent().split(";");

                if (strArr.length > 1) {
                    theLatitude = strArr[0];
                    theLongitude = strArr[1];
                }
            }
        } catch (XPathExpressionException theException) {
            //Log.logException("Placemark populateLatLongFromURL:" + getId(), theException);
        }

        if (!(theLatitude.isEmpty()
                || theLongitude.isEmpty())) {
            retVal = new Position(theLatitude, theLongitude);
        }

        return retVal;
    }

    /**
     * Try and get the latitude and longitude from either the location ref
     * e.g. the location ref NJ540671
     * 
     * @param theLogger 
     * @return a valid position or null
     */
    public Position getPositionFromLocationRef(Logger theLogger) {
        Position retVal = null;
        String theLatitude = "";
        String theLongitude = "";

        try {
            XPath lonXpath = XPathFactory.newInstance().newXPath();
            NodeList lonNodeList = (NodeList) lonXpath.evaluate("html//span[@class='longitude']", theDocument, XPathConstants.NODESET);
            int lonLength = lonNodeList.getLength();
            if (lonLength > 0) {
                Element element = (Element) lonNodeList.item(0);
                theLongitude = element.getTextContent();
            }

            XPath latXpath = XPathFactory.newInstance().newXPath();
            NodeList latNodeList = (NodeList) latXpath.evaluate("html//span[@class='latitude']", theDocument, XPathConstants.NODESET);
            int latLength = latNodeList.getLength();
            if (latLength > 0) {
                Element element = (Element) latNodeList.item(0);
                theLatitude = element.getTextContent();
            }
        } catch (XPathExpressionException theException) {
            // failure handled by returning null 
            //- no exception processng required
        }

        if (!(theLatitude.isEmpty()
                || theLongitude.isEmpty())) {
            retVal = new Position(theLatitude, theLongitude);
        }

        return retVal;
    }
}
