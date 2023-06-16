package com.example.checksensoravailability;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.util.Log;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.example.checksensoravailability.databinding.ActivityMainBinding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//Bluetooth library
import android.bluetooth.BluetoothAdapter;

//sensors library SDK
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

public class MainActivity extends Activity implements SensorEventListener {

    public ArrayList<BluetoothDevice> discovered_devices_list = new ArrayList<>();
    public DeviceListAdapter deviceListAdapter;


    private TextView tv_heartRate;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView tv_pressure;
    private TextView tv_Accelerometer;
    private TextView tv_Gyroscope;
    private TextView tv_Temp;
    private ActivityMainBinding binding;
    private SeekBar slider;
    private TextView tv_heart_beat;
    private Button btn_search;

    //bluetooth handler data type
    private BluetoothAdapter bluetoothAdapter;

    // sensors data type ( manager + sensor )
    private SensorManager sensorManager ;
    private Sensor heart_rate_sensor;

    //heart  rate sensor value
    private float heartRateValue ;

    private static final String TAG = "____Main___";
    private static final String TAG_broadcast_1 = "__BRADCAST1__";

    BluetoothAdapter bluetoothAdapter_default;


    // device self discovery options BroadcastReceiver
    private final BroadcastReceiver discovery_broadcast  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:   //device is discovered and can be coneected
                        Log.d(TAG_broadcast_1, "Mode 1 : device is discoverable and connectable  ");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:        //device can't be discovered and can be coneected
                        Log.d(TAG_broadcast_1, "Mode 2 : connectable but not discoverable ");
                        break;

                    case BluetoothAdapter.SCAN_MODE_NONE:        //device can't be discovered  or connected
                        Log.d(TAG_broadcast_1, "Mode 3 : total lockdown ");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:        //establishing connection ......
                        Log.d(TAG_broadcast_1, "Mode 4: Connecting ....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG_broadcast_1, "Mode 5:Device  Connected ");
                        break;

                }

            }

        }
    };

    //END OF  device self discovery options BroadcastReceiver



    private BroadcastReceiver discovery_broadcast2 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG_broadcast_1 , "On Receive action found ");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discovered_devices_list.add(device);
                Log.d(TAG_broadcast_1 , " On Receive : " + device.getName() +  device.getAddress());


            }



        }
    };







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG, "ِActivity started ");

        bluetoothAdapter_default = BluetoothAdapter.getDefaultAdapter();

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter_default.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(discovery_broadcast,intentFilter);









        binding = ActivityMainBinding.inflate(getLayoutInflater()); // binder for framelayout
        setContentView(binding.getRoot());

        tv_heartRate = binding.textHEARTRATE;
        tv_pressure = binding.textPressure;
        //v_Gyroscope = binding.textGyroscope;
        btn_search = binding.buttonSearch;
        tv_heart_beat = binding.heartBeatValue;
//        tv_Accelerometer = binding.textAccelerometer;
//        tv_Temp = binding.textTemp;







      //  checkBlueTooth();

//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
       // registerReceiver()

        // broadcast Reciver for checking surrounding devices
//        final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
//
//            public void onReceive(Context context, Intent intent) {             // inner class
//                String action = intent.getAction();
//                Log.d(TAG, " >>>>>>>>>>> DEBG : INSIDE BLUETOOTH SCANNING");
//
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // A new device is discovered
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    // Do something with the device (e.g., display in a list, connect, etc.)
//                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        BluetoothSocket socket = null;
//                        try {
//                            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//                            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
//                            socket.connect();
//                            Log.d(TAG, "<<<<<<<<<>>>>>>>>>>>CONNECTION : OK ");
//
//                            // Assuming you have obtained the BluetoothSocket object named 'socket'
//
//                            try {
//                                InputStream inputStream = socket.getInputStream();
//                                OutputStream outputStream = socket.getOutputStream();
//
//                                // Reading data from the InputStream
//                                byte[] buffer = new byte[1024];
//                                int bytesRead;
//                                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                                    // Process the received data
//                                    String receivedData = new String(buffer, 0, bytesRead);
//                                    // Do something with the received data
//                                }
//
//                                // Writing data to the OutputStream
//                                String dataToSend = "Hello, Bluetooth device!";
//                                outputStream.write(dataToSend.getBytes());
//                                outputStream.flush();
//
//                                // Close the streams when done
//                                inputStream.close();
//                                outputStream.close();
//                            } catch (IOException e) {
//                                // Handle exceptions
//                            }
//
//
//
//
//
//                        } catch (IOException e) {
//                            Log.d(TAG, "<<<<<<<<<>>>>>>>>>>>CONNECTION : NOT OK ");
//                        } finally {
//                            // Close the socket when no longer needed
//                            if (socket != null) {
//                                try {
//                                    socket.close();
//                                } catch (IOException e) {
//                                    // Error occurred while closing the socket
//                                }
//                            }
//                        }
//
//
//                    }
//                }
//            }
//        };
//
//        Intent intent = new Intent() ;
//        discoveryReceiver.onReceive(MainActivity.this ,intent );
//



        checkPermission();                  // access sensors permission level                      DONE
      //  checkSensorAvailability();        // list  and count all available sensors in the watch   DONE
                     // check bluetooth availability




        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        heart_rate_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);




//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(discoveryReceiver, filter);
//        bluetoothAdapter.startDiscovery();


        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : bondedDevices) {
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            Log.d(TAG , "DEBUG  : available surrounding devices ...." + device + "\t \t " +  "Device NAme :" + deviceName + "\t \t " + "Device Address : " + deviceAddress);

        }




