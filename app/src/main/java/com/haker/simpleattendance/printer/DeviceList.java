package com.haker.simpleattendance.printer;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.haker.simpleattendance.R;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class DeviceList extends ListActivity {
    private static String TAG = "---DeviceList";
    public static final int REQUEST_COARSE_LOCATION = 200;
    public static final int REQUEST_BLUETOOTH_SCAN = 300;
    public static final int REQUEST_BLUETOOTH_CONNECT = 400;

    static public final int REQUEST_CONNECT_BT = 0 * 2300;
    static private final int REQUEST_ENABLE_BT = 0 * 1000;
    static private BluetoothAdapter mBluetoothAdapter = null;
    static private ArrayAdapter<String> mArrayAdapter = null;

    static private ArrayAdapter<BluetoothDevice> btDevices = null;

    private static final UUID SPP_UUID = UUID
            .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    // UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    static private BluetoothSocket mbtSocket = null;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Bluetooth Devices");

        try {
            if (initDevicesList() != 0) {
                finish();
            }

        } catch (Exception ex) {
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        } else {
            proceedDiscovery();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.S)
    protected void proceedDiscovery() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(mBTReceiver, filter);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN);
        } else {
            mBluetoothAdapter.startDiscovery();
        }
    }

    public static BluetoothSocket getSocket() {
        return mbtSocket;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void flushData() {
        try {
            if (mbtSocket != null) {
                mbtSocket.close();
                mbtSocket = null;
            }

            if (mBluetoothAdapter != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_BLUETOOTH_SCAN);
                } else {
                    mBluetoothAdapter.cancelDiscovery();
                }
            }

            if (btDevices != null) {
                btDevices.clear();
                btDevices = null;
            }

            if (mArrayAdapter != null) {
                mArrayAdapter.clear();
                mArrayAdapter.notifyDataSetChanged();
                mArrayAdapter.notifyDataSetInvalidated();
                mArrayAdapter = null;
            }

            //finalize();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private int initDevicesList() {
        flushData();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth not supported!!", Toast.LENGTH_LONG).show();
            return -1;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN);
        }
        else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        mArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.layout_list);

        setListAdapter(mArrayAdapter);

        Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        try {
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } catch (Exception ex) {

            return -2;
        }

        Toast.makeText(getApplicationContext(),
                        "Getting all available Bluetooth Devices", Toast.LENGTH_SHORT)
                .show();

        return 0;

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent intent) {
        super.onActivityResult(reqCode, resultCode, intent);

        switch (reqCode) {
            case REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_BLUETOOTH_CONNECT
                                );
                    }
                    else {
                        Set<BluetoothDevice> btDeviceList = mBluetoothAdapter
                                .getBondedDevices();
                        try {
                            if (btDeviceList.size() > 0) {

                                for (BluetoothDevice device : btDeviceList) {
                                    if (!btDeviceList.contains(device)) {

                                        btDevices.add(device);

                                        mArrayAdapter.add(device.getName() + "\n"
                                                + device.getAddress());
                                        mArrayAdapter.notifyDataSetInvalidated();
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                }

                break;
        }
        mBluetoothAdapter.startDiscovery();

    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                try {
                    if (btDevices == null) {
                        btDevices = new ArrayAdapter<BluetoothDevice>(
                                getApplicationContext(), R.layout.layout_list);
                    }

                    if (btDevices.getPosition(device) < 0) {
                        btDevices.add(device);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                                    REQUEST_BLUETOOTH_CONNECT
                            );
                        }
                        else {
                            mArrayAdapter.add(device.getName() + "\n"
                                    + device.getAddress() + "\n");
                            mArrayAdapter.notifyDataSetInvalidated();
                        }
                    }
                } catch (Exception ex) {
                    ex.fillInStackTrace();
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onListItemClick(ListView l, View v, final int position,
                                   long id) {
        super.onListItemClick(l, v, position, id);

        if (mBluetoothAdapter == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN);
        }
        else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        Toast.makeText(
                getApplicationContext(),
                "Connecting to " + btDevices.getItem(position).getName() + ","
                        + btDevices.getItem(position).getAddress(),
                Toast.LENGTH_SHORT).show();

        Thread connectThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                                new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_BLUETOOTH_CONNECT
                        );
                    }
                    else {
                        boolean gotuuid = btDevices.getItem(position)
                                .fetchUuidsWithSdp();
                        UUID uuid = btDevices.getItem(position).getUuids()[0]
                                .getUuid();
                        mbtSocket = btDevices.getItem(position)
                                .createRfcommSocketToServiceRecord(uuid);

                        mbtSocket.connect();
                    }
                } catch (IOException ex) {
                    runOnUiThread(socketErrorRunnable);
                    try {
                        mbtSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mbtSocket = null;
                } finally {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            finish();

                        }
                    });
                }
            }
        });

        connectThread.start();
    }

    private Runnable socketErrorRunnable = new Runnable() {

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(),
                    "Cannot establish connection", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                        new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_SCAN);
            }
            else {
                mBluetoothAdapter.startDiscovery();
            }

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceedDiscovery();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Permission is not granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, Menu.FIRST, Menu.NONE, "Refresh Scanning");

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case Menu.FIRST:
                initDevicesList();
                break;
        }

        return true;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        try {
            unregisterReceiver(mBTReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
