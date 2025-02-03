package com.peppe289.echotrail.utils;

import com.peppe289.echotrail.R;
import android.content.Context;

public enum ErrorType {
    POSITION_PERMISSION_ERROR(1001, R.string.error_location_permission),
    POSITION_MOCK_ERROR(1002, R.string.error_mock_location),
    CANNOT_OPEN_LINK_ERROR(1003, R.string.error_cannot_open_link),
    SEND_FRIEND_REQUEST_ERROR(1004, R.string.error_send_friend_request),
    UNKNOWN_ERROR(9999, R.string.error_unknown);

    private final int code;
    private final int stringResId;

    ErrorType(int code, int stringResId) {
        this.code = code;
        this.stringResId = stringResId;
    }

    public int getCode() {
        return code;
    }

    public String getMessage(Context context) {
        return context.getString(stringResId);
    }

    /*
    public static ErrorType fromCode(int code) {
        for (ErrorType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return UNKNOWN_ERROR;
    }
    */
}
