package CastleScraper.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data class for collection of castles.
 * @author al
 */
public class ScottishCastles {

    private SortedMap<String, ScottishCastlePlacemark> theCastles = new TreeMap<String, ScottishCastlePlacemark>();
    private final Logger theLogger;
 
    /**
     * 
     * @param theLogger
     */    
    public ScottishCastles(Logger theLogger) {
        this.theLogger = theLogger;
    }
    
    /**
     * 
     * @param castleId
     * @param theCastle
     */
    public void addCastle(String castleId, ScottishCastlePlacemark theCastle){
        theCastles.put(castleId, theCastle);
    }
    
     /**
     * 
     * @param outputDir - the directory where the castles file will be written.
     */
    public void outputAsKML(String outputDir) {
        String strSave = outputDir + "/";
        String targetPath = strSave + "castles.kml";
        FileOutputStream fso = null;

        try {
            fso = new FileOutputStream(new File(targetPath));

            PrintStream ps = new PrintStream(fso);

            ps.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            ps.println();
            ps.print("<kml xmlns=\"http://earth.google.com/kml/2.1\">");
            ps.println();
            ps.print("<Document>");
            ps.println();

            ps.print("<Style id=\"highlightPlacemark\">");
            ps.println();
            ps.print("<IconStyle>");
            ps.println();
            ps.print("<Icon>");
            ps.println();
            ps.print("<href>http://maps.google.com/mapfiles/kml/paddle/red-stars.png</href>");
            ps.println();
            ps.print("</Icon>");
            ps.println();
            ps.print("</IconStyle>");
            ps.println();
            ps.print("</Style>");
            ps.println();
            ps.print("<Style id=\"normalPlacemark\">");
            ps.println();
            ps.print("<IconStyle>");
            ps.println();
            ps.print("<color>ffffffff</color>");
            ps.println();
            ps.print("<scale>0.8</scale>");
            ps.println();
            ps.print("<Icon>");
            ps.println();
            ps.print("<href>http://upload.wikimedia.org/wikipedia/commons/5/5e/Chess_rook_icon.png</href>");
            ps.println();
            ps.print("</Icon>");
            ps.println();
            ps.print("</IconStyle>");
            ps.println();
            ps.print("</Style>");
            ps.println();
            ps.print("<StyleMap id=\"exampleStyleMap\">");
            ps.println();
            ps.print("<Pair>");
            ps.println();
            ps.print("<key>normal</key>");
            ps.println();
            ps.print("<styleUrl>#normalPlacemark</styleUrl>");
            ps.println();
            ps.print("</Pair>");
            ps.println();
            ps.print("<Pair>");
            ps.println();
            ps.print("<key>highlight</key>");
            ps.println();
            ps.print("<styleUrl>#highlightPlacemark</styleUrl>");
            ps.println();
            ps.print("</Pair>");
            ps.println();
            ps.print("</StyleMap>");
            ps.println();

            Set<Map.Entry<String, ScottishCastlePlacemark>> castleValues = theCastles.entrySet();
            Iterator<Map.Entry<String, ScottishCastlePlacemark>> castleIterator = castleValues.iterator();

            String currentFolderName = "";

            while (castleIterator.hasNext()) {
                Map.Entry<String, ScottishCastlePlacemark> anEntry = castleIterator.next();
                String name = anEntry.getKey();
                ScottishCastlePlacemark tmpPlacemark = anEntry.getValue();
                String regionName = tmpPlacemark.theRegion;

                if (!(regionName.equals(currentFolderName))) {
                    if (!(currentFolderName.isEmpty())) {
                        ps.print("</Folder>");
                        ps.println();
                    }

                    ps.print("<Folder>");
                    ps.println();
                    ps.print("<name>" + regionName + "</name>");
                    ps.println();
                    ps.print("<description>" + regionName + "</description>");
                    ps.println();

                    currentFolderName = regionName;
                }
                tmpPlacemark.outputAsKML(ps);
            }

            ps.print("</Folder>");
            ps.println();

            ps.print("</Document>");
            ps.println();
            ps.print("</kml>");
            ps.println();
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "Error: ", e);
        } finally {
            try {
                if (fso != null) {
                    fso.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
