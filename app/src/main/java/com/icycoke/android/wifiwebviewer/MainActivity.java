package com.icycoke.android.wifiwebviewer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ScanResDialog.ScanResDialogListener,
        PasswordEnterDialog.PasswordEnterDialogListener,
        ExploreFragment.PageStartListener {

    private static final int LOCATION_REQUEST_CODE = 1;

    private static final String TAG = "MainActivity";

    private static final String PROTOCOL_TYPE_WPA = "WPA";
    private static final String PROTOCOL_TYPE_WEP = "WEP";
    private static final String PROTOCOL_TYPE_WPA2 = "WPA2";

    private WifiManager wifiManager;
    private FragmentManager fragmentManager;

    private Fragment exploreFragment;
    private Fragment shareFragment;

    private BottomNavigationView bottomNavigationView;

    public void wifiConnectOnClick(MenuItem menuItem) {
        scanWifi();
    }

    @Override
    public void connectTo(ScanResult target) {
        if (target == null) {
            Log.d(TAG, "connectTo: target is null!");
            return;
        }
        Log.d(TAG, "connectTo: trying to connect to the target...");

        DialogFragment passwordEnterDialog = PasswordEnterDialog.newInstance(target);
        passwordEnterDialog.show(fragmentManager, "PasswordEnterDialog");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: on click!");
        return true;
    }

    @Override
    public void connectWithPassword(ScanResult target, String password) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + target.SSID + "\"";

        String type = target.capabilities;
        if (type.contains(PROTOCOL_TYPE_WEP)) {
            conf.wepKeys[0] = "\"" + password + "\"";
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (type.contains(PROTOCOL_TYPE_WPA) || type.contains(PROTOCOL_TYPE_WPA2)) {
            conf.preSharedKey = "\"" + password + "\"";
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        } else {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        Log.d(TAG, "connectWithPassword: disconnected");
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        Log.d(TAG, "connectWithPassword: reconnected");
    }

    @Override
    public void setShareContent(String url) {
        ((ShareFragment) shareFragment).setShareContent(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        fragmentManager = getSupportFragmentManager();

        exploreFragment = new ExploreFragment();
        shareFragment = new ShareFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, exploreFragment)
                .add(R.id.fragment_container, shareFragment)
                .show(exploreFragment)
                .hide(shareFragment)
                .commit();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "onNavigationItemSelected: item selected");
                switchFragment(item.getItemId());
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shareFragment.onActivityResult(requestCode, resultCode, data);
    }

    private void switchFragment(int code) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (exploreFragment != null) {
            fragmentTransaction.hide(exploreFragment);
        }
        if (shareFragment != null) {
            fragmentTransaction.hide(shareFragment);
        }

        switch (code) {
            case R.id.nav_explore:
                fragmentTransaction.show(exploreFragment);
                break;
            case R.id.nav_favourite:
                fragmentTransaction.show(shareFragment);
                break;
        }

        fragmentTransaction.commit();
    }

    private void scanWifi() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }

        boolean success = wifiManager.startScan();
        if (!success) {
            Log.d(TAG, "scanWifi: wifi scan failed!");
        } else {
            List<ScanResult> results = wifiManager.getScanResults();
            DialogFragment scanResFragment = ScanResDialog.newInstance(results);
            scanResFragment.show(fragmentManager, "ScanResDialog");
        }
    }
}