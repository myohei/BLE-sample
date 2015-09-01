package org.yohei.blesample.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import org.yohei.blesample.utils.Logger;

import java.util.List;

/**
 * Created by yohei on 8/22/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BeaconScanCallback extends ScanCallback {
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        Logger.debug("onBatchScanResults: ");
        for (ScanResult result : results) {
            Logger.debug("result device:" + result.getDevice());
            Logger.debug("result rssi:" + result.getRssi());
            Logger.debug("result scanRecord:" + result.getScanRecord());
            Logger.debug("========================");
        }
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Logger.debug("onScanFailed -> errorCode:" + errorCode);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        Logger.debug("onScanResult -> callbackType: " + callbackType);
        Logger.debug("RSSI:" + result.getRssi()); // 電波強度
        BluetoothDevice device = result.getDevice();
        if (device != null) {
            Logger.debug("device address:" + device.getAddress());
        }

        ScanRecord record = result.getScanRecord();
        if (record != null) {
            Logger.debug("record:" + record);
            Logger.debug("record device name:" + record.getDeviceName());
            Logger.debug("record tx:" + record.getTxPowerLevel()); // tx パワー
            Logger.debug("distance:" + calculateAccuracy(-61, result.getRssi()));
//            Logger.debug("record byte:" + new String(record.getBytes()));
            readRecord(record.getBytes());

            Logger.debug("distance----------" + Math.pow(10d, ((double) -61 - result.getRssi()) / (10 * 2)));
        }
    }

    void readRecord(byte[] scanRecord) {
        Logger.debug("byte length:" + scanRecord.length);
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
                Logger.debug("uuid:" + uuid);
                Logger.debug("major:" + major);
                Logger.debug("minor:" + minor);
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


    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
