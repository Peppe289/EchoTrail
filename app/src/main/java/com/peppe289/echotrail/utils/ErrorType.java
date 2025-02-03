package com.peppe289.echotrail.utils;

import com.peppe289.echotrail.R;
import android.content.Context;

public enum ErrorType {
    POSITION_PERMISSION_ERROR(1001, R.string.error_location_permission),
    POSITION_MOCK_ERROR(1002, R.string.error_mock_location),
    CANNOT_OPEN_LINK_ERROR(1003, R.string.error_cannot_open_link),
    SEND_FRIEND_REQUEST_ERROR(1004, R.string.error_send_friend_request),
    INVALID_DATA_ERROR(1005, R.string.error_invalid_data),
    INVALID_USERNAME_ERROR(1006, R.string.error_invalid_username),
    INVALID_EMAIL_ERROR(1007, R.string.error_invalid_email),
    INVALID_PASSWORD_ERROR(1008, R.string.error_invalid_password),
    INVALID_CREDENTIALS_ERROR(1009, R.string.error_invalid_credentials),
    ACCEPT_FRIEND_REQUEST_ERROR(1010, R.string.error_accept_friend_request),
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
