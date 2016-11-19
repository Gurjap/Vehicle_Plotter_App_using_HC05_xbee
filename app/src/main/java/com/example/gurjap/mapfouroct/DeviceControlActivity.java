package com.example.gurjap.mapfouroct;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
@SuppressWarnings("ALL")
public final class DeviceControlActivity extends BaseActivity implements OnMapReadyCallback,LocationListener {


    private static final String DEVICE_NAME = "DEVICE_NAME";
    static private GoogleMap mMap;
    static double mainLongitude=0;
    static double mainLatitude=0;
    private LocationManager locationManager;
    private static String MSG_NOT_CONNECTED;
    private static String MSG_CONNECTING;
    private static String MSG_CONNECTED;

    private static DeviceConnector connector;
    private static BluetoothResponseHandler mHandler;


    private String deviceName;
    private  Broadcasr_recievre receiver;
   static String tobesend;
   static Marker mymarker,othermaker;
    static DeviceControlActivity a=new DeviceControlActivity();

    @Override

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        PreferenceManager.setDefaultValues(this, R.xml.settings_activity, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        if (mHandler == null) mHandler = new BluetoothResponseHandler(this);
        else mHandler.setTarget(this);

        MSG_NOT_CONNECTED = getString(R.string.msg_not_connected);
        MSG_CONNECTING = getString(R.string.msg_connecting);
        MSG_CONNECTED = getString(R.string.msg_connected);


        if (isConnected() && (savedInstanceState != null)) {
            setDeviceName(savedInstanceState.getString(DEVICE_NAME));
        } else {
       getSupportActionBar().setSubtitle(MSG_NOT_CONNECTED);
        }



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,2000,0,this);
        Intent mservice=new Intent(DeviceControlActivity.this,Background_service.class);
        startService(mservice);
        IntentFilter filter = new IntentFilter(receiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new Broadcasr_recievre();
        registerReceiver(receiver, filter);

    }

    @Override
    public void onLocationChanged(Location location) {

         mainLatitude=location.getLatitude();
         mainLongitude=location.getLongitude();
         mupdatemaps(mainLatitude,mainLongitude, Id.myname);
        sendCommand(null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }

    public class Broadcasr_recievre extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.example.gurjap.mapfouroct.intent.action.PROCESS_RESPONSE";
        @Override
        public void onReceive(Context context, Intent intent) {
         //   DeviceControlActivity a= new DeviceControlActivity();
            DeviceControlActivity.tobesend="123";
            sendCommand(null);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
    public void mupdatemaps(double Latitute, double Longitude, String id){

        LatLng location=new LatLng(Latitute,Longitude);
        if(mymarker!=null)mymarker.remove();
        mymarker= mMap.addMarker(new MarkerOptions().position(location).title(id));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
    }
    public void oupdatemaps(double Latitute, double Longitude, String id){

        LatLng location=new LatLng(Latitute,Longitude);
        if(othermaker!=null)othermaker.remove();
        othermaker= mMap.addMarker(new MarkerOptions().position(location).title(Id.othername));
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEVICE_NAME, deviceName);
    }

    private boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }

    private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;
            deviceName = null;
        }
    }

    private void startDeviceListActivity() {
        stopConnection();
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    @Override

    public boolean onSearchRequested() {
        if (super.isAdapterReady()) startDeviceListActivity();
        return false;
    }
    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control_activity, menu);
        final MenuItem bluetooth = menu.findItem(R.id.menu_search);
        if (bluetooth != null) bluetooth.setIcon(this.isConnected() ?
                R.drawable.ic_action_device_bluetooth_connected :
                R.drawable.ic_action_device_bluetooth);
        return true;
    }
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_search:
                if (super.isAdapterReady()) {
                    if (isConnected()) stopConnection();
                    else startDeviceListActivity();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                return true;

            case R.id.menu_about:
                final Intent intent1 = new Intent(this, About.class);
                startActivity(intent1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override

    public void onStart() {
        super.onStart();
 }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    if (super.isAdapterReady() && (connector == null)) setupConnector(device);
                }
                break;
            case REQUEST_ENABLE_BT:
               super.pendingRequestEnableBt = false;
                break;
        }
    }

    private void setupConnector(BluetoothDevice connectedDevice) {
        stopConnection();
        try {
            String emptyName = getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, mHandler);
            connector.connect();
        } catch (IllegalArgumentException e) {
        }
    }

    public void sendCommand(View view) {
            String commandString;
            DecimalFormat df = new DecimalFormat("##0.0000000");
            commandString= "T"+df.format(mainLatitude)+"G"+df.format(mainLongitude)+"I"+Id.your_id;
            byte[] command = commandString.getBytes();
            if (isConnected()) {
                connector.write(command);
            }
        }
  void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        getSupportActionBar().setSubtitle(deviceName);
    }

    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<DeviceControlActivity> mActivity;

        public BluetoothResponseHandler(DeviceControlActivity activity) {
            mActivity = new WeakReference<DeviceControlActivity>(activity);
        }

        public void setTarget(DeviceControlActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<DeviceControlActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceControlActivity activity = mActivity.get();
           if (activity != null)
            {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        final android.support.v7.app.ActionBar bar = activity.getSupportActionBar();
                        switch (msg.arg1) {
                            case DeviceConnector.STATE_CONNECTED:
                                bar.setSubtitle(MSG_CONNECTED);
                                break;
                            case DeviceConnector.STATE_CONNECTING:
                                bar.setSubtitle(MSG_CONNECTING);
                                break;
                            case DeviceConnector.STATE_NONE:
                                bar.setSubtitle(MSG_NOT_CONNECTED);
                                break;
                        }
                        activity.invalidateOptionsMenu();
                        break;

                    case MESSAGE_READ:
                        final String readMessage = (String) msg.obj;
                        if (readMessage != null) {
                            Otheruserupdates(readMessage);
                        }
                        break;

                    case MESSAGE_DEVICE_NAME:
                        activity.setDeviceName((String) msg.obj);
                        break;

                 }
            }
        }

        private void Otheruserupdates(String msg) {

           try{
                if (msg.substring(0, 1).equals("T")&&msg.substring(11, 12).equals("G")&&msg.substring(22, 23).equals("I")) {
                    double lt = Double.parseDouble(msg.substring(1, 11));
                    double lg = Double.parseDouble(msg.substring(12, 22));
                    String id = msg.substring(23, 27);
                    Log.d("check","lt"+lt+"ln"+lg+"id"+id);
                    a.oupdatemaps(lt,lg,id);

                 }
                else {
                }
            }
            catch (Exception a){
          }
        }
    }
}