package jp.kshoji.blemidi.device;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.kshoji.blemidi.listener.OnMidiDataListener;
import jp.kshoji.blemidi.listener.OnMidiInputEventListener;

/**
 * Represents BLE MIDI Input Device
 *
 * @author K.Shoji
 */
public abstract class MidiInputDevice {

    /**
     * Attaches {@link jp.kshoji.blemidi.listener.OnMidiInputEventListener}
     *
     * @param midiInputEventListener the listener
     */
    public abstract void setOnMidiInputEventListener(@Nullable OnMidiInputEventListener midiInputEventListener);

    /**
     * Obtains the device name
     *
     * @return device name
     */
    @NonNull
    public abstract String getDeviceName();

    /**
     * Obtains the device address
     *
     * @return device address
     */
    @NonNull
    public abstract String getDeviceAddress();

    @NonNull
    @Override
    public final String toString() {
        return getDeviceName();
    }

    /**
     * Listener when midi data is received
     *
     * @param listener
     */
    public abstract void setOnMidiDataListener(OnMidiDataListener listener);
}
