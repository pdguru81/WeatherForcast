package com.example.weatherforcast.weatherforcast;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.net.Uri;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // add logic to open up Settings Activity
            Context context = getApplicationContext();
            Intent intent = new Intent(this,SettingsActivity.class);
            // start the intent
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ForecastFragment extends Fragment{


        private HttpFetchWeatherTask fetchweather = new MainActivity().new HttpFetchWeatherTask();

        public ForecastFragment() {

        }

        public double[] updateWeatherUnits(double hightemp, double lowtemp){
            double[] result = new double[2];
            // retrieve the preferred temeprature value
            SharedPreferences settings= PreferenceManager.
                    getDefaultSharedPreferences(getActivity());
            String value = getString(R.string.preffered_temperature_key);
            // modify after getting the value
            if(value.equals("fahreinheit")){
                //change from celcius to fahreinheit
                double high = 1.8* (hightemp+32.);
                double low =1.8* (lowtemp+32);
                result[0]= high;
                result[1]= low;
                return result;
            }else if(value.equals("celsius")){
                   result[0]= hightemp;
                   result[1]= lowtemp;
                    return result;
            }else{
                // this should not happen
                throw new IllegalArgumentException("No valid temperature Unit selected");
            }

        }

        public void updateWeather(){
            SharedPreferences settings= PreferenceManager.
                    getDefaultSharedPreferences(getActivity());
            String location = getString(R.string.preffered_location_key);

            System.out.println("The preference key is not undefined: "+location);
            String weather_location = settings.getString(location,"Boston");
            fetchweather.execute(weather_location.toString());
        }

        @Override
        public void onStart(){
            super.onStart();
            updateWeather();
        }
        //before view is created
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            //before the view is initailized, indicate that fragment should
            setHasOptionsMenu(true);

        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater Inflater){
            // inflate the menu with items
            Inflater.inflate(R.menu.forecastfragment,menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
                // check which item option was picked and trigger a call
            int id = item.getItemId();
            if (id==R.id.action_refresh){
                //return true;
                //execute the task to fetch weather.
                //fetchweather.execute();
                return true;

            }
            return super.onOptionsItemSelected(item);
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            // get all the needed fields and pass into arrayAdapater
            fetchweather.setView(rootView);
            return rootView;
        }
    }

    public interface datafromfetchWeatherData{
        void getWeatherdatafromtask(ArrayList<String> data);
    }

    /**
     * A class in a thread that sends the request to retrieve
     * information about Boston's weather from
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=Boston&cnt=7.
     */
    private class HttpFetchWeatherTask extends AsyncTask<String,Void,ArrayList<String>>{
        private ArrayList<String> weatherdescription;
        private View weatherview ;

        public void setView(View weatherview){
            this.weatherview = weatherview;
        }

        // send the request in a separate thread to prevent UI from freezing
        @Override
        protected ArrayList<String> doInBackground(String... location){
            //url from which to retrieve information about Boston weather forecast
            String weather_location = location[0];
            System.out.println("This is the weather location: "+ weather_location);
            String location_ = null;
            // build uri for query request
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            final String QUERY_PARAM = "q";
            final String DAYS_PARAM = "cnt";
            int numOfdays = 7;
            if (weather_location!=null){
                location_= weather_location;
            }else{
                location_= "Boston";
            }

            Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM,location_)
                    .appendQueryParameter(DAYS_PARAM,Integer.toString(numOfdays))
                    .build();
            String url = builder.toString();
            try{
                //from http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
                URL oracle = new URL(url);
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        yc.getInputStream()));
                StringBuffer queryresponse = new StringBuffer();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    queryresponse.append(inputLine +'\n');
                in.close();
                String queryResponse= queryresponse.toString();
                //create a Parser
                WeatherDataParser weatherparser = new WeatherDataParser(queryResponse);
                ArrayList<String> weather_descriptions = new ArrayList<String>();

                for (int i=0;i<7;i++){
                    String description = weatherparser.getWeatherDescriptionForDay(i);
                    if(description==""){
                        throw new IllegalArgumentException("Weather descriptions can't be empty");
                    }else{
                        weather_descriptions.add(description);
                    }
                }
                return weather_descriptions;
            }catch (Exception e){
                Log.e("MainActivity", e.getMessage(),e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> weather_descriptions){

            //get the response from the query
            if(!this.isCancelled()){
                if(weather_descriptions!=null){

                    //set the weather descriptions
                    this.weatherdescription = weather_descriptions;

                    //View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    final ArrayAdapter weatherdata = new ArrayAdapter<String>(

                           // get the parent, ie fragment activity
                            weatherview.getContext(),
                           //id if list item layout
                            R.layout.list_item_forecast,

                            //id of listview  to populate
                            R.id.list_item_forecast_textview,
                            weather_descriptions);
                    // find the list in the view
                    ListView listview = (ListView) weatherview.findViewById(R.id.listview_forecast);

                    //set a toast listerner for the list view
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                                Context itemcontext =weatherview.getContext();
                                CharSequence text = weather_descriptions.get(pos);

                            Intent intent = new Intent(weatherview.getContext(),DetailActivity.class);
                            //create an intent and launch a new page
                            //Intent intent = new Intent(itemcontext, DetailActivity.class);
                            intent.putExtra("weatherdata",text);
                            weatherview.getContext().startActivity(intent);
                        }
                    });
                    listview.setAdapter(weatherdata);


                }
            }

        }


    }
}


//http://api.openweathermap.org/data/2.5/forecast/daily?q=Boston&cnt=7