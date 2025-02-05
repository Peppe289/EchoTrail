package com.peppe289.echotrail.controller.callback;

public interface CommonCallback<R, E> {
    void onSuccess(R result);
    void onError(E error);
}
