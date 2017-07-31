package jp.kshoji.blemidi.exception;


public class BlueToothNotEnableException extends BleException {
    public BlueToothNotEnableException() {
        super("BlueTooth Not Enable Exception Occurred!");
    }

    public BlueToothNotEnableException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BlueToothNotEnableException(String detailMessage) {
        super(detailMessage);
    }
}
