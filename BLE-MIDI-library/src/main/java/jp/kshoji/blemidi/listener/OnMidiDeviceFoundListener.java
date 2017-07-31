package jp.kshoji.blemidi.listener;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

/**
 * Created by jim on 2017/7/23.
 */

public interface OnMidiDeviceFoundListener {
    void onDeviceFound(@NonNull BluetoothDevice device);
}
