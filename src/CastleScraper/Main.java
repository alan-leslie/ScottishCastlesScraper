package CastleScraper;

import CastleScraper.ui.ScottishCastlesScraper;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author al
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    // TODO -
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        FileInputStream is = null;

        try {
            is = new FileInputStream("CastleScraper.properties");
            properties.load(is);
        } catch (IOException e) {
            // ...
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    /* .... */
                }
            }
        }

        String poolSizeStr = properties.getProperty("PoolSize", "2");
        int poolSize = Integer.parseInt(poolSizeStr);
        String theURL = properties.getProperty("StartURL", "http://en.wikipedia.org/wiki/Scottish_castles");
        String theOutputDir = properties.getProperty("OutputDir", ".");

        Logger theLogger = Main.makeLogger();
        ScottishCastlesScraper theUI = new ScottishCastlesScraper();
        LocationController theController = new LocationController(theURL,
                poolSize, theLogger);

        theUI.setOutputDir(theOutputDir);
        theUI.setController(theController);
        theController.setManager(theUI);
        theController.setProgressDisplay(theUI.getProgressDisplay());

        theUI.setVisible(true);
    }

    /**
     *
     * @return - valid logger (single file).
     */
    private static Logger makeLogger() {
        Logger lgr = Logger.getLogger("CastleScraper");
        lgr.setUseParentHandlers(false);
        lgr.addHandler(simpleFileHandler());
        return lgr;
    }

    /**
     *
     * @return - valid file handler for logger.
     */
    private static FileHandler simpleFileHandler() {
        try {
            FileHandler hdlr = new FileHandler("CastleScraper.log");
            hdlr.setFormatter(new SimpleFormatter());
            return hdlr;
        } catch (Exception e) {
            System.out.println("Failed to create log file");
            return null;
        }
    }
}
