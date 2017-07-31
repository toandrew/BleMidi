package jp.kshoji.blemidi.base;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.Set;

import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.listener.OnMidiDataListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceFoundListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;

/**
 * Created by jim on 2017/7/30.
 */

public abstract class BaseMidiOverBleManager {
    protected OnMidiScanStatusListener midiScanStatusListener;

    protected OnMidiDeviceStatusListener midiDeviceStatusListener;

    protected OnMidiDataListener midiDataListener;

    protected OnMidiDeviceFoundListener deviceFoundListener;

    protected OnMidiDeviceAttachedListener midiDeviceAttachedListener;

    protected OnMidiDeviceDetachedListener midiDeviceDetachedListener;

    /**
     * Set the listener for attaching devices
     *
     * @param listener the listener
     */
    public void setOnMidiDeviceAttachedListener(OnMidiDeviceAttachedListener listener) {
        this.midiDeviceAttachedListener = listener;
    }

    /**
     * Set the listener for detaching devices
     *
     * @param listener the listener
     */
    public void setOnMidiDeviceDetachedListener(OnMidiDeviceDetachedListener listener) {
        this.midiDeviceDetachedListener = listener;
    }

    public void setOnMidiDataListener(OnMidiDataListener listener) {
        this.midiDataListener = listener;
    }

    /**
     * Listener when ble devices are found
     *
     * @param listener
     */
    public void setOnBluetoothDeviceFoundListener(OnMidiDeviceFoundListener listener) {
        this.deviceFoundListener = listener;
    }

    /**
     * Set the listener of device scanning status
     *
     * @param listener the listener
     */
    public void setOnMidiScanStatusListener(OnMidiScanStatusListener listener) {
        this.midiScanStatusListener = listener;
    }

    /**
     * Set the listener for device status(connecting, connected, idle, offline, error, etc)
     *
     * @param listener
     */
    public void setOnMidiDeviceStatusListener(OnMidiDeviceStatusListener listener) {
        this.midiDeviceStatusListener = listener;
    }

    public abstract void startScanDevice(int timeoutInMilliSeconds);

    public abstract void stopScanDevice();

    public abstract boolean connect(@NonNull BluetoothDevice device);

    public abstract void disconnect(@NonNull MidiInputDevice midiInputDevice, @NonNull MidiOutputDevice midiOutputDevice);

    public abstract Set<MidiInputDevice> getMidiInputDevices();

    @NonNull
    public abstract Set<MidiOutputDevice> getMidiOutputDevices();

    public abstract void terminate();
}
