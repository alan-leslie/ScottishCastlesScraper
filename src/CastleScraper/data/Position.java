package CastleScraper.data;

/**
 * Class to model position (latitude and longitude)
 * @author al
 */
public class Position {
    private String theLatitude;
    private String theLongitude;
    
    /**
     * 
     * @param latitude - dd format
     * @param longitude - dd format 
     */
    public Position(String latitude, 
            String longitude){
        theLatitude = latitude;
        theLongitude = longitude;        
    }
    
    /**
     * 
     * @return - latitude in dd format
     */
    public String getLatitude(){
        return theLatitude;
    }
    
    /**
     * 
     * @return - longitude in dd format
     */
    public String getLongitude(){
        return theLongitude;
    }   
    
    /**
     * 
     * @return -whether the position data is complete and valid
     */
    public boolean isComplete(){
        boolean latComplete = (theLatitude != null && !theLatitude.isEmpty());
        boolean lonComplete = (theLongitude != null && !theLongitude.isEmpty());
        
        return (latComplete && lonComplete);
    }
}
