package jp.kshoji.blemidi.exception;

public abstract class BleException extends RuntimeException {
    public BleException() {
        super();
    }

    public BleException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BleException(String detailMessage) {
        super(detailMessage);
    }

    public BleException(Throwable throwable) {
        super(throwable);
    }
}
