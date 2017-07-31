package com.theonepiano.blemidi.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import com.theonepiano.blemidi.bluetooth.bean.MyBluetoothDevice;
import com.wanaka.midicore.ble.BleMidiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.listener.OnMidiDataListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceFoundListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;

import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_DISCONNECTED;
import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_IDLE;
import static jp.kshoji.blemidi.util.Constants.TAG;

/**
 * Created by jim on 2017/7/18.
 */

public class BluetoothPresenter extends BluetoothContract.Presenter {
    BleMidiManager mBleMidiManager;

    private List<MyBluetoothDevice> mBluetoothDeviceList = new ArrayList<>();

    private HashMap<String, BluetoothDevice> mBluetoothDevicesHashMap = new HashMap<>();

    @Override
    public void init() {
        mBleMidiManager.getInstance().init(mView.getMyContext());
    }

    @Override
    public void startScan() {
        reset();

        initEventListeners();

        mBleMidiManager.getInstance().open();
    }

    @Override
    public void stopScan() {
        mBleMidiManager.getInstance().close();

        // after close
        reset();
    }

    @Override
    public boolean isBluetoothDeviceConnected() {
        return mBleMidiManager.getInstance().isConnected();
    }

    @Override
    public void connect(String devId, String name) {
        BluetoothDevice d = mBluetoothDevicesHashMap.get(devId.toLowerCase());
        if (d != null) {
            mBleMidiManager.getInstance().connect(d);
        }
    }

    @Override
    public void disconnect() {
        mBleMidiManager.getInstance().disconnect();
    }

    @Override
    public void sendMidiMessage(byte[] data) {
        mBleMidiManager.getInstance().sendMsg(data);
    }

    private MyBluetoothDevice createMyBluetoothDevice(@NonNull BluetoothDevice device, int status) {
        MyBluetoothDevice d = new MyBluetoothDevice();
        d.id = device.getAddress();
        d.name = device.getName();
        d.info = device.getAddress() + "(" + device.getType() + ")";
        d.status = status;

        return d;
    }

    private List<MyBluetoothDevice> updateDevices(@NonNull BluetoothDevice device, int status) {
        Log.w(TAG, "updateDevices![" + mBluetoothDeviceList.size() + "]");

        synchronized (mBluetoothDeviceList) {
            BluetoothDevice d = mBluetoothDevicesHashMap.get(device.getAddress().toLowerCase());
            if (d == null) {
                MyBluetoothDevice m = createMyBluetoothDevice(device, status);
                mBluetoothDevicesHashMap.put(m.id.toLowerCase(), device);

                mBluetoothDeviceList.add(m);
            } else {
                for (MyBluetoothDevice m : mBluetoothDeviceList) {
                    if (m.id.equalsIgnoreCase(device.getAddress())) {
                        m.status = status;
                        Log.w(TAG, "device: " + device + "]status[ " + m.status + "]");
                    }
                }
            }
        }

        return mBluetoothDeviceList;
    }

    /**
     * reset all
     */
    private void reset() {
        removeUselessDevices();

        mBleMidiManager.getInstance().setOnBluetoothDeviceFoundListener(null);
        mBleMidiManager.getInstance().setOnMidiScanStatusListener(null);
        mBleMidiManager.getInstance().setOnMidiDeviceStatusListener(null);
    }

    /**
     * init device event listeners
     */
    private void initEventListeners() {
        mBleMidiManager.getInstance().setOnBluetoothDeviceFoundListener(new OnMidiDeviceFoundListener() {
            @Override
            public void onDeviceFound(@NonNull BluetoothDevice device) {
                Log.w(TAG, "onDeviceFound[" + device + "]");
                mView.onDeviceUpdated(updateDevices(device, DEVICE_IDLE));
            }
        });

        mBleMidiManager.getInstance().setOnMidiScanStatusListener(new OnMidiScanStatusListener() {
            @Override
            public void onMidiScanStatusChanged(boolean isScanning) {
                mView.onDeviceScanStatusChanged(isScanning);
            }
        });

        mBleMidiManager.getInstance().setOnMidiDeviceStatusListener(new OnMidiDeviceStatusListener() {
            @Override
            public void onDeviceStatusChanged(@NonNull BluetoothDevice device, int status) {
                Log.w(TAG, "device:" + device + " status[" + status + "]");
                mView.onDeviceUpdated(updateDevices(device, status));
            }
        });

        mBleMidiManager.getInstance().setOnMidiDataListener(new OnMidiDataListener() {
            @Override
            public void onMidiData(MidiInputDevice device, byte[] data) {
                Log.w(TAG, "onMidiData!!!!");
                mView.onMidiDataReceived(data);
            }
        });
    }

    /**
     * remove all non-connected ble devices
     */
    private void removeUselessDevices() {
        synchronized (mBluetoothDeviceList) {
            Iterator<MyBluetoothDevice> it = mBluetoothDeviceList.iterator();
            while (it.hasNext()) {
                MyBluetoothDevice d = it.next();
                if (d.status == DEVICE_IDLE || d.status == DEVICE_DISCONNECTED) {
                    mBluetoothDevicesHashMap.remove(d.id.toLowerCase());
                    it.remove();
                }
            }
        }
    }
}
