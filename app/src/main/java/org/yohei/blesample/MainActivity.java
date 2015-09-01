package org.yohei.blesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.yohei.blesample.scan.BeaconScanCallback;
import org.yohei.blesample.utils.Logger;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 123;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning = false;
    private BeaconScanCallback mScanCallback;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Logger.debug("no has ble");
            Toast.makeText(this, "フハハハ'`,､('∀`) '`,､", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Logger.debug("cant get adapter.");
            Toast.makeText(this, "フハハハ'`,､('∀`) '`,､", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mScanCallback = new BeaconScanCallback();

        Logger.debug("start scanning.");
        mScanning = false;
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        BluetoothAdapter.getDefaultAdapter().startLeScan(this);
        BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(mScanCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this);
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Logger.debug(" -- device:" + device);
        Logger.debug(" --rssi:" + rssi);
        Logger.debug(" -- record:" + scanRecord);
        readRecord(scanRecord);
    }

    void readRecord(byte[] scanRecord) {
        Logger.debug(" -- byte length:" + scanRecord.length);
        if (scanRecord.length > 30) {
            //iBeacon の場合 6 byte 目から、 9 byte 目はこの値に固定されている。
            if ((scanRecord[5] == (byte) 0x4c) && (scanRecord[6] == (byte) 0x00) &&
                    (scanRecord[7] == (byte) 0x02) && (scanRecord[8] == (byte) 0x15)) {
                // UUID 128bit(16byte)
                String uuid = IntToHex2(scanRecord[9] & 0xff)
                        + IntToHex2(scanRecord[10] & 0xff)
                        + IntToHex2(scanRecord[11] & 0xff)
                        + IntToHex2(scanRecord[12] & 0xff)
                        + "-"
                        + IntToHex2(scanRecord[13] & 0xff)
                        + IntToHex2(scanRecord[14] & 0xff)
                        + "-"
                        + IntToHex2(scanRecord[15] & 0xff)
                        + IntToHex2(scanRecord[16] & 0xff)
                        + "-"
                        + IntToHex2(scanRecord[17] & 0xff)
                        + IntToHex2(scanRecord[18] & 0xff)
                        + "-"
                        + IntToHex2(scanRecord[19] & 0xff)
                        + IntToHex2(scanRecord[20] & 0xff)
                        + IntToHex2(scanRecord[21] & 0xff)
                        + IntToHex2(scanRecord[22] & 0xff)
                        + IntToHex2(scanRecord[23] & 0xff)
                        + IntToHex2(scanRecord[24] & 0xff);
                // 16bit(2byte) -> 0-65535
                String major = IntToHex2(scanRecord[25] & 0xff) + IntToHex2(scanRecord[26] & 0xff);
                // 16bit(2byte) -> 0-65535
                String minor = IntToHex2(scanRecord[27] & 0xff) + IntToHex2(scanRecord[28] & 0xff);
                Logger.debug(" -- uuid:" + uuid);
                Logger.debug(" -- major:" + major);
                Logger.debug(" -- major:" + (scanRecord[25] + scanRecord[26]));
                Logger.debug(" -- minor:" + minor);
                Logger.debug(" -- minor:" + (scanRecord[27] + scanRecord[28]));
                Logger.debug(" -- tx:" + scanRecord[29]);
//                for (int i = 0; i < scanRecord.length; i++) {
//                    Logger.debug("" + i + " -> " + IntToHex2(scanRecord[i] & 0xff) + " / 10->" + scanRecord[i]);
//                }
            }
        }
    }
//intデータを 2桁16進数に変換するメソッド

    public String IntToHex2(int i) {
        char hex_2[] = {Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16)};
        String hex_2_str = new String(hex_2);
        return hex_2_str.toUpperCase();
    }

}
