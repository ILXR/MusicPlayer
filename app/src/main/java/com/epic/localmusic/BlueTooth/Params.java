package com.epic.localmusic.BlueTooth;

/**
 * Created by Administrator on 2017/4/4.
 */

public class Params {

    // BlueTooth Permission Code
    public static final int MY_PERMISSION_REQUEST_CONSTANT = 12;
    public static final int REQUEST_ENABLE_BT              = 11;
    public static final int REQUEST_ENABLE_VISIBILITY      = 22;

    // BlueTooth Connect Params
    public static final String UUID             = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String NAME             = "EPICBluetooth";
    public static final String START_CODE       = "F002A20D0A";
    public static final byte[] START_BYTE_ARRAY = {(byte) 0xF0, (byte) 0x02, (byte) 0xA2, (byte) 0x0D, (byte) 0x0A};

    // WebSocket Params
    public static final String SERVER_URL = "ws://222.20.79.254:50001";
    public static final String WS_HEADER  = "__EPIC__";

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
    public static final int MSG_CONNECT_FAILED    = 1001;
    public static final int MSG_CONNECT_SUCCEED   = 1002;

}
