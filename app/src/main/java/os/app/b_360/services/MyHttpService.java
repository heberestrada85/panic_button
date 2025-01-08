package os.app.b_360.services;

import static os.app.b_360.App.authenticationToken;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import os.app.b_360.R;

public class MyHttpService extends Service {

    private static final String TAG = "HttpService";
    private static final int INTERVAL = 1 * 15 * 1000; // 3 minutos en milisegundos
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");

        // Configurar la tarea periódica
        runnable = new Runnable() {
            @Override
            public void run() {
                sendHttpRequest();
                handler.postDelayed(this, INTERVAL); // Repetir cada 3 minutos
            }
        };
        handler.post(runnable); // Iniciar la tarea
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new NotificationCompat.Builder(this, "DATA_SYNC_CHANNEL")
                .setContentTitle("HttpService en ejecución")
                .setContentText("Realizando peticiones periódicas")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(2, notification); // Convierte el servicio en primer plano
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Detener las tareas periódicas
        Log.d(TAG, "Servicio destruido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No se utiliza en este caso
    }

    private void sendHttpRequest() {

        OkHttpClient client = new OkHttpClient();

        FormBody rb = new FormBody.Builder()
                .add("name", "Sample Location")
                .add("latitude",  MyLocationService.getLAT())
                .add("longitude", MyLocationService.getLON())
                .add("details", "This is a sample location in Los Angeles.")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + authenticationToken)
                .url("http://3.92.83.103/api/v1/maps") // Reemplaza con tu URL real
                .post(rb) // Puedes cambiar a .post() si necesitas enviar datos
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Error en la solicitud: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Respuesta del servidor: " + responseBody);
                    } else {
                        Log.e(TAG, "Error en la respuesta del servidor: " + response.message());
                    }
                }
            }
        );
    }

}
