package dev.jojo.seismonitor.utils;

/**
 * Created by myxroft on 28/01/2018.
 */

public class NumParser {

    /**
     * I know I'm being lazy here but...
     * @param toParse
     * @returns 0 when error is encountered
     */
    public static Double parseDouble(String toParse){
        try{
            return Double.parseDouble(toParse);
        }
        catch(NumberFormatException numEx){
            return 0d;
        }
    }
}
