package com.example.android.quakereport;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {

    }

    public static ArrayList<Earthquake> fetchEarthquakeData (String reuestUrl) {
        URL url = createUrl(reuestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHTTPrequest(url);
        } catch (IOException e) {
            Log.e("","Failed to make HTTP request");
        }

        ArrayList<Earthquake> earthquakes = extractFeaturesFromJson(jsonResponse);

        return earthquakes;
    }


    private static URL createUrl(String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e("","Invalid Url");
        }
        return url;
    }

    private static String makeHTTPrequest(URL url) throws IOException {
        String jsonResponse = null;
        if(url == null){
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode()==200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputSteam(inputStream);
            }
            else {
                Log.e("","Invalid Request");
            }

        } catch (IOException e) {
            Log.e("","Problem retrieving the earthquake JSON results.",e);
        } finally {
            if (urlConnection==null){
                urlConnection.disconnect();
            }
            if(inputStream==null) {
                inputStream.close();
            }
        }


        return jsonResponse;
    }

    private static String readFromInputSteam(InputStream inputStream) throws IOException {

        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }



    public static ArrayList<Earthquake> extractFeaturesFromJson(String earthquakeJson) {
        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Earthquake> earthquakes = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // TODO: Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of Earthquake objects with the corresponding data.

            JSONObject baseJsonResponse = new JSONObject(earthquakeJson);
            JSONArray features = baseJsonResponse.getJSONArray("features");

            for (int i=0;i<features.length();i++) {
                JSONObject currentEarthquake = features.getJSONObject(i);
                JSONObject properties = currentEarthquake.getJSONObject("properties");
                double magnitude = properties.getDouble("mag");
                String location = properties.getString("place");
                long timeInMilliSecond = properties.getLong("time");
                String url = properties.getString("url");

                Date dateObject = new Date(timeInMilliSecond);
                SimpleDateFormat dateFormat = new SimpleDateFormat("LLL DD,YYYY");
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                String formattedDate = dateFormat.format(dateObject);
                String formattedTime = timeFormat.format(dateObject);

                DecimalFormat formatMagnitude = new DecimalFormat("0.0");
                String formattedMagnitude = formatMagnitude.format(magnitude);

                Earthquake earthquake = new Earthquake(formattedMagnitude,location,formattedDate,formattedTime,url);
                earthquakes.add(earthquake);
            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

}