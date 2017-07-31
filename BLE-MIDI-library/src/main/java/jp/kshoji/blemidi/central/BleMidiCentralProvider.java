package jp.kshoji.blemidi.central;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.Set;

import jp.kshoji.blemidi.base.BaseMidiOverBleManager;
import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.exception.BleAdapterException;
import jp.kshoji.blemidi.exception.BlueToothNotEnableException;
import jp.kshoji.blemidi.listener.OnMidiDataListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceFoundListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;
import jp.kshoji.blemidi.util.BleMidiDeviceUtils;
import jp.kshoji.blemidi.util.BleUtils;

import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_CONNECTING;
import static jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener.DEVICE_ERROR;
import static jp.kshoji.blemidi.util.Constants.TAG;

/**
 * Client for BLE MIDI Peripheral device service
 *
 * @author K.Shoji
 */
public final class BleMidiCentralProvider extends BaseMidiOverBleManager {
    private final BluetoothAdapter bluetoothAdapter;

    private final Context context;

    private final Handler handler;

    private final BleMidiCallback midiCallback;

    /**
     * Callback for BLE device scanning (for Lollipop or later)
     */
    private ScanCallback scanCallback;

    private volatile boolean isScanning = false;

    private Runnable stopScanRunnable = null;

    private boolean autoConnect = false;

    /**
     * Callback for BLE device scanning
     */
    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            Log.w(TAG, "bluetoothDevice[" + bluetoothDevice + "]name[" + bluetoothDevice.getName() + "]type[" + bluetoothDevice.getType() + "][" + bluetoothDevice.toString() + "]");

            if (!BleUtils.isBleType(bluetoothDevice)) {
                return;
            }

            Log.w(TAG, "connectGatt![" + bluetoothDevice + "][" + bluetoothDevice.getName() + "]");

