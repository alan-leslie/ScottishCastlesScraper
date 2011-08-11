package CastleScraper.ui;

import CastleScraper.LocationController;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author al
 */
public class ScottishCastlesScraper extends javax.swing.JFrame implements ITaskComplete {

    /**
     * The underlying spider object.
     */
    LocationController theController = null;
    
    /**
     * The directory where the output file will be placed.
     */
    String outputDir;

    /**
     * The constructor. Set up the visual Swing
     * components that make up the user interface
     * for this program.
     */
    public ScottishCastlesScraper() {
        setTitle("Download Site");
        getContentPane().setLayout(null);
        setSize(234, 140);
        setVisible(false);
        
        theProgressBar.setValue(0);
        getContentPane().add(theProgressBar);
        theProgressBar.setBounds(12, 10, 210, 24);
        
        getContentPane().add(textStatusDisplay);
        textStatusDisplay.setBounds(12, 44, 210, 24);
       
        goButton.setText("GO!");
        getContentPane().add(goButton);
        goButton.setBounds(12, 78, 210, 24);
        
        goButton.setActionCommand("jbutton");
        GoListener theListener = new GoListener();
        goButton.addActionListener(theListener);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(32, 32);
    }

    /**
     *
     * @param b
     */
    @Override
    public void setVisible(boolean b) {
        if (b) {
            setLocation(50, 50);
        }
        super.setVisible(b);
    }

    JButton goButton = new JButton();
    JLabel textStatusDisplay = new JLabel();
    JProgressBar theProgressBar = new JProgressBar();
     
    /**
     * 
     * @return
     */
    public ScrapeProgressDisplay getProgressDisplay(){
        return new JProgressBarWrapper(theProgressBar);
    }

    /**
     * 
     * @param controller
     */
    public void setController(LocationController controller) {
        theController = controller;
    }

    /**
     * 
     * @param theOutputDir
     */
    public void setOutputDir(String theOutputDir) {
        outputDir = theOutputDir;
    }

    /**
     * An event handler for the go button.
     *
     */
    class GoListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == goButton) {
                Go_actionPerformed(event);
            }
        }
    }

    /**
     * Called to start processing (or to cancel it) when the go button 
     * is pressed.
     *
     * @param event The event - not used
     */
    void Go_actionPerformed(java.awt.event.ActionEvent event) {
        if (theController.isAlive()) {

            Runnable doLater = new Runnable() {
                public void run() {
                    goButton.setText("Canceling...");
                }
            };
            
            SwingUtilities.invokeLater(doLater);

            theController.halt();
            return;
        }

        Runnable doLater = new Runnable() {
            public void run() {
                goButton.setText("Cancel");
                textStatusDisplay.setText("Loading....");
            }
        };
        
        SwingUtilities.invokeLater(doLater);

        theController.start();
    }

    /**
     * Callback method to indicate the controller is complete.
     */
    public void notifyComplete() {
        if (theController.isHalted()) {
            JOptionPane.showMessageDialog(this,
                    "Download of site has been cancelled. " + 
                    "Check log file for any errors.",
                    "Done",
                    JOptionPane.OK_CANCEL_OPTION,
                    null);
        } else {
            theController.outputResults(outputDir);

            JOptionPane.showMessageDialog(this,
                    "Download of site is complete. " +
                    "Check log file for any errors.",
                    "Done",
                    JOptionPane.OK_CANCEL_OPTION,
                    null);
        }

        Runnable doLater = new Runnable() {
            public void run() {
                goButton.setText("GO!!");
                textStatusDisplay.setText("");
                theProgressBar.setValue(0);
            }
        };
        
        SwingUtilities.invokeLater(doLater);
    }
}