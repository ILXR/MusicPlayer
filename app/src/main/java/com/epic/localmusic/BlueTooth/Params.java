package com.epic.localmusic.BlueTooth;

/**
 * Created by Administrator on 2017/4/4.
 */

public class Params {

    // BlueTooth Permission Code
    public static final int MY_PERMISSION_REQUEST_CONSTANT = 12;
    public static final int REQUEST_ENABLE_BT              = 11;
    public static final int REQUEST_ENABLE_VISIBILITY      = 22;

    // BlueTooth Connect params
    public static final String UUID       = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String NAME       = "EPICBluetooth";
    public static final String START_CODE = "F002A10D0A";

    public static final int MSG_REV_A_CLIENT   = 33;
    public static final int MSG_SERVER_REV_NEW = 44;
    public static final int ME                 = 999;

    // UI CODE
    public static final int REMOTE                = 998;
    public static final int MSG_WRITE_DATA        = 563;
    public static final int MSG_SERVER_WRITE_NEW  = 346;
    public static final int MSG_CLIENT_REV_NEW    = 347;
    public static final int MSG_CLIENT_WRITE_NEW  = 348;
    public static final int MSG_CONNECT_TO_SERVER = 658;

    public static final int MSG_CONNECT_FAILED  = 1001;
    public static final int MSG_CONNECT_SUCCEED = 1002;

}