            processScanResults(bluetoothDevice);
        }
    };

    /**
     * Constructor<br />
     * Before constructing the instance, check the Bluetooth availability.
     *
     * @param context the context
     */
    @SuppressLint("NewApi")
    public BleMidiCentralProvider(@NonNull final Context context) throws UnsupportedOperationException, BleAdapterException, BlueToothNotEnableException {
        if (!BleUtils.isBleSupported(context)) {
            throw new UnsupportedOperationException("Bluetooth LE not supported on this device.");
        }

        bluetoothAdapter = getBleAdapter(context);
        if (bluetoothAdapter == null) {
            throw new BleAdapterException("Bluetooth is not available.");
        }

        if (bluetoothAdapter.isEnabled() == false) {
            //throw new BlueToothNotEnableException("Bluetooth is disabled.");
            bluetoothAdapter.enable();
        }

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            Log.w(TAG, "support midi======!!!!");
        } else {
            Log.w(TAG, "not support midi======!!!!");
        }


        this.context = context;
        this.midiCallback = new BleMidiCallback(context);
        this.handler = new Handler(context.getMainLooper());

        setDeviceScanCallback();
    }

    /**
     * Starts to scan devices
     *
     * @param timeoutInMilliSeconds 0 or negative value : no timeout
     */
    @SuppressLint({"Deprecation", "NewApi"})
    @Override
    public void startScanDevice(int timeoutInMilliSeconds) {
        startScan(timeoutInMilliSeconds);
    }

    /**
     * Stops to scan devices
     */
    @SuppressLint({"Deprecation", "NewApi"})
    @Override
    public void stopScanDevice() {
        stopScan();
    }

    /**
     * Connect the specific ble device
     *
     * @param device
     * @return
     */
    @Override
    public boolean connect(BluetoothDevice device) {
        notifyMidiDeviceStatusChanged(device, DEVICE_CONNECTING);

        BluetoothDevice d = bluetoothAdapter.getRemoteDevice(device.getAddress());
        if (d == device) {
            Log.w(TAG, "connect same device[" + device + "][" + d + "]");
        } else {
            Log.w(TAG, "connect not the same device[" + device + "][" + d + "][" + context + "]");
        }

        if (d.connectGatt(context, true, midiCallback) != null) {
            return true;
        }

        notifyMidiDeviceStatusChanged(d, DEVICE_ERROR);

        return false;
    }

    /**
     * Disconnect ble device
     *
     * @param midiInputDevice
     * @param midiOutputDevice
     */
    @Override
    public void disconnect(@NonNull MidiInputDevice midiInputDevice, @NonNull MidiOutputDevice midiOutputDevice) {
        disconnectDevice(midiInputDevice);
        disconnectDevice(midiOutputDevice);
    }

    /**
     * Set the listener for attaching devices
     *
     * @param midiDeviceAttachedListener the listener
     */
    @Override
    public void setOnMidiDeviceAttachedListener(@Nullable OnMidiDeviceAttachedListener midiDeviceAttachedListener) {
        this.midiCallback.setOnMidiDeviceAttachedListener(midiDeviceAttachedListener);
    }

    /**
     * Set the listener for detaching devices
     *
     * @param midiDeviceDetachedListener the listener
     */
    @Override
    public void setOnMidiDeviceDetachedListener(@Nullable OnMidiDeviceDetachedListener midiDeviceDetachedListener) {
        this.midiCallback.setOnMidiDeviceDetachedListener(midiDeviceDetachedListener);
    }

    /**
     * Listener when ble devices status changed
     *
     * @param listener
     */
    @Override
    public void setOnMidiDeviceStatusListener(OnMidiDeviceStatusListener listener) {
        super.setOnMidiDeviceStatusListener(listener);

        if (midiCallback != null) {
            midiCallback.setOnMidiDeviceStatusListener(listener);
        }
    }

    @Override
    public void setOnMidiDataListener(OnMidiDataListener listener) {
        super.setOnMidiDataListener(listener);

        if (midiCallback != null) {
            midiCallback.setOnMidiDataListener(listener);
        }
    }

    /**
     * Obtains the set of {@link jp.kshoji.blemidi.device.MidiInputDevice} that is currently connected
     *
     * @return unmodifiable set
     */
    @NonNull
    @Override
    public Set<MidiInputDevice> getMidiInputDevices() {
        return midiCallback.getMidiInputDevices();
    }

    /**
     * Obtains the set of {@link jp.kshoji.blemidi.device.MidiOutputDevice} that is currently connected
     *
     * @return unmodifiable set
     */
    @NonNull
    @Override
    public Set<MidiOutputDevice> getMidiOutputDevices() {
        return midiCallback.getMidiOutputDevices();
    }

    /**
     * Terminates provider
     */
    @Override
    public void terminate() {
        stopScanDevice();
        midiCallback.terminate();
    }

    /**
     * Auto connect ble device
     *
     * @param auto
     */
    public void setAutoConnect(boolean auto) {
        autoConnect = auto;
    }

    /**
     * Whether we should auto connect ble device
     *
     * @return
     */
    public boolean isAutoConnect() {
        return autoConnect;
    }

    /**
     * Disconnects the specified device
     *
     * @param midiInputDevice the device
     */
    private void disconnectDevice(@NonNull MidiInputDevice midiInputDevice) {
        midiCallback.disconnectDevice(midiInputDevice);
    }

    /**
     * Disconnects the specified device
     *
     * @param midiOutputDevice the device
     */
    private void disconnectDevice(@NonNull MidiOutputDevice midiOutputDevice) {
        midiCallback.disconnectDevice(midiOutputDevice);
    }

    /**
     * Get bluetooth adapter according to different api
     *
     * @param context
     * @return
     */
    private BluetoothAdapter getBleAdapter(@NonNull Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return bluetoothManager.getAdapter();
        }

        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Set device scan callback according to different api
     */
    private void setDeviceScanCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        final BluetoothDevice bluetoothDevice = result.getDevice();
                        if (!BleUtils.isBleType(bluetoothDevice)) {
                            return;
                        }

                        processScanResults(bluetoothDevice);
                    }
                }
            };
        } else {
            scanCallback = null;
        }
    }

    /**
     * Start scan ble devices
     */
    private void startScan(int timeoutInMilliSeconds) {
        Log.w(TAG, "Begin startScan!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            List<ScanFilter> scanFilters = BleMidiDeviceUtils.getBleMidiScanFilters(context);
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            //bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            //bluetoothAdapter.startLeScan(BleMidiDeviceUtils.getUuidListForService(context), leScanCallback);
            bluetoothAdapter.startLeScan(leScanCallback);
        }

        Log.w(TAG, "End startScan!!!");
        // scanning
        notifyMidiScanStatusChanged(true);

        // start stop scan timer
        triggerStopScanTimer(timeoutInMilliSeconds);
    }

    /**
     * Stop scan ble devices
     */
    private void stopScan() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            } else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        } catch (Throwable ignored) {
            // NullPointerException on Bluetooth is OFF
        }

        if (stopScanRunnable != null) {
            handler.removeCallbacks(stopScanRunnable);
            stopScanRunnable = null;
        }

        notifyMidiScanStatusChanged(false);
    }

    /**
     * Stop scan ble devices when the specific time is over
     *
     * @param timeoutInMilliSeconds
     */
    private void triggerStopScanTimer(int timeoutInMilliSeconds) {
        if (stopScanRunnable != null) {
            handler.removeCallbacks(stopScanRunnable);
        }

        if (timeoutInMilliSeconds > 0) {
            stopScanRunnable = new Runnable() {
                @Override
                public void run() {
                    stopScanDevice();

                    notifyMidiScanStatusChanged(false);
                }
            };
            handler.postDelayed(stopScanRunnable, timeoutInMilliSeconds);
        }
    }

    /**
     * Notify whether we are scanning ble devices
     *
     * @param scanning
     */
    private void notifyMidiScanStatusChanged(boolean scanning) {
        isScanning = scanning;

        if (midiScanStatusListener != null) {
            midiScanStatusListener.onMidiScanStatusChanged(isScanning);
        }
    }

    static boolean isConnecting = false;

    /**
     * Process the scanned ble devices
     *
     * @param bluetoothDevice
     */
    private void processScanResults(@NonNull final BluetoothDevice bluetoothDevice) {
        if (deviceFoundListener != null) {
            deviceFoundListener.onDeviceFound(bluetoothDevice);
        }

        // Auto connect ble device if it's necessary
        if (isAutoConnect() && !midiCallback.isConnected(bluetoothDevice) && !isConnecting) {
            isConnecting = true;
            bluetoothDevice.connectGatt(BleMidiCentralProvider.this.context, false, midiCallback);
        }
    }

    /**
     * Notify when ble midi device status changed!
     *
     * @param status
     */
    private void notifyMidiDeviceStatusChanged(@NonNull BluetoothDevice device, int status) {
        if (midiDeviceStatusListener != null) {
            midiDeviceStatusListener.onDeviceStatusChanged(device, status);
        }
    }
}
