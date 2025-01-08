package os.app.b_360;

import static os.app.b_360.App.btn_mac;
import static os.app.b_360.App.isAlarmed;
import static os.app.b_360.App.isBluetoothActive;
import static os.app.b_360.MainActivity.myShEdit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import os.app.b_360.services.MyBluetoothScanServices;
import os.app.b_360.services.ScannBluetooth;


public class AddInfoBtn extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 123;

    TextView txtDeviceName, txtBtnName, txtBtnBattery, txtBtnRSSI;
    EditText editMac;
    Button btnSave;
    ImageButton btnScann;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swScannActive;

    public static Handler handler;
    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    // Mostrar el resultado en el TextView
                    editMac.setText(result.getContents());
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_info_btn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        txtBtnName = (TextView) findViewById(R.id.txtBtnName);
        txtBtnBattery = (TextView) findViewById(R.id.txtBtnBattery);
        txtBtnRSSI = (TextView) findViewById(R.id.txtBtnRSSI);


        // Configurar Handler para actualizar UI
        handler = new Handler(message -> {
            //String texto = (String) message.obj;
            //Log.d("new Handler(message -> ", texto);
            txtBtnName.setText("Detalle del Dispositivo : " + App.btn_name);
            txtBtnBattery.setText("Batery: " + App.btn_battery);
            txtBtnRSSI.setText("RSSI: " + App.btn_rssi);
            return true;
        });

        editMac = (EditText) findViewById(R.id.editMac);
        btnScann = (ImageButton) findViewById(R.id.btnScann);
        btnSave = (Button) findViewById(R.id.btnSave);

        swScannActive = (Switch) findViewById(R.id.swScannActive);


        swScannActive.setChecked(isBluetoothActive);


        // Configurar listener del Switch
        swScannActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("swScannActive True", "is " + swScannActive);

                    ScannBluetooth.isRunning = true;

                    myShEdit.putBoolean("App.bluetoothActive", true);
                    myShEdit.apply();

                    Intent i = new Intent(getBaseContext(), MyBluetoothScanServices.class);
                    startService(i);
                } else {
                    Log.d("swScannActive False", "is " + swScannActive);
                    ScannBluetooth.isRunning = false;

                    myShEdit.putBoolean("App.bluetoothActive", false);
                    myShEdit.apply();

                    stopService(new Intent(getBaseContext(), MyBluetoothScanServices.class));
                }
            }
        });


        txtDeviceName = (TextView) findViewById(R.id.txtDeviceName);

        editMac.setText(btn_mac);
        txtDeviceName.setText(btn_mac);

        btnSave.setOnClickListener(view -> {
            Log.d("btnSave", "Click");
            myShEdit.putString("App.btn_mac", editMac.getText().toString());
            myShEdit.apply();

            btn_mac = editMac.getText().toString();

        });

        btnScann.setOnClickListener(view -> {
            // Configurar y lanzar el escáner
            ScanOptions options = new ScanOptions()
                    .setPrompt("Coloca el código QR dentro del rectángulo")
                    .setBeepEnabled(true)
                    .setOrientationLocked(true)
                    //.setCaptureActivity(CustomScannerActivity.class)
                    ;
            qrLauncher.launch(options);
        });
    }
}

/*

class CaptureCam extends CaptureActivity {

}

*/