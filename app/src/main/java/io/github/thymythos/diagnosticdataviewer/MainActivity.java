package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static String SETTINGS_BLUETOOTH = "BLUETOOTH";
    public final static String SETTING_DEVICE_ADDRESS = "SETTING_DEVICE_ADDRESS";

    private final static String TAG = MainActivity.class.getSimpleName();

    private boolean mConnected;

    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.

            SharedPreferences settings = getSharedPreferences(SETTINGS_BLUETOOTH, MODE_PRIVATE);
            String deviceAddress = settings.getString(SETTING_DEVICE_ADDRESS, "");
            if (deviceAddress != "") {
                final boolean result = mBluetoothLeService.connect(deviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                // TODO: clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                for (BluetoothGattService service : mBluetoothLeService.getSupportedGattServices()) {
                    if (BluetoothLeService.UUID_DataLogger.equals(service.getUuid())) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BluetoothLeService.UUID_Data16m);
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                if (BluetoothLeService.UUID_Data16m.equals(uuid)) {
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        }

        private void updateConnectionState(final int resourceId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Set device name in title or drawer
                }
            });
        }

        private void displayData(String data) {
            String[] separated = data.split(",");
            Integer dataType = Integer.valueOf(separated[0]);

            // TODO: Use different characteristics instead of multiplexing the data into one
            switch (dataType) {
                case 1:
                    TextView RPM_data = (TextView) (findViewById(R.id.label_RPM_data));
                    RPM_data.setText(String.valueOf(separated[1]));
                    break;
                case 2:
                    TextView TPS_data = (TextView) (findViewById(R.id.label_TPS_data));
                    TPS_data.setText(String.valueOf(separated[1]));
                    break;
                case 3:
                    TextView AFR1_data = (TextView) (findViewById(R.id.label_AFR1_data));
                    AFR1_data.setText(String.valueOf(separated[1]));
                    break;
                case 4:
                    TextView AFR2_data = (TextView) (findViewById(R.id.label_AFR2_data));
                    AFR2_data.setText(String.valueOf(separated[1]));
                    break;
                case 5:
                    TextView N_data = (TextView) (findViewById(R.id.label_N_Data));
                    if (Integer.valueOf(separated[1]) == 1) {
                        N_data.setTextColor(Color.parseColor("#009900")); //green
                    } else {
                        N_data.setTextColor(Color.parseColor("#ff0000")); //red
                    }
                    break;
                case 6:
                    TextView EBS_data = (TextView) (findViewById(R.id.label_EBS_data));
                    if (Integer.valueOf(separated[1]) == 1) {
                        EBS_data.setTextColor(Color.parseColor("#009900")); //green
                    } else {
                        EBS_data.setTextColor(Color.parseColor("#ff0000")); //red
                    }
                    break;
                case 7:
                    TextView CTS_data = (TextView) (findViewById(R.id.label_CTS_data));
                    CTS_data.setText(String.valueOf(separated[1]));
                    break;
                case 8:
                    TextView IAT_data = (TextView) (findViewById(R.id.label_IAT_data));
                    IAT_data.setText(String.valueOf(separated[1]));
                    break;
                case 9:
                    TextView ADV_data = (TextView) (findViewById(R.id.label_ADV_data));
                    ADV_data.setText(String.valueOf(separated[1]));
                    break;
                case 11:
                    TextView INJ1_data = (TextView) (findViewById(R.id.label_INJ1_data));
                    INJ1_data.setText(String.valueOf(separated[1]));
                    break;
                case 13:
                    TextView INJ2_data = (TextView) (findViewById(R.id.label_INJ2_data));
                    INJ2_data.setText(String.valueOf(separated[1]));
                    break;
                case 14:
                    TextView AP_data = (TextView) (findViewById(R.id.label_AP_data));
                    AP_data.setText(String.valueOf(separated[1]));
                    break;
                case 15:
                    TextView BV_data = (TextView) (findViewById(R.id.label_BV_data));
                    BV_data.setText(String.valueOf(separated[1]));
                    break;
                case 16://
                    TextView AN3_data = (TextView) (findViewById(R.id.label_AN3_data));
                    AN3_data.setText(String.valueOf(separated[1]));
                    break;
                case 17:
                    TextView AN4_data = (TextView) (findViewById(R.id.label_AN4_data));
                    AN4_data.setText(String.valueOf(separated[1]));
                    break;
                case 99:
                    TextView Message_data = (TextView) (findViewById(R.id.label_Message_data));
                    Message_data.setText(separated[1]);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.text_view);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, new GraphFragment())
                .commit();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        registerReceiver(mGattUpdateReceiver, intentFilter);

        if (mBluetoothLeService != null) {
            SharedPreferences settings = getSharedPreferences(SETTINGS_BLUETOOTH, MODE_PRIVATE);
            String deviceAddress = settings.getString(SETTING_DEVICE_ADDRESS, "");
            if (deviceAddress != "") {
                final boolean result = mBluetoothLeService.connect(deviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                final Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_disconnect:
                SharedPreferences settings = getSharedPreferences(SETTINGS_BLUETOOTH, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove(SETTING_DEVICE_ADDRESS);
                editor.commit();
                mBluetoothLeService.disconnect();
                break;
            case R.id.menu_settings:
                break;
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        if (id == R.id.nav_text_view) {
            fragment = TextDataItemFragment.newInstance();
//            getActionBar().setTitle(R.string.text_view);
        } else if (id == R.id.nav_graph_view) {
            fragment = new GraphFragment();
//            getActionBar().setTitle(R.string.graph_view);
        } else if (id == R.id.nav_table_view) {
            fragment = TableFragment.newInstance();
//            getActionBar().setTitle(R.string.table_view);
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {

        }

        if (fragment != null) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
