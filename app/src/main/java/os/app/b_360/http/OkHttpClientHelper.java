package os.app.b_360.http;


import android.util.Log;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkHttpClientHelper {

    private static final OkHttpClient client = new OkHttpClient();

    public static String getPageWithAuth(String url, String username, String password) {
        String responseString = "";

        // Crear credenciales para autenticación básica
        String credential = Credentials.basic(username, password);

        // Crear la solicitud
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", credential)
                .build();

        // Ejecutar la solicitud
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                responseString = response.body().string();
                Log.d("Http Ok responseString: ", "HTTP Ok: " + response.code());
            } else {
                responseString = "HTTP Error: " + response.code();
                Log.d("Http Error responseString: ", "HTTP Error: " + response.code());
            }
        } catch (IOException e) {
            responseString = "Error: " + e.getMessage();
            Log.d("Error: ", e.getMessage());
        }

        return responseString;
    }
}

