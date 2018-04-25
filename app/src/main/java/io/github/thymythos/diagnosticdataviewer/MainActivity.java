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
import android.content.res.Resources;
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

import java.util.UUID;

import de.nitri.gauge.Gauge;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static String SETTINGS_BLUETOOTH = "BLUETOOTH";
    public final static String SETTING_DEVICE_ADDRESS = "SETTING_DEVICE_ADDRESS";

    public static final float AFR_LOWER_LIMIT = 13.0f;
    public static final float AFR_UPPER_LIMIT = 13.6f;

    public static long logStart = System.currentTimeMillis();
    public static int logLastTime;
    public static int motorcycleModel;
    public static boolean resetTime;
    public static boolean centigrade = true;
    public static boolean idleView;
    public static boolean afrView;


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

        public void displayLogTime() {
            if (!resetTime) { // reset log timer
                resetTime = true;
                logStart = System.currentTimeMillis();
            }
            int logDuration = (int) (System.currentTimeMillis() - logStart) / 1000;
            if (logDuration - logLastTime > 0) {
                TextView Message_data = (TextView) (findViewById(R.id.label_Message_data));

                String logTimeFormat = String.format(" %02d:%02d:%02d",
                        (logDuration % 86400) / 3600, (logDuration % 3600) / 60, (logDuration % 60));

                Message_data.setText(getString(R.string.LogTime_label) + logTimeFormat);
                logLastTime = logDuration;
            }
        }

        private void displayData(String data) {
            Resources res = getResources();
            String[] separated = data.split(",");
            Integer dataType = Integer.valueOf(separated[0]);

            // TODO: Use different characteristics instead of multiplexing the data into one
            switch (dataType) {
                case 1:
                    if (idleView) { // start log timer on 1st loop
                        Gauge gauge1 = findViewById(R.id.gauge1);
                        gauge1.setDeltaTimeInterval(1);
                        gauge1.setLowerText(separated[1]);
                        float value = Float.valueOf(separated[1]);
                        if (value > 2000) value = 2000;
                        gauge1.moveToValue(value);
                    } else {
                        TextView RPM_data = (TextView) (findViewById(R.id.label_RPM_data));
                        RPM_data.setText(separated[1]);
                    }
                    displayLogTime();
                    break;
                case 2:
                    if (idleView) {
                        Gauge gauge2 = findViewById(R.id.gauge2);
                        gauge2.setDeltaTimeInterval(1);
                        gauge2.setLowerText(separated[1]);
                        float value = Float.valueOf(separated[1]);
                        if (value < 1) value = 1;
                        if (value > 3) value = 3;
                        gauge2.moveToValue(value);
                    } else {
                        TextView TPS_data = (TextView) (findViewById(R.id.label_TPS_data));
                        TPS_data.setText(separated[1]);
                    }
                    break;
                case 3:
                    float afr1Value = Float.valueOf(separated[1]);
                    if ((idleView || afrView) && afr1Value > 0) {
                        Gauge gauge3 = findViewById(R.id.gauge3);
                        gauge3.setDeltaTimeInterval(1);
                        gauge3.setLowerText(separated[1]);
                        if (afr1Value < 11) afr1Value = 11;
                        if (afr1Value > 15) afr1Value = 15;
                        gauge3.moveToValue(afr1Value);
                    } else {
                        TextView AFR1_data = (TextView) (findViewById(R.id.label_AFR1_data));
                        AFR1_data.setText(separated[1]);
                        Double AFR1d = Double.parseDouble(separated[1]);
                        if (AFR1d < AFR_LOWER_LIMIT) {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRrich));
                        } else if (AFR1d > AFR_UPPER_LIMIT) {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRlean));
                        } else {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRok));
                        }
                    }
                    break;
                case 4:
                    float afr2Value = Float.valueOf(separated[1]);
                    if ((idleView || afrView) && afr2Value > 0) {
                        Gauge gauge4 = findViewById(R.id.gauge4);
                        gauge4.setDeltaTimeInterval(1);
                        gauge4.setLowerText(separated[1]);
                        if (afr2Value < 11) afr2Value = 11;
                        if (afr2Value > 15) afr2Value = 15;
                        gauge4.moveToValue(afr2Value);
                    } else {
                        TextView AFR2_data = (TextView) (findViewById(R.id.label_AFR2_data));
                        AFR2_data.setText(separated[1]);
                        Double AFR2d = Double.parseDouble(separated[1]);
                        if (AFR2d < AFR_LOWER_LIMIT) {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRrich));
                        } else if (AFR2d > AFR_UPPER_LIMIT) {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRlean));
                        } else {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRok));
                        }
                    }
                    break;
                case 5:
                    TextView N_data = (TextView) (findViewById(R.id.label_N_Data));
                    if (Integer.valueOf(separated[1]) == 1) {
                        N_data.setTextColor(res.getColor(R.color.good));
                    } else {
                        N_data.setTextColor(res.getColor(R.color.bad));
                    }
                    break;
                case 6:
                    TextView EBS_data = (TextView) (findViewById(R.id.label_EBS_data));
                    if (Integer.valueOf(separated[1]) == 1) {
                        EBS_data.setTextColor(res.getColor(R.color.good));
                    } else {
                        EBS_data.setTextColor(res.getColor(R.color.bad));
                    }
                    break;
                case 7:
                    if (idleView) { // Display coolant temp in RPM dial
                        Gauge gauge1 = findViewById(R.id.gauge1);
                        if (centigrade == true) {
                            gauge1.setUpperText(separated[1] + "°C");
                        } else {
                            gauge1.setUpperText(separated[1] + "°F");
                        }
                    } else {
                        TextView CTS_data = (TextView) (findViewById(R.id.label_CTS_data));
                        CTS_data.setText(separated[1]);
                    }
                    break;
                case 8:
                    TextView IAT_data = (TextView) (findViewById(R.id.label_IAT_data));
                    IAT_data.setText(separated[1]);
                    break;
                case 9:
                    TextView ADV_data = (TextView) (findViewById(R.id.label_ADV_data));
                    ADV_data.setText(separated[1]);
                    break;
                case 11:
                    TextView INJ1_data = (TextView) (findViewById(R.id.label_INJ1_data));
                    INJ1_data.setText(separated[1]);
                    break;
                case 13:
                    TextView INJ2_data = (TextView) (findViewById(R.id.label_INJ2_data));
                    INJ2_data.setText(separated[1]);
                    break;
                case 14:
                    TextView AP_data = (TextView) (findViewById(R.id.label_AP_data));
                    AP_data.setText(separated[1]);
                    break;
                case 15:
                    TextView BV_data = (TextView) (findViewById(R.id.label_BV_data));
                    BV_data.setText(separated[1]);
                    break;
                case 16://
                    TextView AN3_data = (TextView) (findViewById(R.id.label_AN3_data));
                    AN3_data.setText(separated[1]);
                    break;
                case 17:
                    TextView AN4_data = (TextView) (findViewById(R.id.label_AN4_data));
                    AN4_data.setText(separated[1]);
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
                    Message_data.setText(getString(R.string.IGN_ON_message));
                    break;
                case 2:
                    TextView ignOn = (TextView) (findViewById(R.id.label_Ignition_result));
                    ignOn.setText(getString(R.string.ON_message));

                    TextView SDfail = (TextView) (findViewById(R.id.label_SDcard_result));
                    SDfail.setText(getString(R.string.Failed_message));
                    Message_data.setText(getString(R.string.Clear_message)); // Clear message line
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
                case 16: // back to info screen
                    Fragment fragment = null;
                    fragment = StartupFragment.newInstance();
                    getSupportActionBar().setTitle(R.string.info_title);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment)
                            .commit();
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.getMenu().getItem(0).setChecked(true);
                    break;
                case 17: // fast logging
                    Fragment fragment1 = null;
                    fragment1 = FastLoggingFragment.newInstance();
                    getSupportActionBar().setTitle(R.string.fast_title);
                    FragmentManager fragmentManager1 = getFragmentManager();
                    fragmentManager1.beginTransaction()
                            .replace(R.id.content_frame, fragment1)
                            .commit();
                    break;
            }
        }

        private void displayValue(String value, String result) {
            Integer valueData = Integer.valueOf(value);

            switch (valueData) {
                case 1:
                    resetTime = false;
                    logLastTime = 0;
                    TextView ignOn = (TextView) (findViewById(R.id.label_Ignition_result));
                    ignOn.setText(getString(R.string.ON_message));

                    TextView SDpass = (TextView) (findViewById(R.id.label_SDcard_result));
                    SDpass.setText(getString(R.string.OK_message));

                    TextView logNameView = (TextView) (findViewById(R.id.label_Log_result));
                    String[] logName = result.split(".csv");
                    logNameView.setText(logName[0]);
                    break;
                case 2:
                    TextView firm = (TextView) (findViewById(R.id.label_Firmware_result));
                    String firmware = result.replaceAll("\\s", ""); //remove spaces
                    firm.setText(firmware);
                    break;
                case 3:
                    TextView ConfigOK = (TextView) (findViewById(R.id.label_Config_result));
                    ConfigOK.setText(getString(R.string.OK_message));

                    TextView autoSwitch = (TextView) (findViewById(R.id.label_AutoSwitch_result));
                    Integer autoSwitchType = Integer.valueOf(result);
                    if (autoSwitchType == 0) {
                        autoSwitch.setText(getString(R.string.AutoSwitch0_message));
                    } else if (autoSwitchType == 1) {
                        autoSwitch.setText(getString(R.string.AutoSwitch1_message));
                    } else if (autoSwitchType == 2) {
                        autoSwitch.setText(getString(R.string.AutoSwitch2_message));
                    }
                    break;
                case 4:
                    TextView mode = (TextView) (findViewById(R.id.label_Mode_result));
                    Integer modeType = Integer.valueOf(result);

                    if (modeType == 4) {
                        // Insert the fragment by replacing any existing fragment
                        Fragment fragment = null;
                        fragment = ActuatorTestFragment.newInstance();
                        getSupportActionBar().setTitle(R.string.actuator_title);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .commit();
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        navigationView.getMenu().getItem(5).setChecked(true);
                    }

                    if (modeType == 1) {
                        mode.setText(getString(R.string.Mode1_message));
                        TextView newAutoSwitch = (TextView) (findViewById(R.id.label_AutoSwitch_result));
                        newAutoSwitch.setText(getString(R.string.AutoSwitch3_message));
                    } else if (modeType == 2) {
                        mode.setText(getString(R.string.Mode2_message));
                    } else if (modeType == 3) {
                        mode.setText(getString(R.string.Mode3_message));
                    }
                    break;
                case 5:
                    TextView motorcycle = (TextView) (findViewById(R.id.label_Motorcycle_result));
                    char m = result.charAt(0);
                    motorcycleModel = ((int) m) - 65;  // 0 to 12
                    switch (result) {
                        case "A":
                            motorcycle.setText(getString(R.string.Motorcycle_A));
                            break;
                        case "B":
                            motorcycle.setText(getString(R.string.Motorcycle_B));
                            break;
                        case "C":
                            motorcycle.setText(getString(R.string.Motorcycle_C));
                            break;
                        case "D":
                            motorcycle.setText(getString(R.string.Motorcycle_D));
                            break;
                        case "E":
                            motorcycle.setText(getString(R.string.Motorcycle_E));
                            break;
                        case "F":
                            motorcycle.setText(getString(R.string.Motorcycle_F));
                            break;
                        case "G":
                            motorcycle.setText(getString(R.string.Motorcycle_G));
                            break;
                        case "H":
                            motorcycle.setText(getString(R.string.Motorcycle_H));
                            break;
                        case "I":
                            motorcycle.setText(getString(R.string.Motorcycle_I));
                            break;
                        case "J":
                            motorcycle.setText(getString(R.string.Motorcycle_J));
                            break;
                        case "K":
                            motorcycle.setText(getString(R.string.Motorcycle_K));
                            break;
                        case "L":
                            motorcycle.setText(getString(R.string.Motorcycle_L));
                            break;
                    }
                    break;
                case 6:
                    centigrade = false;
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
        getSupportActionBar().setTitle(R.string.info_title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, new StartupFragment())
                .commit();
        navigationView.getMenu().getItem(0).setChecked(true);

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

            Fragment fragment = null;
            fragment = StartupFragment.newInstance();
            getSupportActionBar().setTitle(R.string.info_title);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        idleView = false;
        afrView = false;
        Fragment fragment = null;
        if (id == R.id.nav_startup_view) {
            fragment = StartupFragment.newInstance();
            getSupportActionBar().setTitle(R.string.info_title);
        } else if (id == R.id.nav_idle_view) {
            fragment = IdleFragment.newInstance();
            getSupportActionBar().setTitle(R.string.idle_title);
            idleView = true;
        } else if (id == R.id.nav_afr_view) {
            fragment = AFRFragment.newInstance();
            getSupportActionBar().setTitle(R.string.afr_title);
            afrView = true;
        } else if (id == R.id.nav_monitor_view) {
            fragment = MonitorFragment.newInstance();
            getSupportActionBar().setTitle(R.string.monitor_title);
        } else if (id == R.id.nav_graph_view) {
            fragment = new GraphFragment();
            getSupportActionBar().setTitle(R.string.graph_title);
        } else if (id == R.id.nav_table_view) {
            fragment = TableFragment.newInstance();
            getSupportActionBar().setTitle(R.string.table_title);
        } else if (id == R.id.nav_actuator_view) {
            fragment = ActuatorTestFragment.newInstance();
            getSupportActionBar().setTitle(R.string.actuator_title);
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
