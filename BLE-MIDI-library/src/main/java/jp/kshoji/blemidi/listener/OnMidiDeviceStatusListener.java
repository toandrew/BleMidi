package jp.kshoji.blemidi.listener;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

/**
 * Created by jim on 2017/7/24.
 */

public interface OnMidiDeviceStatusListener {
    public static final int DEVICE_IDLE = 0;

    public static final int DEVICE_SERVICE_DISCOVERING = 1;
    public static final int DEVICE_SERVICE_DISCOVERED_OK = 2;
    public static final int DEVICE_SERVICE_DISCOVERED_FAILED = 3;

    public static final int DEVICE_CHAR_DISCOVERING = 4;
    public static final int DEVICE_CHAR_DISCOVERED_OK = 5;
    public static final int DEVICE_CHAR_DISCOVERED_FAILED = 6;
    public static final int DEVICE_CHAR_RECV_DATA = 7;
    public static final int DEVICE_CHAR_ENABLE_WRITE = 8;

    public static final int DEVICE_CONNECTING = 9;
    public static final int DEVICE_CONNECTED = 10;
    public static final int DEVICE_CONNECT_FAILED = 11;
    public static final int DEVICE_DISCONNECTED = 12;

    public static final int DEVICE_OFFLINE = 13;
    public static final int DEVICE_ERROR = 14;

    void onDeviceStatusChanged(@NonNull BluetoothDevice device, int status);
}
