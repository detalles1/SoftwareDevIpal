package com.example.androidmultiplyapplication;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RobotApiHelper {

    private static final String BASE_URL = "http://192.168.1.100:8080/"; // Replace with the correct IP if needed

    public static void sendRequest(String endpoint) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check response code and log response
            int responseCode = connection.getResponseCode();
            Log.d("RobotApiHelper", "Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.d("RobotApiHelper", "Response: " + response.toString());

        } catch (Exception e) {
            Log.e("RobotApiHelper", "Error in sendRequest: ", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
