package com.peppe289.echotrail.controller.callback;

import com.peppe289.echotrail.utils.ErrorType;

public interface ControllerCallback<R, E extends ErrorType> extends CommonCallback<R, E> {}
