
package com.devostudios.brastoffyting;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class AuthClient {
    interface Callback {
        void onResult(boolean success, String message, Session session);
    }

    static final class Session {
        final String accessToken;
        final String refreshToken;
        Session(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    private AuthClient() {}

    static void signIn(String email, String password, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email.trim());
            body.put("password", password);
            request("/auth/v1/token?grant_type=password", body, null, callback);
        } catch (Exception e) {
            callback.onResult(false, "Could not prepare sign-in request.", null);
        }
    }

    static void signUp(String email, String password, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email.trim());
            body.put("password", password);
            request("/auth/v1/signup", body, null, callback);
        } catch (Exception e) {
            callback.onResult(false, "Could not prepare account request.", null);
        }
    }

    static void refresh(String refreshToken, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("refresh_token", refreshToken);
            request("/auth/v1/token?grant_type=refresh_token", body, null, callback);
        } catch (Exception e) {
            callback.onResult(false, "Could not restore your session.", null);
        }
    }

    static void signOut(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) return;
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl() + "/auth/v1/logout");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("apikey", BuildConfig.BACKEND_PUBLIC_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.getResponseCode();
            } catch (Exception ignored) {
                // Local credentials are cleared by the caller even when offline.
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private static String baseUrl() {
        return BuildConfig.BACKEND_URL.replaceAll("/+$", "");
    }

    private static void request(String path, JSONObject body, String bearer, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                if (BuildConfig.BACKEND_URL.isEmpty() || BuildConfig.BACKEND_PUBLIC_KEY.isEmpty()) {
                    callback.onResult(false,
                        "Account service is not configured. Add BACKEND_URL and BACKEND_PUBLIC_KEY to GitHub Actions secrets.",
                        null);
                    return;
                }

                URL url = new URL(baseUrl() + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(12000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("apikey", BuildConfig.BACKEND_PUBLIC_KEY);
                if (bearer != null && !bearer.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + bearer);
                }
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = connection.getOutputStream()) { out.write(bytes); }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
                String response = readAll(stream);
                JSONObject json = response.isEmpty() ? new JSONObject() : new JSONObject(response);

                if (status >= 200 && status < 300) {
                    String access = json.optString("access_token", "");
                    String refresh = json.optString("refresh_token", "");
                    if (!access.isEmpty()) {
                        callback.onResult(true, "Connected.", new Session(access, refresh));
                    } else {
                        callback.onResult(true,
                            "Account request received. Check your email if verification is required, then sign in.",
                            null);
                    }
                } else {
                    String message = json.optString("msg",
                        json.optString("message", json.optString("error_description",
                        json.optString("error", "Request failed (" + status + ")."))));
                    callback.onResult(false, message, null);
                }
            } catch (Exception e) {
                callback.onResult(false, "Connection failed. Check your internet and try again.", null);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private static String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
