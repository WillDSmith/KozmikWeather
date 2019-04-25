package com.kozmiksoftware.kozmikweather.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by psychotic on 3/6/17.
 */

public class DailyWeatherReport {
    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR  = "Clear";
    public static final String WEATHER_TYPE_RAIN   = "Rain";
    public static final String WEATHER_TYPE_WIND   = "Wind";
    public static final String WEATHER_TYPE_SNOW   = "Snow";

    private String cityName;
    private int dayTemp;
    private int maxTemp;
    private int minTemp;
    private String weather;
    private String country;
    private String dateString;

    public DailyWeatherReport(String cityName, int dayTemp, int maxTemp, int minTemp, String weather, String country, String dateRaw) {
        this.cityName = cityName;
        this.dayTemp = dayTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = weather;
        this.country = country;
        this.dateString = formatDate(dateRaw);
    }

    private String formatDate(String dateRaw) {

        int convertedString = Integer.parseInt(dateRaw);

        int unixSeconds = convertedString;
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // the format of your date

        String actualDate = sdf.format(date);

        String convertedDate = newFormattedDate(actualDate);

        //System.out.println(actualDate);

        return convertedDate;
    }

    private String newFormattedDate( String actualDate)  {
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = sdf.parse(actualDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String month_name = month_date.format(date);

        return month_name;
    }

    public String getDateString() {
        return dateString;
    }

    public String getCityName() {
        return cityName;
    }

    public int getdayTemp() {
        return dayTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getCountry() {
        return country;
    }
}
