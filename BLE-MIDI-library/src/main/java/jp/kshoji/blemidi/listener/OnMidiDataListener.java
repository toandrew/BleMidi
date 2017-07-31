package jp.kshoji.blemidi.listener;

import jp.kshoji.blemidi.device.MidiInputDevice;

/**
 * Created by jim on 2017/7/26.
 */

public interface OnMidiDataListener {
    void onMidiData(MidiInputDevice device, final byte[] data);
}
