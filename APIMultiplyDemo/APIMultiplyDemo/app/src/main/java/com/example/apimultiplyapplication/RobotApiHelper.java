package com.example.apimultiplyapplication;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RobotApiHelper {
    private static final String BASE_URL = "http://192.168.1.100:8080/"; // Update with actual endpoint

    public static void startSpeechRecognition() {
        sendRequest("speech/startRecognition"); // Replace with actual endpoint
        Log.d("RobotApiHelper", "Speech recognition started.");
    }

    public static void stopSpeechRecognition() {
        sendRequest("speech/stopRecognition"); // Replace with actual endpoint
        Log.d("RobotApiHelper", "Speech recognition stopped.");
    }

    public static String fetchSpeechResult() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "speech/result"); // Replace with actual endpoint
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            Log.d("RobotApiHelper", "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d("RobotApiHelper", "Response: " + response.toString());
                return response.toString(); // Return the response as a string
            } else {
                Log.e("RobotApiHelper", "Error: Server returned response code " + responseCode);
                return null; // Return null if the server response is not OK
            }
        } catch (Exception e) {
            Log.e("RobotApiHelper", "Error in fetchSpeechResult: ", e);
            return null; // Return null if there is an exception
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private static void sendRequest(String endpoint) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            Log.d("RobotApiHelper", "Response Code: " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.d("RobotApiHelper", "Response: " + response.toString());
            // Add logic here to process the API response if needed

        } catch (Exception e) {
            Log.e("RobotApiHelper", "Error in sendRequest: ", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