//
//        BluetoothDevice device = ... // Retrieve the desired device
//        BluetoothSocket socket = null;
//
//        try {
//            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
//            socket.connect();
//            // Connection successful, perform data transfer or other operations
//        } catch (IOException e) {
//            // Handle connection errors
//        } finally {
//            // Close the socket when finished
//            try {
//                if (socket != null) {
//                    socket.close();
//                }
//            } catch (IOException e) {
//                // Handle socket closing error
//            }
//        }




    }

    @Override
    protected void onResume() {
        super.onResume();

            sensorManager.registerListener(this, heart_rate_sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, ">>>>>>>>>>> DEBUGIN : OnResume method Check  <<<<<<<<<<<");




    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener to save resources
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle the sensor data
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRateValue = event.values[0];
            tv_heart_beat.setText("Heart Rate: " + heartRateValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }







    // Register the receiver and start discovery


    @SuppressLint("MissingPermission")
    public void button_search(View view)
    {
        Log.d(TAG_broadcast_1 , "Searching for devices ");

        if(bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG_broadcast_1 , "canceling discovery ");

            bluetoothAdapter.startDiscovery();
            IntentFilter discorey_intnet = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discovery_broadcast2 ,discorey_intnet );
        }
        if(!bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.startDiscovery();
            IntentFilter discorey_intnet = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discovery_broadcast2 ,discorey_intnet );

        }



    }





//    private void checkBlueTooth() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
//            Log.d(TAG, ">>>>>>>>>>> DEBUGIN : Bluetooth service  is not working <<<<<<<<<<<");
//        } else {
//            Log.d(TAG, ">>>>>>>>>>> DEBUGIN : Bluetooth service  is  working <<<<<<<<<<<");
//
//        }

//        btn_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_LONG).show();
//
//                if (!bluetoothAdapter.isEnabled()) {
//                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    startActivityForResult(enableBluetoothIntent, 1);
//
//                    Intent intent_bluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
//                    {
//                        requestPermissions(
//                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
////                        startActivityForResult(intent_bluetooth, 0);
//                        Log.d(TAG, ">>>>>>>>>>> DEBUGIN : Bluetooth service  is permitted  <<<<<<<<<<<");
//                        return;
//                    }
//                }
//                else
//                {
//                    Toast.makeText(MainActivity.this, "Bluetooth is already enabled ", Toast.LENGTH_LONG).show();
//
//                }
//                Intent act2 = new Intent(MainActivity.this , MainActivity2.class);
//                startActivity(act2);
//            }
//
//        });
//
//    }
//
//




    private void checkPermission() {

        // Runtime permission ------------
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) // check runtime permission for BODY_SENSORS
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.BODY_SENSORS}, 1); // If BODY_SENSORS permission has not been taken before then ask for the permission with popup
        } else {
            Log.d(TAG, "ALREADY GRANTED"); //if BODY_SENSORS is allowed for this app then print this line in log.
        }




// Check if the app has location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Location permission is already granted, continue with your app logic
            // ...
        }

    }


    private void checkSensorAvailability() {
        SensorManager mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE)); //Step 5: SensorManager Instantiate

        //List of integrated sensor of device---------
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL); //Step 6: List of integrated sensors.
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Sensor sensor : sensors) {

            arrayList.add(sensor.getName()); // put integrated sensor list in arraylist


    }
        Log.d(TAG, ">>>>>>>>>>>>>SUPPORETED Sensors Messegaes DEBUGG  : <<<<<<<<<<<<<<<\n");
    int sensors_counter =0;
        for( String element : arrayList) {
        sensors_counter++;
        Log.d(TAG, element); // print the arraylist in log.
    }
        Log.d(TAG, "number of Sensors listed in the samsnung galaxy watch 5 pro is : "); // print the arraylist in log.
        Log.d(TAG, String.valueOf( sensors_counter)); // print the arraylist in log.


        //check accessibility for 3rd party developers/public-------
        if ((mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)) != null) { //Step 7: Check for particular sensor, if return value then the sensor is accessible
            tv_heartRate.setText(tv_heartRate.getText() + " Accessible"); // Show textView
            tv_heartRate.setTextColor(Color.parseColor("#32cd32")); //text color of textView
        } else { //Step 6: If return null, then sensor is not accessible and entered into else method.
            tv_heartRate.setText(tv_heartRate.getText() + " Inaccessible");
            tv_heartRate.setTextColor(Color.parseColor("#FF0000")); // textColor
        }

        if ((mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)) != null) { // step 7
            tv_pressure.setText(tv_pressure.getText() + " Accessible");
            tv_pressure.setTextColor(Color.parseColor("#32cd32"));
        } else {
            tv_pressure.setText(tv_pressure.getText() + " Inaccessible");
            tv_pressure.setTextColor(Color.parseColor("#454B1B"));
        }



//        if ((mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)) != null) { // step 7
//            Log.d(TAG, "TYPE_HEART_BEAT____ available");
//            tv_Gyroscope.setText(tv_Gyroscope.getText() + " Accessible");
//            tv_Gyroscope.setTextColor(Color.parseColor("#32cd32"));
//        } else {
//            tv_Gyroscope.setText(tv_Gyroscope.getText() + " Inaccessible");
//            tv_Gyroscope.setTextColor(Color.parseColor("#FF0000"));
//        }
//
//        if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) != null) { // step 7
//            Log.d(TAG, "TYPE_STEP_COUNTER____ available");
//            tv_Accelerometer.setText(tv_Accelerometer.getText() + " Accessible");
//            tv_Accelerometer.setTextColor(Color.parseColor("#32cd32"));
//        } else {
//            tv_Accelerometer.setText(tv_Accelerometer.getText() + " Inaccessible");
//            tv_Accelerometer.setTextColor(Color.parseColor("#FF0000"));
//        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
