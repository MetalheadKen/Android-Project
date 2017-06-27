package tw.edu.ncut.csie.qr_code;

/**
 * Created by user on 2017/5/4.
 */

public class IBeaconGlobal {
    public static String[] all_product_uuid;
    public static String[] all_product_mac_address;

    public static int product_push_id = 0;
    public static String product_push_information = "";

    public static int PRODUCT_PUSH_ID = 999;

    public static double PRODUCT_BROADCAST_DISTANCE = 1.0f;

    public static boolean route_planning_flag = false;

    public static String beacon_uuid_url = "http://192.168.200.126/PushNotification.php?ctrl=2";
    public static String beacon_mac_address_url = "http://192.168.200.126/PushNotification.php?ctrl=1";
    public static String product_information_url = "http://192.168.200.126/PushSprice.php";
}
