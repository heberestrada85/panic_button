package os.app.b_360.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.minew.beaconplus.sdk.MTCentralManager;

import os.app.b_360.R;

public class MyBluetoothScanServices extends Service {


    private static final String TAG = "Bluetooth Scan Services";
    private static final int INTERVAL = 1 * 15 * 1000; // 3 minutos en milisegundos
    private Handler handler = new Handler();
    private Runnable runnable;

    ScannBluetooth myScan = new ScannBluetooth();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio Bluetooth Creado");

        myScan.mtCentralManager = MTCentralManager.getInstance(this);

        // Configurar la tarea periódica
        runnable = new Runnable() {
            @Override
            public void run() {
                //sendHttpRequest();
                myScan.starScan();
                //handler.postDelayed(this, INTERVAL); // Repetir cada 3 minutos
            }
        };
        handler.post(runnable); // Iniciar la tarea
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new NotificationCompat.Builder(this, "DATA_SYNC_BLUETOOTH")
                .setContentTitle("Bluetooth Scan Services en ejecución")
                .setContentText("Scan Activo")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(3, notification); // Convierte el servicio en primer plano

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Detener las tareas periódicas
        Log.d(TAG, "Servicio Bluetooth Scan Finalizado");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
