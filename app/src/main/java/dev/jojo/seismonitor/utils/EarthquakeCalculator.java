package dev.jojo.seismonitor.utils;

/**
 * Created by myxroft on 26/01/2018.
 */

public class EarthquakeCalculator {

    private Double MAGNITUDE;
    private Double DISTANCE;

    public static final String INTENSITY_STRONGLY = "Strongly";
    public static final String INTENSITY_MODERATELY = "Moderately";
    public static final String INTENSITY_LIGHTLY = "Lightly";
    public static final String INTENSITY_UNAFFECTED = "Unaffected";

    public EarthquakeCalculator(Double magnitude, Double distance){
        this.MAGNITUDE = magnitude;
        this.DISTANCE = distance;
    }

    public EarthquakeCalculator(){
        this.MAGNITUDE = 0d;
        this.DISTANCE = 0d;
    }

    public void setMagnitude(Double mag){
        this.MAGNITUDE = mag;
    }

    public void setDistance(Double dist){
        this.DISTANCE = dist;
    }

    public boolean isInRangeOf(Double val,Double min, Double max){
        return (val >= min) && (val <= max);
    }

    public Double getETA(){
        return this.DISTANCE / 3.0;
    }

    public String getIntensity(){

        //Check magnitude
        if(isInRangeOf(this.MAGNITUDE,6.8d,11.0d)){

            if(isInRangeOf(this.DISTANCE,0.1d,20.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,21.0d,40.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,41.0d,60.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else if(isInRangeOf(this.MAGNITUDE,6.0d,6.7d)){

            if(isInRangeOf(this.DISTANCE,0.1d,17.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,18.0d,37.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,38.0d,57.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else if(isInRangeOf(this.MAGNITUDE,5.0d,5.9d)){

            if(isInRangeOf(this.DISTANCE,0.1d,14.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,15.0d,32.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,33.0d,50.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else if(isInRangeOf(this.MAGNITUDE, 4.5d, 4.9d)){

            if(isInRangeOf(this.DISTANCE,0.1d,10.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,11.0d,18.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,19.0d,26.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else if(isInRangeOf(this.MAGNITUDE, 4.0d, 4.4d)){

            if(isInRangeOf(this.DISTANCE,0.1d,8.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,9.0d,15.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,16.0d,21.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else if(isInRangeOf(this.MAGNITUDE, 3.7d, 3.9d)){

            if(isInRangeOf(this.DISTANCE,0.1d,5.0d)){
                return INTENSITY_STRONGLY;
            }
            else if(isInRangeOf(this.DISTANCE,6.0d,11.0d)){
                return INTENSITY_MODERATELY;
            }
            else if(isInRangeOf(this.DISTANCE,12.0d,15.0d)){
                return INTENSITY_LIGHTLY;
            }
            else{
                return INTENSITY_UNAFFECTED;
            }
        }
        else{
            return INTENSITY_UNAFFECTED;
        }
    }
}
