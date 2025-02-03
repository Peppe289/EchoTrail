package com.peppe289.echotrail.utils;

public interface ControllerCallback<R, E> {
    void onSuccess(R result);
    void onError(E error);
}
