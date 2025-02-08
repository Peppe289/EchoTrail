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
    REJECT_FRIEND_REQUEST_ERROR(1011, R.string.error_reject_friend_request),
    REMOVE_FRIEND_ERROR(1012, R.string.error_remove_friend),
    FRIEND_NOT_FOUND_ERROR(1013, R.string.error_friend_not_found),
    GET_PENDING_REQUESTS_ERROR(1014, R.string.error_get_pending_requests),
    GET_FRIENDS_ERROR(1015, R.string.error_get_friends),
    SAVE_NOTE_FAILED(1016, R.string.error_save_note_failed),
    GET_USER_READ_NOTES_ERROR(1017, R.string.error_get_user_read_notes),
    USER_NOT_LOGGED_IN_ERROR(1018, R.string.error_user_not_logged_in),
    GET_USER_NOTES_ERROR(1019, R.string.error_get_user_notes),
    POSITION_NOT_FOUND_ERROR(1020, R.string.error_position_not_found),
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
