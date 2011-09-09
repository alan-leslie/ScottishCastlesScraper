package CastleScraper.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Model of wikipedia page
 * @author al
 */
public class WikipediaListPage {

    private final URL theURL;
    private final Document theDocument;
    private final Logger theLogger;
    private static String theBaseURL = "http://en.wikipedia.org";

    /**
     * Constructs model of wikipedia page.
     * @param newURL 
     * @param logger  
     */
    public WikipediaListPage(URL newURL,
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
     * Finds the region names by looking up the section headings (h2).
     * @return - regions names as a list
     */
    private List<String> getRegionNames() {
        List<String> theRegions = new ArrayList<String>();
        theLogger.log(Level.INFO, "Construction worker - getting region names");

        try {
            XPath editSectionXpath = XPathFactory.newInstance().newXPath();
            NodeList editSectionNodeList = (NodeList) editSectionXpath.evaluate("/html//span[@class='editsection']", theDocument, XPathConstants.NODESET);
            int editSectionLength = editSectionNodeList.getLength();

            for (int j = 0; j < editSectionLength; j++) {
                Element show = (Element) editSectionNodeList.item(j);
                NodeList rowNodeList = show.getChildNodes();
                int sectionLength = rowNodeList.getLength();

                for (int z = 0; z < sectionLength; ++z) {
                    Node childNode = (Node) rowNodeList.item(z);

                    if (childNode.getNodeName().equals("a")) {
                        NamedNodeMap theMap = childNode.getAttributes();
                        int aLength = theMap.getLength();

                        if (aLength > 1) {
                            Node titleNode = (Node) theMap.getNamedItem("title");
                            String sectionTitle = (String) titleNode.getNodeValue();
                            String regionTitle = sectionTitle.substring(13).trim();
                            theRegions.add(regionTitle);
                        }
                    }
                }
            }
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
        }

        theLogger.log(Level.INFO, "Construction worker - getting region names - complete");
        return theRegions;
    }

    /**
     * Finds the tables in the list page.
     * Tables should correspond to regions.
     * @return - the list of tables as a node list 
     */
    private NodeList getTables() {
        NodeList tableNodeList = null;

        try {
            String searchString = "/html//table[@class='wikitable sortable']";
            XPath tableXpath = XPathFactory.newInstance().newXPath();
            tableNodeList = (NodeList) tableXpath.evaluate(searchString, theDocument, XPathConstants.NODESET);
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
        }

        return tableNodeList;
    }
    
   /**
     * Finds the main place URL from the table row.
     * @param rowEntry - the table row to be searched
     * @param theLogger 
     * @return - the place URL (e.g. Glasgow) 
     */
    private URL getMainURLFromRow(Element rowEntry,
            Logger theLogger) {
        URL theRowURL = null;

        try {
            XPath anchorXpath = XPathFactory.newInstance().newXPath();
            NodeList anchorNodeList = (NodeList) anchorXpath.evaluate("./a", rowEntry, XPathConstants.NODESET);

            if (anchorNodeList.getLength() > 0) {
                Element anchorElement = (Element) anchorNodeList.item(0);
                String theElementHREF = anchorElement.getAttribute("href");

                if (theElementHREF.indexOf("wikipedia") == -1) {
                    theElementHREF = theBaseURL + theElementHREF;
                }

                if (theElementHREF.indexOf("action=edit") != -1) {
                    theElementHREF = "";
                }

                try {
                    theRowURL = new URL(theElementHREF);
                } catch (MalformedURLException e) {
                    theRowURL = null;
                }
            }
        } catch (Exception e) {
            Throwable theCause = e.getCause();

            if (theCause instanceof TransformerException) {
                theLogger.log(Level.INFO, "XPath table not found.");
            } else {
                theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
            }
        }

        return theRowURL;
    }

    /**
     * Finds the URL where the location can be converted to lat/long.
     * Note: the structure changed from location ref being a child to being a 
     * grandchild.
     * if it changes again may need to re-jig this 
     * @param theLocationRowEntry - the element containing the href 
     * @return - the required URL.
     * @throws XPathExpressionException  
     */
    private String findLocationHRef(Element theLocationRowEntry) throws XPathExpressionException {
        String retVal = null;

        XPath locationAnchorXpath = XPathFactory.newInstance().newXPath();
        NodeList locationAnchorNodeList = (NodeList) locationAnchorXpath.evaluate("./span/a", theLocationRowEntry, XPathConstants.NODESET);

        if (locationAnchorNodeList.getLength() > 0) {
            for (int j = 0; j < locationAnchorNodeList.getLength(); ++j) {
                Element anchorElement = (Element) locationAnchorNodeList.item(j);
                String theElementHREF = anchorElement.getAttribute("href");

                if (theElementHREF.indexOf("rhaworth") != -1) {
                    retVal = theElementHREF;
                }
            }
        }

        return retVal;
    }
    
   /**
     * Finds the main place URL from the table row.
     * @return - the place URL (e.g. Glasgow) 
     */
    public List<ScottishCastlePlacemark> extractLinks() {
        List<ScottishCastlePlacemark> retVal = new ArrayList<ScottishCastlePlacemark>();
        List<String> regionNames = getRegionNames();
        NodeList castleTables = getTables();
        int castleTablesLength = castleTables.getLength();

        for (int i = 0; i < castleTablesLength; ++i) {
            String regionName = regionNames.get(i);
            theLogger.log(Level.INFO, "WikipediaListPage - processinng region : {0}", regionName);
            Node tableNode = castleTables.item(i);

            List<ScottishCastlePlacemark> thePlacemarks = extractLinksFromTable(tableNode, regionName);
            boolean addAll = retVal.addAll(thePlacemarks);

            if (!addAll) {
                theLogger.log(Level.WARNING, "Not All placemarks added");
            }
        }

        return retVal;
    }

    /**
     * Builds the placemarks from data in the table.
     * @param tableNode - the table which contains the required data 
     * @param regionName - the name of the region being processed
     * @return - whether the processing was successful
     */
    private List<ScottishCastlePlacemark> extractLinksFromTable(Node tableNode,
            String regionName) {
        List<ScottishCastlePlacemark> retVal = new ArrayList<ScottishCastlePlacemark>();

        try {
            String searchString = "./tbody/tr";
            XPath tableXpath = XPathFactory.newInstance().newXPath();
            NodeList tableNodeRowList = (NodeList) tableXpath.evaluate(searchString, tableNode, XPathConstants.NODESET);
            int tableLength = tableNodeRowList.getLength();

            for (int i = 0; i < tableLength; i++) {
                theLogger.log(Level.FINEST, "Construction worker - getting placemark");

                Element show = (Element) tableNodeRowList.item(i);
                String tagName = show.getTagName();
                String tagContent = show.getTextContent();

                XPath rowXpath = XPathFactory.newInstance().newXPath();
                NodeList rowNodeList = (NodeList) rowXpath.evaluate("./td", show, XPathConstants.NODESET);
                int rowLength = rowNodeList.getLength();

                if (rowLength > 7) {
                    ScottishCastlePlacemark thePlacemark = getPlacemarkData(rowNodeList, regionName);
                    retVal.add(thePlacemark);
                }
            }
        } catch (Exception e) {
            Throwable theCause = e.getCause();

            if (theCause instanceof TransformerException) {
                theLogger.log(Level.INFO, "XPath table not found.");
            } else {
                theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
            }
        }

        return retVal;
    }
    
   /**
     * Gets the placemsrk from the table row data.
     * @param rowNodeList - the table row to be searched
     * @param tregionName - the region that this placemark belongs to.
     * @return - the populated placemark or null if unobtainable. 
     */
    private ScottishCastlePlacemark getPlacemarkData(NodeList rowNodeList,
            String regionName) {
        ScottishCastlePlacemark thePlacemark = null;

        theLogger.log(Level.FINEST, "Construction worker - got table xpath");

        Element rowEntry = (Element) rowNodeList.item(0);
        URL theMainURL = getMainURLFromRow(rowEntry, theLogger);
        String theName = rowEntry.getTextContent().replace("&", "and");

        theLogger.log(Level.INFO, "Construction worker - got anchor node: {0}", theName);

        rowEntry = (Element) rowNodeList.item(1);
        String theType = rowEntry.getTextContent().replace("&", "and");
        rowEntry = (Element) rowNodeList.item(2);
        String theDate = rowEntry.getTextContent().replace("&", "and");
        rowEntry = (Element) rowNodeList.item(3);
        String theCondition = rowEntry.getTextContent().replace("&", "and");
        rowEntry = (Element) rowNodeList.item(6);
        String theNotes = rowEntry.getTextContent();
        rowEntry = (Element) rowNodeList.item(7);
        String theImage = rowEntry.getTextContent();
        
        String theImageSource = getImageSource(rowEntry);
        String theImageHREF = getImageHREF(rowEntry);

        URL theLocation = null;

        try {
            Element locationRowEntry = (Element) rowNodeList.item(5);
            String theLocationRefString = findLocationHRef(locationRowEntry);
            if (theLocationRefString != null) {
                try {
                    theLocation = new URL(theLocationRefString);
                } catch (MalformedURLException ex) {
                    theLogger.log(Level.SEVERE, "Unable to convert location ref", ex);
                }
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        thePlacemark = new ScottishCastlePlacemark(theName, regionName,
                theNotes, theType, theLocation, theCondition, theDate, theMainURL, 
                theImageSource, theImageHREF, theLogger);

        return thePlacemark;
    }
    
    private String getImageSource(Node imageRowNode) {
        String retVal = "";

        try {
            String searchString = ".//div[@class='thumbinner']//img[@class='thumbimage']";
            XPath imageXpath = XPathFactory.newInstance().newXPath();
            Node imageNode = (Node)imageXpath.evaluate(searchString, imageRowNode, XPathConstants.NODE);
            
            if(imageNode != null){
                Element imageElement = (Element) imageNode;
                retVal = imageElement.getAttribute("src");
            }
       } catch (Exception e) {
           theLogger.log(Level.INFO, "XPath image not found.");
       }

        return retVal;    
    }
    
    private String getImageHREF(Node imageRowNode) {
        String retVal = "";

        try {
            String searchString = ".//div[@class='thumbinner']//a[@class='image']";
            XPath imageXpath = XPathFactory.newInstance().newXPath();
            Node imageNode = (Node)imageXpath.evaluate(searchString, imageRowNode, XPathConstants.NODE);
            
            if(imageNode != null){
                Element imageAnchorElement = (Element) imageNode;
                String theHREF = imageAnchorElement.getAttribute("href");
                
                if(theHREF.indexOf("http") != 0){         
                    retVal = theBaseURL + theHREF;
                } else {
                    retVal = theHREF;
                }
            }
       } catch (Exception e) {
           theLogger.log(Level.INFO, "XPath image not found.");
       }

        return retVal;    
    }
}
