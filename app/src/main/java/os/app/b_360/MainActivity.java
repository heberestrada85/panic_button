package os.app.b_360;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import static os.app.b_360.App.isBluetoothActive;
import static os.app.b_360.App.isLoged;
import static os.app.b_360.App.isAlarmed;
import static os.app.b_360.App.btn_mac;

import static os.app.b_360.notification.Channels.CHANNEL_ID;
import static os.app.b_360.notification.Channels.CHANNEL_NAME;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import os.app.b_360.proxy.Platform;
import os.app.b_360.proxy.data.P360;
import os.app.b_360.proxy.data.Rapi;
import os.app.b_360.services.MyBluetoothScanServices;
import os.app.b_360.services.MyLocationService;
import os.app.b_360.services.ScannBluetooth;


public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private LocationManager locationManager;

    private FloatingActionButton btnMenu;


    public static SharedPreferences sh;
    public static SharedPreferences.Editor myShEdit;

    ImageView btnPanic;
    ImageView imgLogo360;

    Platform platform;

    public static Handler handlerBtn;

    private static final long LONG_PRESS_DURATION = 2000;
    private long pressStartTime;
    private boolean isPressed = false;

    // Launcher para permisos de ubicación
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allLocationGranted = true;
                        for (Boolean isGranted : permissions.values()) {
                            allLocationGranted = allLocationGranted && isGranted;
                        }
                        if (allLocationGranted) {
                            updateStatus("Permisos de ubicación concedidos");
                            // Después de obtener los permisos de ubicación, solicitar notificaciones
                            //checkAndRequestNotificationPermission();
                            checkAndRequestBluetoothPermissions();
                        } else {
                            updateStatus("Permisos de ubicación denegados");
                        }
                    });

    // Launcher para permisos de Bluetooth
    private final ActivityResultLauncher<String[]> bluetoothPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = areAllPermissionsGranted(permissions);
                        if (allGranted) {
                            updateStatus("Permisos de Bluetooth concedidos");
                            checkAndRequestNotificationPermission();
                        } else {
                            updateStatus("Permisos de Bluetooth denegados");
                        }
                    });

    // Launcher para permiso de notificaciones
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            updateStatus("Todos los permisos concedidos");
                            // Aquí puedes iniciar la funcionalidad completa de tu app
                            startFullFunctionality();
                        } else {
                            updateStatus("Permiso de notificaciones denegado");
                        }
                    });



    private boolean areAllPermissionsGranted(java.util.Map<String, Boolean> permissions) {
        for (Boolean isGranted : permissions.values()) {
            if (!isGranted) {
                return false;
            }
        }
        return true;
    }


    private void updateStatus(String status) {

        Log.d("updateStatus", status);
    }



    private void startFullFunctionality() { // Ok
        // Aquí inicias todas las funcionalidades que requieren los permisos
        // Por ejemplo, comenzar a recibir actualizaciones de ubicación
        // y estar listo para enviar notificaciones

        //Intent iL = new Intent(this, MyLocationService.class);
        //startService(iL);

        startServices(this, MyLocationService.class);

        Toast.makeText(this,
                "¡Aplicación lista con todos los permisos!",
                Toast.LENGTH_SHORT).show();
    }

    private void checkAndRequestBluetoothPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Permisos para Android 12 (API 31) y superiores
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADVERTISE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            // Permisos para Android 11 (API 30) y anteriores
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            bluetoothPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            updateStatus("Permisos de Bluetooth ya concedidos");
            checkAndRequestNotificationPermission();
        }
    }

    private void checkAndRequestNotificationPermission() { //Ok
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                updateStatus("Todos los permisos ya están concedidos");
                startFullFunctionality();
            }
        } else {
            updateStatus("Todos los permisos concedidos (Android < 13)");
            startFullFunctionality();
        }
    }

    private void checkAndRequestBluetoothPermission() { //Ok
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.BLUETOOTH_ADVERTISE)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                updateStatus("Todos los permisos ya están concedidos");
                startFullFunctionality();
            }
        } else {
            updateStatus("Todos los permisos concedidos (Android < 13)");
            startFullFunctionality();
        }
    }


    private final Handler handler = new Handler();
    private Vibrator vibrator;
    private final Runnable vibrateRunnable = new Runnable() {
        @Override
        public void run() {

            btnPanic.setImageResource(R.drawable.b_on_red);

            if (isPressed && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(500); // Vibrar por 500ms
            }
        }
    };



    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        btnPanic = (ImageView) findViewById(R.id.imgBtn);
        imgLogo360 = (ImageView) findViewById(R.id.imgLogoC360);


        handlerBtn = new Handler(message -> {
            //String texto = (String) message.obj;
            //Log.d("new Handler(message -> ", texto);
            //txtBtnName.setText("Detalle del Dispositivo : " + App.btn_name);
            //txtBtnBattery.setText("Batery: " + App.btn_battery);
            //txtBtnRSSI.setText("RSSI: " + App.btn_rssi);

            btnPanic.setImageResource(R.drawable.b_on_red);

            return true;
        });


        btnMenu = (FloatingActionButton) findViewById(R.id.menu);


        btnMenu.setOnClickListener(
                (View v) -> {
                    // Terminar services
                    // si es necesario
                    //this.finishAffinity();

                    //startActivity(new Intent(this, AddInfoBtn.class));

                    showPopupMenu(v);
                    //Intent iB = new Intent(this, MyBluetoothScanServices.class);
                    //startServices(iB);
                }
        );



        imgLogo360.setOnLongClickListener( (View v) -> {

                if(isAlarmed) {

                    isAlarmed = false;

                    myShEdit.putBoolean("App.isAlarmed", isAlarmed);
                    myShEdit.apply();

                    return true;
                }

                return false;
            }
        );


        // Start Permission Sequence
        startPermissionSequence();

        createNotificationChannels();

        platform = new Platform();

        sh = getSharedPreferences("B_360", MODE_PRIVATE);
        myShEdit = sh.edit();


        loadData();


        if (isAlarmed)
            btnPanic.setImageResource(R.drawable.b_on_red);


        if(isLoged) {

            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }


        btnPanic.setOnTouchListener ( (View v, MotionEvent e) -> {

            if ( !isAlarmed ) {
                switch (e.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        Log.d("btnPanic", "Event ACTION_DOWN");
                        btnPanic.setImageResource(R.drawable.b_off_red);
                        isPressed = true;
                        handler.postDelayed(vibrateRunnable, LONG_PRESS_DURATION);
                        // Guardar el tiempo cuando se presiona el botón
                        pressStartTime = System.currentTimeMillis();

                        return true;

                    case MotionEvent.ACTION_UP:
                        Log.d("btnPanic", "Event ACTION_UP");
                        isPressed = false;
                        // Cancelar la vibración programada si el botón se suelta
                        handler.removeCallbacks(vibrateRunnable);

                        // Calcular la duración del press
                        long pressDuration = System.currentTimeMillis() - pressStartTime;

                        // Si la duración es mayor a LONG_PRESS_DURATION
                        if (pressDuration >= LONG_PRESS_DURATION) {
                            Log.d("btnPanic", "3 Sec.");
                            btnPanic.setImageResource(R.drawable.b_on_red);
                            platform.sendEvent();
                            isAlarmed = true;

                            myShEdit.putBoolean("App.isAlarmed", isAlarmed);
                            myShEdit.apply();
                        }
                        // SiNo, la duración es menor a LONG_PRESS_DURATION
                        else
                            btnPanic.setImageResource(R.drawable.b_off_blue);

                        return true;
                }
            }
            return false;
        });

        if(isBluetoothActive) {
            startService(new Intent(getBaseContext(), MyBluetoothScanServices.class));
            ScannBluetooth.isRunning = true;
        }

        Log.d("onCreate", "End.");

    }





    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        menu.setHeaderTitle("Config.");
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.context_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.add_device) {
                    //Toast.makeText(MainActivity.this, "Opción 1 seleccionada", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getBaseContext(), AddInfoBtn.class));
                    return true;
                } else if (id == R.id.add_url) {
                    //Toast.makeText(MainActivity.this, "Opción 2 seleccionada", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getBaseContext(), UrlStore.class));
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }



    private void startPermissionSequence() { //OK
        updateStatus("Iniciando solicitud de permisos...");
        checkAndRequestLocationPermissions();
    }



    private void checkAndRequestLocationPermissions() { // Ok
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            locationPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            updateStatus("Permisos de ubicación ya concedidos");
            checkAndRequestNotificationPermission();
        }
    }



    public void startServices(Activity activity, Class<?> _class_) {

        Intent i = new Intent(activity, _class_);
        startService(i);
    }


    public void loadData() {

        isLoged =   sh.getBoolean("App.isLoged", false);
        isAlarmed = sh.getBoolean("App.isAlarmed", false);

        P360.url = sh.getString("App.url360", "");
        Rapi.url = sh.getString("App.urlRapi", "");

        btn_mac = sh.getString("App.btn_mac", "");

        isBluetoothActive = sh.getBoolean("App.bluetoothActive", false);

    }


    private void createNotificationChannels () {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Canal para sincronización de datos
            NotificationChannel dataSyncChannel = new NotificationChannel(
                    "DATA_SYNC_CHANNEL",  // ID único
                    "Sincronización de datos",  // Nombre visible
                    NotificationManager.IMPORTANCE_MIN // Importancia
            );
            dataSyncChannel.setDescription("Notificaciones relacionadas con la sincronización de datos.");


            // Canal para sincronización de datos
            NotificationChannel bluetoothSyncChannel = new NotificationChannel(
                    "DATA_SYNC_BLUETOOTH",  // ID único
                    "Sincronización de Triggers Bluetooth",  // Nombre visible
                    NotificationManager.IMPORTANCE_MIN // Importancia
            );

            bluetoothSyncChannel.setDescription("Notificaciones relacionadas con los Triggers Bluetooth.");

            // Canal para alertas críticas
            NotificationChannel criticalAlertsChannel = new NotificationChannel(
                    CHANNEL_ID,  // ID único
                    CHANNEL_NAME,  // Nombre visible
                    NotificationManager.IMPORTANCE_MIN // Alta importancia
            );

            criticalAlertsChannel.setDescription("Notificaciones para alertas críticas que requieren atención inmediata.");


            criticalAlertsChannel.enableLights(true);
            criticalAlertsChannel.setLightColor(Color.RED);
            criticalAlertsChannel.enableVibration(true);

            // Registrar los canales en el sistema
            /*val manager = getSystemService(
                NotificationManager::class.java
            )*/


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(dataSyncChannel);
                notificationManager.createNotificationChannel(bluetoothSyncChannel);
                notificationManager.createNotificationChannel(criticalAlertsChannel);
            }

        }
    }


}
