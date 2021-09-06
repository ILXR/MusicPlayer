package com.epic.localmusicnoserver.util;

/**
 * Created by Administrator on 2017/4/4.
 */

public class EpicParams {

    // BlueTooth Permission Code
    public static final int MY_PERMISSION_REQUEST_CONSTANT = 12;
    public static final int REQUEST_ENABLE_BT              = 11;
    public static final int REQUEST_ENABLE_VISIBILITY      = 22;

    // BlueTooth Connect Params
    public static final String   UUID             = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String   NAME             = "EPICBluetooth";
    public static final String   START_CODE       = "F002A20D0A";
    public static final byte[]   START_BYTE_ARRAY = {(byte) 0xF0, (byte) 0x02, (byte) 0xA2, (byte) 0x0D, (byte) 0x0A};
    public static final Double[] BtValueParams    = {0.000137774d, -0.003350385d, 0.027770317d, 0.865758775d, 0.708142224d};

    // UI CODE
    public static final int MSG_REV_A_CLIENT   = 33;
    public static final int MSG_SERVER_REV_NEW = 44;
    public static final int ME                 = 999;

    public static final int REMOTE                = 998;
    public static final int MSG_WRITE_DATA        = 563;
    public static final int MSG_SERVER_WRITE_NEW  = 346;
    public static final int MSG_CLIENT_REV_NEW    = 347;
    public static final int MSG_CLIENT_WRITE_NEW  = 348;
    public static final int MSG_CONNECT_TO_SERVER = 658;
    public static final int MSG_CONNECT_FAILED    = 1001;
    public static final int MSG_CONNECT_SUCCEED   = 1002;
}
