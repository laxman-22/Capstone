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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.databinding.FragmentHomeBinding;
import com.example.capstone.ui.home.HomeFragment;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

    private UUID pulseUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1215");
    private UUID oxygenUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1216");
    private UUID stepUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1217");

    private UUID latitudeUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1218");

    private UUID longitudeUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1219");

    private UUID batteryUuid = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1220");


    private boolean scanning;

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
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("Read Characteristics", "reading...");
            Log.d("services", gatt.getServices().toString());

            BluetoothGattService service = gatt.getService(healthService);

            if (service != null) {

                BluetoothGattCharacteristic pulseCharacteristic = service.getCharacteristic(pulseUuid);

                BluetoothGattCharacteristic oxygenCharacteristic = service.getCharacteristic(oxygenUuid);

                BluetoothGattCharacteristic stepCharacteristic = service.getCharacteristic(stepUuid);

                BluetoothGattCharacteristic batteryCharacteristic = service.getCharacteristic(batteryUuid);

                BluetoothGattCharacteristic latCharacteristic = service.getCharacteristic(latitudeUuid);

                BluetoothGattCharacteristic lonCharacteristic = service.getCharacteristic(longitudeUuid);

                Thread readPulse = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(pulseCharacteristic);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readPulse.start();
                Thread readOx = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(oxygenCharacteristic);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readOx.start();
                Thread readSteps = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(stepCharacteristic);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readSteps.start();
                Thread readBattery = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(batteryCharacteristic);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readBattery.start();
                Thread readLat = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(latCharacteristic);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readLat.start();
                Thread readLon = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(lonCharacteristic);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                readLon.start();


            } else {
                Log.d("Disconnected Device", "Disconnected Device");
            }

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("Characteristic Read", "Characteristic UUID: " + characteristic.getUuid());


            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(characteristic);
            }

        }
        private void broadcastUpdate(BluetoothGattCharacteristic characteristic) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = characteristic.getValue();
                    int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();

                    float result = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();


                    if (characteristic.getUuid().toString().equals(pulseUuid.toString())) {
                        // check if bpm service
                        Log.d("Pulse Data Received", Integer.toString(receivedValue));
                        HomeFragment.bpm = receivedValue;
                    }
                    else if (characteristic.getUuid().toString().equals(oxygenUuid.toString())) {
                        // check if ox service
                        Log.d("Oxygen Data Received", Integer.toString(receivedValue));
                        HomeFragment.oxSat = receivedValue;
                    }
                    else if (characteristic.getUuid().toString().equals(stepUuid.toString())) {
                        // check if step service
                        Log.d("Steps Data Received", Integer.toString(receivedValue));
                        HomeFragment.steps = receivedValue;
                    } else if (characteristic.getUuid().toString().equals(batteryUuid.toString())) {
                        // check if battery service
                        Log.d("Battery Data Received", Integer.toString(receivedValue));
                        HomeFragment.battery = receivedValue;
                    } else if (characteristic.getUuid().toString().equals(latitudeUuid.toString())) {
                        // check if lat service
                        Log.d("Latitude Data Received", ""+result);
                        HomeFragment.lat = result;

                    } else if (characteristic.getUuid().toString().equals(longitudeUuid.toString())) {
                        // check if lon service
                        Log.d("Longitude Data Received", ""+result);
                        HomeFragment.lon = result;
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

}

