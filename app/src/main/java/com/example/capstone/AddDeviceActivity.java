package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.capstone.databinding.FragmentHomeBinding;
import com.example.capstone.ui.home.HomeFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AddDeviceActivity extends AppCompatActivity {

    private Button newButton;
    private BluetoothAdapter bluetoothAdapter;
    private boolean deviceFound;
    public BluetoothDevice arduinoDevice;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt gatt;

    private UUID healthService = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");

    private UUID deviceIdUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1215");
    private UUID emailUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1216");
    private UUID wifiSsidUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1217");

    private UUID wifiPassUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1218");

    private UUID ageUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1219");

    private UUID sexUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1220");

    private UUID weightUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1221");

    private boolean scanning;

    private int scanCount = 5;
    private boolean ssidSuccess, emailSuccess, passSuccess, ageSuccess, sexSuccess, weightSuccess;

    private Handler handler = new Handler();

    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add Device");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        checkPermissions();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    public void refreshList(View view) {

        if (arduinoDevice != null && deviceFound && newButton == null) {
            newButton = new Button(this);
            LinearLayout layout = findViewById(R.id.linearLayout);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 1);
            }
            newButton.setText(arduinoDevice.getName());
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // add the device to the home page with details
                    connectToDevice(arduinoDevice);
                }
            });
            layout.addView(newButton);
        } else {
            if (!scanning) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanning = false;
                        bleScanner.stopScan(leScanCallback);
                    }
                }, SCAN_PERIOD);

                scanning = true;
                bleScanner.startScan(leScanCallback);
            } else {
                scanning = false;
                bleScanner.stopScan(leScanCallback);
            }
        }

    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
            try {
                if (device.getAddress() != null && device.getName().equals("HEALTH SERVICE") && deviceFound != true) {
                    Log.d("Bluetooth", device.getName().toString());
                    deviceFound = true;
                    arduinoDevice = device;
                    refreshList(findViewById(R.id.imageButton9));
                } else {
                    deviceFound = false;
                    return;
                }

            } catch (NullPointerException e) {
                deviceFound = false;
                return;
            }

        }
    };

    private void connectToDevice(BluetoothDevice device) {
        Log.d("Connection Status", "Attempting to Connect...");
        gatt = device.connectGatt(this, false, gattCallback);

        Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        super.finish();
    }
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.d("Connection State Change", "Connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Connection State Change", "Disconnected");
                if (scanCount > 0) {
                    retryConnection();
                    scanCount--;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("Read Characteristics", "reading...");
            Log.d("services", gatt.getServices().toString());

            BluetoothGattService service = gatt.getService(healthService);

            if (service != null) {

                BluetoothGattCharacteristic deviceIdCharacteristic = service.getCharacteristic(deviceIdUuid);
                gatt.readCharacteristic(deviceIdCharacteristic);

            } else {
                Log.d("Disconnected Device", "Disconnected Device");
                if (scanCount > 0) {
                    retryConnection();
                    scanCount--;
                }
            }

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("Characteristic Read", "Characteristic UUID: " + characteristic.getUuid());
            BluetoothGattService service = gatt.getService(healthService);

            BluetoothGattCharacteristic wifiSsidCharacteristic = service.getCharacteristic(wifiSsidUuid);
            BluetoothGattCharacteristic wifiPassCharacteristic = service.getCharacteristic(wifiPassUuid);
            BluetoothGattCharacteristic emailCharacteristic = service.getCharacteristic(emailUuid);
            BluetoothGattCharacteristic ageCharacteristic = service.getCharacteristic(ageUuid);
            BluetoothGattCharacteristic sexCharacteristic = service.getCharacteristic(sexUuid);
            BluetoothGattCharacteristic weightCharacteristic = service.getCharacteristic(weightUuid);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid  = info.getSSID().replaceAll("\"", "");
            Log.d("bigballs", ssid);
            if (ssid != null) {
                byte[] ssidBytes = ssid.getBytes(StandardCharsets.UTF_8);
                wifiSsidCharacteristic.setValue(ssidBytes);
            }

            if (MainActivity.wifiPass != "") {
                byte[] wifiPassBytes = MainActivity.wifiPass.getBytes(StandardCharsets.UTF_8);
                wifiPassCharacteristic.setValue(wifiPassBytes);
                Log.d("bigballs", MainActivity.wifiPass);
            }

            if (MainActivity.email != "") {
                byte[] emailBytes = MainActivity.email.getBytes(StandardCharsets.UTF_8);
                emailCharacteristic.setValue(emailBytes);
                Log.d("bigballs", MainActivity.email);
            }

            if (MainActivity.age != -1) {
                byte[] ageBytes = Integer.toString(MainActivity.age).getBytes(StandardCharsets.UTF_8);
                ageCharacteristic.setValue(ageBytes);
                Log.d("bigballs", Integer.toString(MainActivity.age));
            }


            if (MainActivity.sex != "") {
                byte[] sexBytes = MainActivity.sex.getBytes(StandardCharsets.UTF_8);
                sexCharacteristic.setValue(sexBytes);
                Log.d("bigballs", MainActivity.sex);
            }



            if (MainActivity.weight != -1) {
                byte[] weightBytes = Float.toString(MainActivity.weight).getBytes(StandardCharsets.UTF_8);
                weightCharacteristic.setValue(weightBytes);
                Log.d("bigballs", Float.toString(MainActivity.weight));
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
                Handler writeDataSsidHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataSsid = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Successssss: ", "runnable" + ssidSuccess);
                        if (ssidSuccess) {
                            writeDataSsidHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(wifiSsidCharacteristic);
                            writeDataSsidHandler.postDelayed(this, 10);

                        }
                    }
                };
                writeDataSsidHandler.postDelayed(writeDataSsid,10);

                Handler writeDataPassHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataPwd = new Runnable() {
                    @Override
                    public void run() {
                        if (passSuccess) {
                            writeDataPassHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(wifiPassCharacteristic);
                            writeDataPassHandler.postDelayed(this, 10);
                        }

                    }
                };
                writeDataPassHandler.postDelayed(writeDataPwd,10);


                Handler writeDataEmailHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataEmail = new Runnable() {
                    @Override
                    public void run() {
                        if (emailSuccess) {
                            writeDataEmailHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(emailCharacteristic);
                            writeDataEmailHandler.postDelayed(this, 10);
                        }

                    }
                };
                writeDataEmailHandler.postDelayed(writeDataEmail,10);

                Handler writeDataAgeHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataAge = new Runnable() {
                    @Override
                    public void run() {
                        if (ageSuccess) {
                            writeDataAgeHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(ageCharacteristic);
                            writeDataAgeHandler.postDelayed(this, 10);
                        }

                    }
                };
                writeDataAgeHandler.postDelayed(writeDataAge,10);


                Handler writeDataSexHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataSex = new Runnable() {
                    @Override
                    public void run() {
                        if (sexSuccess) {
                            writeDataSexHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(sexCharacteristic);
                            writeDataSexHandler.postDelayed(this, 10);
                        }

                    }
                };
                writeDataSexHandler.postDelayed(writeDataSex,10);

                Handler writeDataWeightHandler = new Handler(Looper.getMainLooper());
                Runnable writeDataWeight = new Runnable() {
                    @Override
                    public void run() {
                        if (weightSuccess) {
                            writeDataWeightHandler.removeCallbacks(this);
                        } else {
                            gatt.writeCharacteristic(weightCharacteristic);
                            writeDataWeightHandler.postDelayed(this, 10);
                        }

                    }
                };
                writeDataWeightHandler.postDelayed(writeDataWeight,10);


            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("Characteristic Write", "Characteristic UUID: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristic write successful
                Log.d(TAG, "Characteristic write successful");
                if (characteristic.getUuid().equals(emailUuid)) {
                    emailSuccess = true;
                } else if (characteristic.getUuid().equals(wifiSsidUuid)) {
                    ssidSuccess = true;
                    Log.d("Successssss: ", "balls" + ssidSuccess);
                } else if (characteristic.getUuid().equals(wifiPassUuid)) {
                    passSuccess = true;
                } else if (characteristic.getUuid().equals(sexUuid)) {
                    sexSuccess = true;
                } else if (characteristic.getUuid().equals(weightUuid)) {
                    weightSuccess = true;
                } else if (characteristic.getUuid().equals(ageUuid)) {
                    ageSuccess = true;
                }




            } else {
                // Characteristic write failed
                Log.e(TAG, "Characteristic write failed with status: " + status);
            }
        }
        private void broadcastUpdate(BluetoothGattCharacteristic characteristic) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = characteristic.getStringValue(0);

                    if (characteristic.getUuid().toString().equals(deviceIdUuid.toString())) {
                        // check if bpm service
                        Log.d("Device ID", value);
                        HomeFragment.isRegistered = 1;
                        RequestQueue volleyQueue = Volley.newRequestQueue(AddDeviceActivity.this);

                        String createurl = "https://78.138.17.29:3000/registerDevice";
                        JSONObject createjsonObject = new JSONObject();
                        try {
                            createjsonObject.put("is_registered", 1);
                            createjsonObject.put("device_id", value);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                                Request.Method.PUT,
                                createurl,
                                createjsonObject,
                                response -> {
                                    Log.d("Response", "Raw response: " + response.toString());

                                },
                                error -> {
                                    if (error.networkResponse != null) {
                                        Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                                        Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                                    }

                                    Toast.makeText(AddDeviceActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                                    Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                                }
                        ) { @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("x-api-key", SignUpPage.apiKey.toString());

                            // Add other headers if needed
                            return headers;
                        }
                        };
                        volleyQueue.add(newjsonObjectRequest);

                    }

                }
            });
        }

    };



    protected void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }
    private void retryConnection() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Reconnect to the device here
                connectToDevice(arduinoDevice);
            }
        }, 5000); // Adjust RETRY_DELAY_MS as needed
    }

}

