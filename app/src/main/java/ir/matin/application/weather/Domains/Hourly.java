package ir.matin.application.weather.Domains;

public class Hourly {
    private String hour ;
    private int temperature ;
    private String picPath ;

    public Hourly(String hour, int temperature, String picPath) {
        this.hour = hour;
        this.temperature = temperature;
        this.picPath = picPath;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
