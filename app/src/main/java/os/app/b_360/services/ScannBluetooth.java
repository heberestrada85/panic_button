package os.app.b_360.services;

import static android.content.ContentValues.TAG;

import static os.app.b_360.App.authenticationToken;
import static os.app.b_360.App.btn_battery;
import static os.app.b_360.App.isAlarmed;
import static os.app.b_360.MainActivity.myShEdit;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import com.minew.beaconplus.sdk.MTCentralManager;
import com.minew.beaconplus.sdk.MTFrameHandler;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.enums.FrameType;
import com.minew.beaconplus.sdk.frames.AccFrame;
import com.minew.beaconplus.sdk.frames.ForceFrame;
import com.minew.beaconplus.sdk.frames.GInfoFrame;
import com.minew.beaconplus.sdk.frames.HTFrame;
import com.minew.beaconplus.sdk.frames.IBeaconFrame;
import com.minew.beaconplus.sdk.frames.InfoFrame;
import com.minew.beaconplus.sdk.frames.LightFrame;
import com.minew.beaconplus.sdk.frames.MinewFrame;
import com.minew.beaconplus.sdk.frames.PIRFrame;
import com.minew.beaconplus.sdk.frames.TamperProofFrame;
import com.minew.beaconplus.sdk.frames.TemperatureFrame;
import com.minew.beaconplus.sdk.frames.TlmFrame;
import com.minew.beaconplus.sdk.frames.TvocFrame;
import com.minew.beaconplus.sdk.frames.UidFrame;
import com.minew.beaconplus.sdk.frames.UrlFrame;
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import os.app.b_360.AddInfoBtn;
import os.app.b_360.App;
import os.app.b_360.MainActivity;
import os.app.b_360.http.OkHttpClientHelper;

/**
 * Created by Oscar Beltrán on 11/12/24.
 */
public class ScannBluetooth {

    public static boolean isRunning = false;
    boolean firstTime = false;
    boolean firstTimeBtn = false;
    private Thread workerThread;

    MTCentralManager mtCentralManager; // = MTCentralManager.getInstance(this);


