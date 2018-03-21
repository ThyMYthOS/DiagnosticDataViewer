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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.UUID;


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
                    Log.d(TAG, "Service " + service.getUuid().toString());
                    if (BluetoothLeService.UUID_DataLogger.equals(service.getUuid())) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BluetoothLeService.UUID_Data16m);
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                if (BluetoothLeService.UUID_Data16m.equals(UUID.fromString(uuid))) {
                    try {
                        displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    } catch (Exception e) {
                    }
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
                    TextView Message_data = (TextView) (findViewById(R.id.label_Message_data));
                    Message_data.setText(getString(R.string.Connected_message));

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
                case 96:
                    actuatorTest(separated[1], separated[2]);
                    break;
                case 97:
                    displayValue(separated[1], separated[2]);
                    break;
                case 99:
                    displayMessage(separated[1]);
                    break;
            }
        }

        private void displayMessage(String message) {
            TextView Message_data = (TextView) (findViewById(R.id.label_Message_data));
            Integer messageType = Integer.valueOf(message);

            switch (messageType) {
                case 1:
                    TextView SDfail = (TextView) (findViewById(R.id.label_SDcard_result));
                    SDfail.setText(getString(R.string.Failed_message));
                    Message_data.setText(getString(R.string.Clear_message)); // Clear message line
                    break;
                case 2:
                    TextView SDpass = (TextView) (findViewById(R.id.label_SDcard_result));
                    SDpass.setText(getString(R.string.OK_message));
                    Message_data.setText(getString(R.string.IGN_ON_message));
                    break;
                case 3:
                    TextView Faults = (TextView) (findViewById(R.id.label_Fault_Codes_result));
                    Faults.setText(getString(R.string.Yes_message));
                    TextView FaultsC = (TextView) (findViewById(R.id.label_Fault_Clear_result));
                    FaultsC.setText(getString(R.string.No_message));
                    break;
                case 4:
                    TextView FaultsC2 = (TextView) (findViewById(R.id.label_Fault_Clear_result));
                    FaultsC2.setText(getString(R.string.Yes_message));
                    break;
                case 5:
                    TextView Config = (TextView) (findViewById(R.id.label_Config_result));
                    Config.setText(getString(R.string.Failed_message));
                    break;
                case 6:
                    TextView Config2 = (TextView) (findViewById(R.id.label_Config_result));
                    Config2.setText(getString(R.string.OK_message));
                    Message_data.setText(getString(R.string.Start_ENG_message));
                    break;
                case 7:
                    Message_data.setText(getString(R.string.AFR_Cold_message));
                    TextView AfrCold = (TextView) (findViewById(R.id.label_AFR_result));
                    AfrCold.setText(getString(R.string.Cold_message));
                    break;
                case 8:
                    Message_data.setText(getString(R.string.AFR_NA_message));
                    TextView AfrNA = (TextView) (findViewById(R.id.label_AFR_result));
                    AfrNA.setText(getString(R.string.NA_message));
                    break;
                case 9:
                    Message_data.setText(getString(R.string.AFR_Err_message));
                    TextView AfrErr = (TextView) (findViewById(R.id.label_AFR_result));
                    AfrErr.setText(getString(R.string.Error_message));
                    break;
                case 10:
                    Message_data.setText(getString(R.string.AFR_Ready_message));
                    TextView AfrOK = (TextView) (findViewById(R.id.label_AFR_result));
                    AfrOK.setText(getString(R.string.OK_message));
                    break;
                case 11:
                    Message_data.setText(getString(R.string.Eng_Cold_message));
                    TextView EngineCold = (TextView) (findViewById(R.id.label_Engine_result));
                    EngineCold.setText(getString(R.string.Cold_message));
                    break;
                case 12:
                    Message_data.setText(getString(R.string.Eng_Warm_message));
                    TextView EngineOK = (TextView) (findViewById(R.id.label_Engine_result));
                    EngineOK.setText(getString(R.string.OK_message));
                    break;
                case 13:
                    Message_data.setText(getString(R.string.Clear_message));
                    break;
                case 14:
                    Message_data.setText(getString(R.string.IGN_OFF_message));
                    break;
                case 15:
                    Message_data.setText(getString(R.string.TimeOut_message));
                    break;
                case 16:
                    // Insert the fragment by replacing any existing fragment
                    Fragment fragment = null;
                    fragment = LoggerInfoFragment.newInstance();
                    getSupportActionBar().setTitle(R.string.Clear_message);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                    break;
                case 17:
                    Message_data.setText(getString(R.string.Fast_Logging_message));
                    break;

            }

        }

        private void displayValue(String value, String result) {
            Integer valueData = Integer.valueOf(value);

            switch (valueData) {
                case 1:
                    TextView vcal = (TextView) (findViewById(R.id.label_Calibration_result));
                    vcal.setText(result);
                    break;
                case 2:
                    TextView logNameView = (TextView) (findViewById(R.id.label_Log_result));
                    String[] logName = result.split(".csv");
                    logNameView.setText(String.valueOf(logName[0]));
                    break;
                case 3:
                    TextView firm = (TextView) (findViewById(R.id.label_Firmware_result));
                    String firmware= result.replaceAll("\\s", ""); //remove spaces
                    firm.setText(firmware);
                    break;
                case 4:
                    TextView autoSwitch = (TextView) (findViewById(R.id.label_AutoSwitch_result));
                    Integer autoSwitchType = Integer.valueOf(result);
                    if (autoSwitchType==0) {
                        autoSwitch.setText(getString(R.string.AutoSwitch0_message));
                    } else if (autoSwitchType==1) {
                        autoSwitch.setText(getString(R.string.AutoSwitch1_message));
                    } else if (autoSwitchType==2) {
                        autoSwitch.setText(getString(R.string.AutoSwitch2_message));
                    }

                    break;
                case 5:
                    TextView mode = (TextView) (findViewById(R.id.label_Mode_result));
                    Integer modeType = Integer.valueOf(result);

                    if (modeType==4){
                        // Insert the fragment by replacing any existing fragment
                        Fragment fragment = null;
                        fragment = ActuatorTestFragment.newInstance();
                        getSupportActionBar().setTitle(R.string.Clear_message);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .commit();
                     }

                    if (modeType==1) {
                        mode.setText(getString(R.string.Mode1_message));
                        TextView newAutoSwitch = (TextView) (findViewById(R.id.label_AutoSwitch_result));
                        newAutoSwitch.setText(getString(R.string.AutoSwitch3_message));
                    } else if (modeType==2) {
                        mode.setText(getString(R.string.Mode2_message));
                    } else if (modeType==3) {
                        mode.setText(getString(R.string.Mode3_message));
                    }

                    TextView ignOn = (TextView) (findViewById(R.id.label_Ignition_result));
                    ignOn.setText(getString(R.string.ON_message));
                    break;

            }
        }



        private void actuatorTest(String test, String result) {
            Integer testData = Integer.valueOf(test);
            Integer resultVal = Integer.valueOf(result);

            switch (testData) {
                case 0:
                    TextView inj1Res = (TextView) (findViewById(R.id.label_Injector1_result));
                    if (resultVal == 0) {
                        inj1Res.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            inj1Res.setText(getString(R.string.Passed_message));
                        } else
                            inj1Res.setText(getString(R.string.Failed_message));
                    break;

                case 1:
                    TextView inj2Res = (TextView) (findViewById(R.id.label_Injector2_result));
                    if (resultVal == 0) {
                        inj2Res.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            inj2Res.setText(getString(R.string.Passed_message));
                        } else
                            inj2Res.setText(getString(R.string.Failed_message));
                    break;

                case 2:
                    TextView coil1Res = (TextView) (findViewById(R.id.label_Coil1_result));
                    if (resultVal == 0) {
                        coil1Res.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            coil1Res.setText(getString(R.string.Passed_message));
                        } else
                            coil1Res.setText(getString(R.string.Failed_message));
                    break;

                case 3:
                    TextView coil2Res = (TextView) (findViewById(R.id.label_Coil2_result));
                    if (resultVal == 0) {
                        coil2Res.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            coil2Res.setText(getString(R.string.Passed_message));
                        } else
                            coil2Res.setText(getString(R.string.Failed_message));
                    break;

                case 4:
                    TextView fuelRes = (TextView) (findViewById(R.id.label_FuelPump_result));
                    if (resultVal == 0) {
                        fuelRes.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            fuelRes.setText(getString(R.string.Passed_message));
                        } else
                            fuelRes.setText(getString(R.string.Failed_message));
                    break;

                case 5:
                    TextView tachRes = (TextView) (findViewById(R.id.label_Tachometer_Result));
                    if (resultVal == 0) {
                        tachRes.setText(getString(R.string.Testing_message));
                    } else if (resultVal == 1) {
                            tachRes.setText(getString(R.string.Passed_message));
                        } else
                            tachRes.setText(getString(R.string.Failed_message));

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
        getSupportActionBar().setTitle(R.string.Clear_message);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, new LoggerInfoFragment())
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
        if (id == R.id.nav_info_view) {
            fragment = LoggerInfoFragment.newInstance();
            getSupportActionBar().setTitle(R.string.Clear_message);
        } else if (id == R.id.nav_idle_view) {
                fragment = IdleFragment.newInstance();
                getSupportActionBar().setTitle(R.string.Clear_message);
            } else if (id == R.id.nav_monitor_view) {
            fragment = TextDataItemFragment.newInstance();
             getSupportActionBar().setTitle(R.string.Clear_message);
        } else if (id == R.id.nav_graph_view) {
            fragment = new GraphFragment();
            getSupportActionBar().setTitle(R.string.Clear_message);
        } else if (id == R.id.nav_table_view) {
            fragment = TableFragment.newInstance();
            getSupportActionBar().setTitle(R.string.Clear_message);
        } else if (id == R.id.nav_actuator_view) {
            fragment = ActuatorTestFragment.newInstance();
            getSupportActionBar().setTitle(R.string.Clear_message);
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