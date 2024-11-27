package com.example.mymicapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;

public class RobotAPIHelper {

    private static final String BASE_URL = "http://192.168.57.156:8080/"; // Replace with your robot's IP

    private static boolean sendRequestWithRetry(String endpoint, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                Log.d("RobotAPIHelper", "Attempt " + (attempt + 1) + " - Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true; // Success
                }
            } catch (Exception e) {
                Log.e("RobotAPIHelper", "Error in sendRequestWithRetry: ", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            attempt++;
        }
        return false; // Failed after retries
    }

    // Starts the speech recognition
    public static void startSpeechRecognition() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                boolean success = sendRequestWithRetry("speech/startRecognition", 3); // Retry up to 3 times
                if (success) {
                    Log.d("RobotAPIHelper", "Speech recognition started successfully.");
                } else {
                    Log.e("RobotAPIHelper", "Failed to start speech recognition after retries.");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Log.d("RobotAPIHelper", "Async task for starting speech recognition completed.");
            }
        }.execute();
    }

    // Stops the speech recognition
    public static void stopSpeechRecognition() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d("RobotAPIHelper", "Stopping speech recognition...");
                boolean success = sendRequest("speech/stopRecognition");
                if (success) {
                    Log.d("RobotAPIHelper", "Speech recognition stopped successfully.");
                } else {
                    Log.e("RobotAPIHelper", "Failed to stop speech recognition.");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Log.d("RobotAPIHelper", "Async task for stopping speech recognition completed.");
            }
        }.execute();
    }

    // Fetches the speech result
    public static String fetchSpeechResult() {
        HttpURLConnection connection = null;
        try {
            Log.d("RobotAPIHelper", "Fetching speech result...");
            URL url = new URL(BASE_URL + "speech/result");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000); // 5 seconds timeout

            int responseCode = connection.getResponseCode();
            Log.d("RobotAPIHelper", "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d("RobotAPIHelper", "Speech result: " + response.toString());
                return response.toString(); // Return the response as a string
            } else {
                Log.e("RobotAPIHelper", "Error: Server returned response code " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("RobotAPIHelper", "Error in fetchSpeechResult: ", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // General method to send GET requests to the robot
    private static boolean sendRequest(String endpoint) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000); // 5 seconds timeout

            int responseCode = connection.getResponseCode();
            Log.d("RobotAPIHelper", "Request to " + endpoint + " - Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d("RobotAPIHelper", "Response from " + endpoint + ": " + response.toString());
                return true; // Success
            } else {
                Log.e("RobotAPIHelper", "Error: Server returned response code " + responseCode + " for endpoint " + endpoint);
                return false; // Failure
            }
        } catch (Exception e) {
            Log.e("RobotAPIHelper", "Error in sendRequest: ", e);
            return false; // Failure
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
