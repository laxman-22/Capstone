package com.example.capstone;

import androidx.annotation.NonNull;
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
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class AddDeviceActivity extends AppCompatActivity {

    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter bluetoothAdapter;
    private boolean deviceFound;
    public BluetoothDevice arduinoDevice;
    private BluetoothLeScanner bleScanner;

    private boolean scanning;

    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 10000;

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

        if (arduinoDevice != null && deviceFound) {
            Button newButton = new Button(this);
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
            deviceFound = true;
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
            if (device.getName() != null && device.getName().equals("HEALTH SERVICE") && deviceFound != true) {
                Log.d("Bluetooth", device.getName().toString());
                deviceFound = true;
                arduinoDevice = device;
                refreshList(findViewById(R.id.imageButton9));
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        Log.d("Connection Status", "Attempting to Connect...");
        device.connectGatt(this, false, gattCallback);

        Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        super.finish();
    }
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.d("Connection State Change", "Connection State changed");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Toast.makeText(AddDeviceActivity.this, "Disconnected from " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("Read Characteristics", "reading...");

            UUID healthService = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");

            UUID pulseService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1215");
            UUID oxygenService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1216");
            UUID stepService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1217");
//            UUID batteryService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1219");
            UUID locationService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1218");
//            UUID alertService = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1220");


            BluetoothGattService service = gatt.getService(healthService);

            if (service != null) {
                BluetoothGattCharacteristic pulseCharacteristic = service.getCharacteristic(pulseService);
                gatt.setCharacteristicNotification(pulseCharacteristic, true);

                BluetoothGattCharacteristic oxygenCharacteristic = service.getCharacteristic(oxygenService);
                gatt.setCharacteristicNotification(oxygenCharacteristic, true);

                BluetoothGattCharacteristic stepCharacteristic = service.getCharacteristic(stepService);
                gatt.setCharacteristicNotification(stepCharacteristic, true);

                BluetoothGattCharacteristic locationCharacteristic = service.getCharacteristic(locationService);
                gatt.setCharacteristicNotification(locationCharacteristic, true);

//                BluetoothGattCharacteristic batteryCharacteristic = service.getCharacteristic(batteryService);
//                gatt.setCharacteristicNotification(batteryCharacteristic, true);

//                BluetoothGattCharacteristic alertCharacteristic = service.getCharacteristic(alertService);
//                gatt.setCharacteristicNotification(alertCharacteristic, true);
                Thread thread = new Thread(() -> {
                    while (true) {
                        gatt.readCharacteristic(pulseCharacteristic);
                        gatt.readCharacteristic(oxygenCharacteristic);
                        gatt.readCharacteristic(stepCharacteristic);
//                        gatt.readCharacteristic(batteryCharacteristic);
                        gatt.readCharacteristic(locationCharacteristic);
//                        gatt.readCharacteristic(alertCharacteristic);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            } else {
                gatt.discoverServices();
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("CharacteristicChanged", "Characteristic UUID: " + characteristic.getUuid());
            broadcastUpdate(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("Characteristic Read", "Characteristic UUID: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                broadcastUpdate(characteristic);
            }

        }
        public void broadcastUpdate(BluetoothGattCharacteristic characteristic) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = characteristic.getValue();
                    //int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    Log.d("Data Received", Arrays.toString(data));
//                    if (characteristic.getUuid().equals("19B10001-E8F2-537E-4F6C-D104768A1215")) {
//                        // check if bpm service
//                        byte[] data = characteristicgetValue();
//                        int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
//                        Log.d("Data Received", Integer.toString(receivedValue));
//                        HomeFragment.bpm = receivedValue;
//                    }
//                    else if (characteristic.getUuid().equals("")) {
//                        // check if steps service
//                        byte[] data = characteristic.getValue();
//                        int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
//                        Log.d("Data Received", Integer.toString(receivedValue));
//                        HomeFragment.steps = receivedValue;
//
//                    }
//                    else if (characteristic.getUuid().equals("")) {
//                        // check if oxygen service
//                        byte[] data = characteristic.getValue();
//                        int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
//                        Log.d("Data Received", Integer.toString(receivedValue));
//                        HomeFragment.oxSat = receivedValue;
//
//                    } else if (characteristic.getUuid().equals("")) {
//                        byte[] data = characteristic.getValue();
//                        int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
//                        Log.d("Data Received", Integer.toString(receivedValue));
//                        HomeFragment.battery = receivedValue;
//
//                    } else if (characteristic.getUuid().equals("19B10001-E8F2-537E-4F6C-D104768A1218")) {
////                        process the location here
//                        byte[] data = characteristic.getValue();
//                        //LatLng location = new LatLng();
////                        HomeFragment.location = location;
//                        Log.d("Data Received", Arrays.toString(data));
//
//                    } else if (characteristic.getUuid().equals("")) {
////                        process the alerts here
//                        byte[] data = characteristic.getValue();
//                        int receivedValue = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
////                        HomeFragment.alert = receivedValue;
//                        Log.d("Data Received", Integer.toString(receivedValue));
//
//                    }

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