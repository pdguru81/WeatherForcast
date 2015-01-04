package com.example.weatherforcast.weatherforcast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.Date;
import java.util.IllegalFormatException;

/**
 * Created by ekwah on 12/27/14.
 */
public class WeatherDataParser {

    JSONObject weatherdata_json = null;
    public static JSONArray forecast_list = null;

    WeatherDataParser(String weatherJsonStr){
        try {
            weatherdata_json = new JSONObject(weatherJsonStr);
            forecast_list =  weatherdata_json.getJSONArray("list");
        }catch (JSONException e){
           throw new IllegalArgumentException("The JSon is impropery formatted");
        }

    }


    /**
     * This method returns the weather description,
     * day and temperatures predictions in celcious.
     * @param 'dayindex', is an int in range 0-6, representing the seven days.
     * @returns a String, with the weather description.
     *
     **/
    public  static  String getWeatherDescriptionForDay(int dayIndex){
        //create a Json object from the string
        try{
            // get the weather forecast at the specified index
            JSONObject weather_forecast = forecast_list.getJSONObject(dayIndex);
            // get the temperature object and extract temperature
            JSONObject temperature= weather_forecast.getJSONObject("temp");
            double day_temp= temperature.getDouble("day")-273.15;
            double night_temp=  temperature.getDouble("night")-273.15;
            double morn_temp=  temperature.getDouble("morn")-273.15;
            double eve_temp=  temperature.getDouble("eve")-273.15;
            double max = temperature.getDouble("max")-275.15;
            double min = temperature.getDouble("min")-275.15;
            JSONArray weather = weather_forecast.getJSONArray("weather");
            // get main and verbose descriptions
            String main = weather.getJSONObject(0).getString("main");
            String description = weather.getJSONObject(0).getString("description");
            //get date
            int time = weather_forecast.getInt("dt");
            Date date =   new Date(time * 1000);
            StringBuffer weather_info = new StringBuffer();
            weather_info.append(""+date.toString() +" -"+ main+" -"+Math.round(max)+"/"+Math.round(min));
            return weather_info.toString();
        }catch (JSONException e){
           System.out.println(e.getMessage());
        }
        return "";
    }
}

