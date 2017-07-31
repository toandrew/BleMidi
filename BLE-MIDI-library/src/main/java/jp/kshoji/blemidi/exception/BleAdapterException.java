package jp.kshoji.blemidi.exception;

public class BleAdapterException extends BleException {

    public BleAdapterException() {
        super("BlueTooth Not Enable Exception Occurred!");
    }

    public BleAdapterException(String detailMessage) {
        super(detailMessage);
    }
}
