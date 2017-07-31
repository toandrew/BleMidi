package jp.kshoji.blemidi.exception;


public class MidiOverBleNotEnableException extends BleException {
    public MidiOverBleNotEnableException() {
        super("Midi Over Ble Exception Occurred!");
    }

    public MidiOverBleNotEnableException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MidiOverBleNotEnableException(String detailMessage) {
        super(detailMessage);
    }
}
