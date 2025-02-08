package com.peppe289.echotrail.utils.callback;

import com.peppe289.echotrail.controller.callback.CommonCallback;
import com.peppe289.echotrail.utils.ErrorType;

public interface HelperCallback<R, E extends ErrorType> extends CommonCallback<R, ErrorType> {}
