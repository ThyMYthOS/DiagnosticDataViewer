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
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;


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


    private final static String TAG = MainActivity.class.getSimpleName();

    private boolean mConnected;

    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                Toast.makeText(MainActivity.this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT);
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            SharedPreferences settings = getSharedPreferences(SETTINGS_BLUETOOTH, MODE_PRIVATE);
            String deviceAddress = settings.getString(SETTING_DEVICE_ADDRESS, "");
            if (deviceAddress.equals("")) {
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
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
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

        public void displayLogTime() {
            if (!resetTime) { // reset log timer
                resetTime = true;
                logStart = System.currentTimeMillis();
            }
            int logDuration = (int) (System.currentTimeMillis() - logStart) / 1000;
            if (logDuration - logLastTime > 0) {
                TextView Message_data = findViewById(R.id.label_Message_data);

                String logTimeFormat = String.format(Locale.US, " %02d:%02d:%02d",
                        (logDuration % 86400) / 3600, (logDuration % 3600) / 60, (logDuration % 60));

                Message_data.setText(getString(R.string.LogTime_label, logTimeFormat));
                logLastTime = logDuration;
            }
        }

        private void displayData(String data) {
            Resources res = getResources();
            String[] separated = data.split(",");
            Integer dataType = Integer.valueOf(separated[0]);
            Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

            // TODO: Use different characteristics instead of multiplexing the data into one
            switch (dataType) {
                case 1:
                    TextView RPM_data = findViewById(R.id.label_RPM_data);
                    if (RPM_data != null) RPM_data.setText(separated[1]);

                    if (fragment instanceof LiveDataFragment) {
                        LiveDataFragment dataFragment = (LiveDataFragment) fragment;
                        dataFragment.setRPM(Float.valueOf(separated[1]));
                    }
                    displayLogTime();
                    break;
                case 2:
                    TextView TPS_data = findViewById(R.id.label_TPS_data);
                    if (TPS_data != null) TPS_data.setText(separated[1]);

                    if (fragment instanceof LiveDataFragment) {
                        LiveDataFragment dataFragment = (LiveDataFragment) fragment;
                        dataFragment.setTPS(Float.valueOf(separated[1]));
                    }
                    break;
                case 3:
                    float afr1Value = Float.valueOf(separated[1]);
                    TextView AFR1_data = findViewById(R.id.label_AFR1_data);
                    if (AFR1_data != null) {
                        AFR1_data.setText(separated[1]);
                        if (afr1Value < AFR_LOWER_LIMIT) {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRrich));
                        } else if (afr1Value > AFR_UPPER_LIMIT) {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRlean));
                        } else {
                            AFR1_data.setTextColor(res.getColor(R.color.colorAFRok));
                        }
                    }
                    if (fragment instanceof LiveDataFragment) {
                        LiveDataFragment dataFragment = (LiveDataFragment) fragment;
                        dataFragment.setAFR1(afr1Value);
                    }
                    break;
                case 4:
                    float afr2Value = Float.valueOf(separated[1]);
                    TextView AFR2_data = findViewById(R.id.label_AFR2_data);
                    if (AFR2_data != null) {
                        AFR2_data.setText(separated[1]);
                        if (afr2Value < AFR_LOWER_LIMIT) {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRrich));
                        } else if (afr2Value > AFR_UPPER_LIMIT) {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRlean));
                        } else {
                            AFR2_data.setTextColor(res.getColor(R.color.colorAFRok));
                        }
                    }
                    if (fragment instanceof LiveDataFragment) {
                        LiveDataFragment dataFragment = (LiveDataFragment) fragment;
                        dataFragment.setAFR1(afr2Value);
                    }
                    break;
                case 5:
                    TextView N_data = findViewById(R.id.label_N_Data);
                    if (Integer.valueOf(separated[1]) == 1) {
                        N_data.setTextColor(res.getColor(R.color.on));
                    } else {
                        N_data.setTextColor(res.getColor(R.color.off));
                    }
                    break;
                case 6:
                    TextView EBS_data = findViewById(R.id.label_EBS_data);
                    if (Integer.valueOf(separated[1]) == 1) {
                        EBS_data.setTextColor(res.getColor(R.color.on));
                    } else {
                        EBS_data.setTextColor(res.getColor(R.color.off));
                    }
                    break;
                case 7:
                    if (fragment instanceof LiveDataFragment) {
                        LiveDataFragment dataFragment = (LiveDataFragment) fragment;
                        if (centigrade) {
                            dataFragment.setCoolantTemp(separated[1] + "°C");
                        } else {
                            dataFragment.setCoolantTemp(separated[1] + "°F");
                        }
                    } else {
                        TextView CTS_data = findViewById(R.id.label_CTS_data);
                        CTS_data.setText(separated[1]);
                    }
                    break;
                case 8:
                    TextView IAT_data = findViewById(R.id.label_IAT_data);
                    IAT_data.setText(separated[1]);
                    break;
                case 9:
                    TextView ADV_data = findViewById(R.id.label_ADV_data);
                    ADV_data.setText(separated[1]);
                    break;
                case 11:
                    TextView INJ1_data = findViewById(R.id.label_INJ1_data);
                    INJ1_data.setText(separated[1]);
                    break;
                case 13:
                    TextView INJ2_data = findViewById(R.id.label_INJ2_data);
                    INJ2_data.setText(separated[1]);
                    break;
                case 14:
                    TextView AP_data = findViewById(R.id.label_AP_data);
                    AP_data.setText(separated[1]);
                    break;
                case 15:
                    TextView BV_data = findViewById(R.id.label_BV_data);
                    BV_data.setText(separated[1]);
                    break;
                case 16://
                    TextView AN3_data = findViewById(R.id.label_AN3_data);
                    AN3_data.setText(separated[1]);
                    break;
                case 17:
                    TextView AN4_data = findViewById(R.id.label_AN4_data);
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
            TextView Message_data = findViewById(R.id.label_Message_data);
            Integer messageType = Integer.valueOf(message);

            switch (messageType) {
                case 1:
                    Message_data.setText(getString(R.string.IGN_ON_message));
                    break;
                case 2:
                    TextView ignOn = findViewById(R.id.label_Ignition_result);
                    ignOn.setText(getString(R.string.ON_message));

                    TextView SDfail = findViewById(R.id.label_SDcard_result);
                    SDfail.setText(getString(R.string.Failed_message));
                    Message_data.setText(getString(R.string.Clear_message)); // Clear message line
                    break;
                case 3:
                    TextView Faults = findViewById(R.id.label_Fault_Codes_result);
                    Faults.setText(getString(R.string.Yes_message));
                    TextView FaultsC = findViewById(R.id.label_Fault_Clear_result);
                    FaultsC.setText(getString(R.string.No_message));
                    break;
                case 4:
                    TextView FaultsC2 = findViewById(R.id.label_Fault_Clear_result);
                    FaultsC2.setText(getString(R.string.Yes_message));
                    break;
                case 5:
                    TextView Config = findViewById(R.id.label_Config_result);
                    Config.setText(getString(R.string.Failed_message));
                    break;
                case 6:
                    Message_data.setText(getString(R.string.Start_ENG_message));
                    break;
                case 7:
                    Message_data.setText(getString(R.string.AFR_Cold_message));
                    TextView AfrCold = findViewById(R.id.label_AFR_result);
                    AfrCold.setText(getString(R.string.Cold_message));
                    break;
                case 8:
                    Message_data.setText(getString(R.string.AFR_NA_message));
                    TextView AfrNA = findViewById(R.id.label_AFR_result);
                    AfrNA.setText(getString(R.string.NA_message));
                    break;
                case 9:
                    Message_data.setText(getString(R.string.AFR_Err_message));
                    TextView AfrErr = findViewById(R.id.label_AFR_result);
                    AfrErr.setText(getString(R.string.Error_message));
                    break;
                case 10:
                    Message_data.setText(getString(R.string.AFR_Ready_message));
                    TextView AfrOK = findViewById(R.id.label_AFR_result);
                    AfrOK.setText(getString(R.string.OK_message));
                    break;
                case 11:
                    Message_data.setText(getString(R.string.Eng_Cold_message));
                    TextView EngineCold = findViewById(R.id.label_Engine_result);
                    EngineCold.setText(getString(R.string.Cold_message));
                    break;
                case 12:
                    Message_data.setText(getString(R.string.Eng_Warm_message));
                    TextView EngineOK = findViewById(R.id.label_Engine_result);
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
                    navigateToFragment(R.id.nav_startup_view);
                    break;
                case 17: // fast logging
                    navigateToFragment(R.id.fastLogging);
                    break;
            }
        }

        private void displayValue(String value, String result) {
            Integer valueData = Integer.valueOf(value);

            switch (valueData) {
                case 1:
                    resetTime = false;
                    logLastTime = 0;
                    TextView ignOn = findViewById(R.id.label_Ignition_result);
                    ignOn.setText(getString(R.string.ON_message));

                    TextView SDpass = findViewById(R.id.label_SDcard_result);
                    SDpass.setText(getString(R.string.OK_message));

                    TextView logNameView = findViewById(R.id.label_Log_result);
                    String[] logName = result.split(".csv");
                    logNameView.setText(logName[0]);
                    break;
                case 2:
                    TextView firm = findViewById(R.id.label_Firmware_result);
                    String firmware = result.replaceAll("\\s", ""); //remove spaces
                    firm.setText(firmware);
                    break;
                case 3:
                    TextView ConfigOK = findViewById(R.id.label_Config_result);
                    ConfigOK.setText(getString(R.string.OK_message));

                    TextView autoSwitch = findViewById(R.id.label_AutoSwitch_result);
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
                    TextView mode = findViewById(R.id.label_Mode_result);
                    Integer modeType = Integer.valueOf(result);

                    if (modeType == 4) {
                        navigateToFragment(R.id.nav_actuator_view);
                    }

                    if (modeType == 1) {
                        mode.setText(getString(R.string.Mode1_message));
                        TextView newAutoSwitch = findViewById(R.id.label_AutoSwitch_result);
                        newAutoSwitch.setText(getString(R.string.AutoSwitch3_message));
                    } else if (modeType == 2) {
                        mode.setText(getString(R.string.Mode2_message));
                    } else if (modeType == 3) {
                        mode.setText(getString(R.string.Mode3_message));
                    }
                    break;
                case 5:
                    TextView motorcycle = findViewById(R.id.label_Motorcycle_result);
                    char m = result.charAt(0);
                    motorcycleModel = ((int) m) - 65;  // 'A':0 to 'L':12
                    motorcycle.setText(getResources().getStringArray(R.array.Motorcycles)[motorcycleModel]);
                    break;
                case 6:
                    centigrade = false;
                    break;
            }
        }

        private void actuatorTest(String test, String result) {
            Integer testData = Integer.valueOf(test);
            Integer resultVal = Integer.valueOf(result);
            TextView textView = null;
            switch (testData) {
                case 0: textView = findViewById(R.id.label_Injector1_result); break;
                case 1: textView = findViewById(R.id.label_Injector2_result); break;
                case 2: textView = findViewById(R.id.label_Coil1_result); break;
                case 3: textView = findViewById(R.id.label_Coil2_result); break;
                case 4: textView = findViewById(R.id.label_FuelPump_result); break;
                case 5: textView = findViewById(R.id.label_Tachometer_Result); break;
            }
            if (textView == null) return;
            if (resultVal == 0) {
                textView.setText(getString(R.string.Testing_message));
            } else if (resultVal == 1) {
                textView.setText(getString(R.string.Passed_message));
            } else
                textView.setText(getString(R.string.Failed_message));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.info_title);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigateToFragment(R.id.nav_startup_view);

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
            if (!deviceAddress.equals("")) {
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            navigateToFragment(R.id.nav_startup_view);
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
                editor.remove(SETTING_DEVICE_ADDRESS).apply();
                mBluetoothLeService.disconnect();
                break;
            case R.id.menu_settings:
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        navigateToFragment(item.getItemId());
        return true;
    }

    private void navigateToFragment(int menuId) {
        int title = 0;
        Fragment fragment = null;
        if (menuId == R.id.nav_startup_view) {
            fragment = StartupFragment.newInstance();
            title = R.string.info_title;
        } else if (menuId == R.id.nav_idle_view) {
            fragment = IdleFragment.newInstance();
            title = R.string.idle_title;
        } else if (menuId == R.id.nav_afr_view) {
            fragment = AFRFragment.newInstance();
            title = R.string.afr_title;
        } else if (menuId == R.id.nav_monitor_view) {
            fragment = MonitorFragment.newInstance();
            title = R.string.monitor_title;
        } else if (menuId == R.id.nav_graph_view) {
            fragment = new GraphFragment();
            title = R.string.graph_title;
        } else if (menuId == R.id.nav_table_view) {
            Bundle bundle = new Bundle();
            // TODO Select IDs depending on current motorcycle
            bundle.putInt(TableFragment.ARG_RPM_MOT_ID, R.array.rpm_mot_A);
            bundle.putInt(TableFragment.ARG_TPS_MOT_ID, R.array.tps_mot_A);
            fragment = TableFragment.newInstance();
            fragment.setArguments(bundle);
            title = R.string.table_title;
        } else if (menuId == R.id.nav_actuator_view) {
            fragment = ActuatorTestFragment.newInstance();
            title = R.string.actuator_title;
        } else if (menuId == R.id.fastLogging) {
            fragment = FastLoggingFragment.newInstance();
            title = R.string.fast_title;
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem menuItem = navigationView.getMenu().findItem(menuId);
        if (menuItem != null) menuItem.setChecked(true);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
        if (fragment != null) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

}
