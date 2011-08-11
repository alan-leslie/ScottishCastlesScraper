package CastleScraper.ui;

import javax.swing.JProgressBar;

/**
 * Implementation of JProgressDisplay
 * Only implements the progress bar functions. 
 * Text functions are ignored.
 * @author al
 */
public class JProgressBarWrapper  implements ScrapeProgressDisplay {
    private final JProgressBar theProgressBar;

    JProgressBarWrapper(JProgressBar newProgressBar){
        theProgressBar = newProgressBar;
    }

    public String getText() {
        return "not implemented";
    }
    
    public void setText(String theText) {
        // not implemented
    }

    public void setProgress(int i) {
        Integer thePercentage = new Integer(i);
        theProgressBar.setValue(thePercentage);
    }
    
}