    private void iniciarActualizaciones() {
        workerThread = new Thread(() -> {
            while (isRunning) {

                if (AddInfoBtn.handler != null) {
                    Message message = Message.obtain();
                    message.obj = "Event";
                    AddInfoBtn.handler.sendMessage(message);
                }
                try {
                    Thread.sleep(500); // Actualizar cada segundo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        workerThread.start();
    }


    public ScannBluetooth() {

        // listen the change of iPhone bluetooth
        // *** also get from "bluetoothstate" property.
        // *** this SDK will work normally only while state == PowerStatePoweredOn!

        /*
        mtCentralManager.setBluetoothChangedListener(new OnBluetoothStateChangedListener() {
            @Override
            public void onStateChanged(BluetoothState state) {
            }
        });

        */
    }


    public void starScan() {

        mtCentralManager.startScan();

        iniciarActualizaciones();

        // Traverse the List，get instance of every device.
        // ** you can also display them on views.
        mtCentralManager.setMTCentralManagerListener(new MTCentralManagerListener() {
            @Override
            public void onScanedPeripheral(final List<MTPeripheral> peripherals) {
                for (MTPeripheral mtPeripheral : peripherals) {
                    // get FrameHandler of a device.
                    MTFrameHandler mtFrameHandler = mtPeripheral.mMTFrameHandler;
                    String mac = mtFrameHandler.getMac(); 		                    //mac address of device
                    String name = App.btn_name = mtFrameHandler.getName();		                    // name of device
                    int battery = App.btn_battery = mtFrameHandler.getBattery();	//battery
                    int rssi = App.btn_rssi = mtFrameHandler.getRssi();		        //rssi
                    //long lastUpdate = mtFrameHandler.getLastUpdate();             //last updated time


                    /*
                    // all data frames of device（such as:iBeacon，UID，URL...）
                    ArrayList<MinewFrame> advFrames = mtFrameHandler.getAdvFrames();
                    for (MinewFrame minewFrame : advFrames) {

                        //Last updated time of each frame
                        Log.v("beaconplus", "lastUpdate:" + minewFrame.getLastUpdate());
                    }
                    */

                    ArrayList<MinewFrame> advFrames = mtFrameHandler.getAdvFrames();

                    if(mac.equals(App.btn_mac))
                        for (MinewFrame minewFrame : advFrames) {
                            FrameType frameType = minewFrame.getFrameType();

                            //Log.d("in for (MinewFrame minewFrame : advFrames)", "FrameType");

                            switch (frameType) {
                                case FrameiBeacon://iBeacon
                                    IBeaconFrame iBeaconFrame = (IBeaconFrame) minewFrame;
                                    Log.v("beaconplus 1", iBeaconFrame.getUuid() + iBeaconFrame.getMajor() + iBeaconFrame.getMinor());
                                    break;

                                case FrameUID://uid
                                    UidFrame uidFrame = (UidFrame) minewFrame;
                                    Log.v("beaconplus 2", uidFrame.getNamespaceId() + uidFrame.getInstanceId());
                                    break;

                                case FrameAccSensor:
                                    AccFrame accFrame = (AccFrame) minewFrame;//acc
                                    //Log.v("beaconplus 3", "" + accFrame.getXAxis() + accFrame.getYAxis() + accFrame.getZAxis());
                                    break;

                                case FrameHTSensor:
                                    HTFrame htFrame = (HTFrame) minewFrame;//ht
                                    Log.v("beaconplus 4", "" + htFrame.getTemperature() + htFrame.getHumidity());
                                    break;

                                case FrameTLM:
                                    TlmFrame tlmFrame = (TlmFrame) minewFrame;//tlm
                                    //Log.v("beaconplus 5", "" + tlmFrame.getTemperature() + tlmFrame.getBatteryVol() + tlmFrame.getSecCount() + tlmFrame.getAdvCount());
                                    break;

                                case FrameURL:
                                    UrlFrame urlFrame = (UrlFrame) minewFrame;//url

                                    isAlarmed = true;

                                    myShEdit.putBoolean("App.isAlarmed", isAlarmed);
                                    myShEdit.apply();

                                    if(!firstTime) {
                                        //Log.v("beaconplus 6", "Link:" + urlFrame.getUrlString() + " Rssi @ 0m:" + urlFrame.getTxPower());

                                        firstTime = !firstTime;

                                        if (MainActivity.handlerBtn != null) {
                                            Message message = Message.obtain();
                                            message.obj = "Alert";
                                            MainActivity.handlerBtn.sendMessage(message);
                                        }

                                        sendHttpRequest();
                                        rapidSOS();
                                    }

                                    Log.v("beaconplus 6", "Link:" + urlFrame.getUrlString() + " Rssi @ 0m:" + urlFrame.getTxPower());
                                    break;

                                case FrameLightSensor:
                                    LightFrame lightFrame = (LightFrame) minewFrame;//light
                                    Log.v("beaconplus 7", "battery:" + lightFrame.getBattery() + "%" + lightFrame.getLuxValue());
                                    break;

                                case FrameForceSensor:
                                    ForceFrame forceFrame = ((ForceFrame) minewFrame);//force
                                    Log.v("beaconplus 8", "battery:" + forceFrame.getBattery() + "%" + "force:" +  forceFrame.getForce() + "gram");
                                    break;

                                case FramePIRSensor:
                                    PIRFrame pirFrame = ((PIRFrame) minewFrame);//pir
                                    Log.v("beaconplus 9", "battery:" + pirFrame.getBattery() + "%" + "PIR: " + pirFrame.getBattery());
                                    break;

                                case FrameTempSensor://temp
                                    TemperatureFrame temperatureFrame = (TemperatureFrame) minewFrame;
                                    Log.v("beaconplus 10", "battery:" + temperatureFrame.getBattery() + "%,temperature:" + String.format("%.2f", temperatureFrame.getValue()) + "°C");
                                    break;

                                case FrameTVOCSensor://tvoc
                                    TvocFrame tvocFrame = (TvocFrame) minewFrame;
                                    Log.v("beaconplus 11", "battery:" + tvocFrame.getBattery() + ",TVOC:" + tvocFrame.getValue() + "ppb," + "battery:" +tvocFrame.getBattery() + "mV");
                                    break;

                                /*
                            case FrameLineBeacon://FrameLineBeacon
                                FrameLineBeacon lineBeacon = ((FrameLineBeacon) minewFrame);
                                Log.v("beaconplus", "Hwid:" + lineBeacon.getHwid()
                                        + ",Rssi@1m:", lineBeacon.getTxPower()
                                        + ",authentication:" + lineBeacon.getAuthentication()
                                        + ",timesTamp:" + lineBeacon.getTimesTamp());
                                break;
                                */
                                case FrameTamperProof://TamperProofFrame
                                    TamperProofFrame tamperProofFrame = ((TamperProofFrame) minewFrame);//
                                    Log.v("beaconplus 12", "battery:" + tamperProofFrame.getBattery());
                                    break;

                                case FrameInfo://InfoFrame
                                    InfoFrame infoFrame = ((InfoFrame) minewFrame);//
                                    Log.v("beaconplus 13", infoFrame.getMajor() + infoFrame.getMinor() + 	infoFrame.getBatteryVoltage());
                                    break;

                                case FrameGInfo://GInfoFrame
                                    GInfoFrame GinfoFrame = ((GInfoFrame) minewFrame);//
                                    Log.v("beaconplus 14", GinfoFrame.getMac() + GinfoFrame.getBattery() + 	GinfoFrame.getUuid());
                                    break;

                                default:
                                    break;
                        }
                    }

                }
            }
        });
    }



    private void rapidSOS() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "https://api-sandbox.rapidsos.com/v1/rem/trigger/hook/oxxo/trigger?tienda=15UCH";
                String username = "rsosoxxo1";
                String password = "2MGJLIfazi";

                String response = OkHttpClientHelper.getPageWithAuth(url, username, password);
                Log.d("SCAN HTTP_RESPONSE", response);
            }
        }).start();

    }


    private void sendHttpRequest() {

        OkHttpClient client = new OkHttpClient();

        FormBody rb = new FormBody.Builder()
                .add("name", "Sample Location")
                .add("alert", "Button Panic")
                .add("latitude", MyLocationService.getLAT())
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
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
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
