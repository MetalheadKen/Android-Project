package tw.edu.ncut.csie.qr_code;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by user on 2017/5/1.
 */

public class IBeaconService {
    public static final int MAX_DEVICE_COUNT = 50;
    public static final int MAX_STORE_RSSI_COUNT = 30;
    public static final int MAX_COORDINATE_COUNT = 3;

    public static final int MATRIX_ROW = 2;
    public static final int MATRIX_COLUMN = 3;

    public static final int RADIUS = 0;
    public static final int X_COORDINATE = 1;
    public static final int Y_COORDINATE = 2;

    public static final double ZERO = 0.00001;

    public ArrayList<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();
    public ArrayList<String> deviceMacList = new ArrayList<String>();

    private double rssi_filter = 0.0;
    private int txpower = 0, major = 0, minor = 0;
    private String uuid;

    public int[] GetPosition(BluetoothDevice device, int rssi, byte[] scanRecord) {
        GetBluetoothDeviceDetail(device, rssi, scanRecord);

        return TraingularPosition(device, getDistance(txpower, rssi_filter), major & 0x0FFF, minor & 0x0FFF);
    }

    private String getDistance(int txPower, double rssi) {
        double distance;

        if(rssi == 0)
            return "-1.0";

		/*double ratio = rssi * 1.0 / txPower;
		if(ratio < 1.0)
			distance = Math.pow(ratio, 10);
		else
			distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;*/

        distance  =  Math.pow(10, (txPower - rssi) / 30 );

        Log.e("distance", distance + " ");

        return String.format("%.5f", distance);
    }

    double [][] min_coordinate = new double [MAX_COORDINATE_COUNT][3]; 	//Three Min Device Store Distance、X Coordinate、Y Coordinate
    double [][] beacon_distance = new double [MAX_DEVICE_COUNT][3];	  		//Every Device Store Distance、X Coordinate、Y Coordinate
    double [][] linear_equation = new double[MATRIX_ROW][MATRIX_COLUMN];	//Store X Coefficient、Y Coefficient、Constant
    double [] linear_answer = new double[MATRIX_ROW];						//Store X Result、Y Result
    //
    private int [] TraingularPosition(BluetoothDevice device, String distances, int x_point, int y_point) {
        int rssi_index = deviceMacList.indexOf(device.getAddress());

        //Store Distance、X Coordinate、Y Coordinate
        beacon_distance[rssi_index][RADIUS] 		 = Double.parseDouble(distances);
        beacon_distance[rssi_index][X_COORDINATE]	 = x_point;
        beacon_distance[rssi_index][Y_COORDINATE]	 = y_point;

        //Sort
        for(int i = 0; i < MAX_DEVICE_COUNT; i++)
        {
            for(int j = 0; j < MAX_DEVICE_COUNT - 1; j++)
            {
                if(beacon_distance[j][RADIUS] > beacon_distance[j + 1][RADIUS])
                {
                    double temp = beacon_distance[j][RADIUS];
                    beacon_distance[j][RADIUS] = beacon_distance[j + 1][RADIUS];
                    beacon_distance[j + 1][RADIUS] = temp;

                    temp = beacon_distance[j][X_COORDINATE];
                    beacon_distance[j][X_COORDINATE] = beacon_distance[j + 1][X_COORDINATE];
                    beacon_distance[j + 1][X_COORDINATE] = temp;

                    temp = beacon_distance[j][Y_COORDINATE];
                    beacon_distance[j][Y_COORDINATE] = beacon_distance[j + 1][Y_COORDINATE];
                    beacon_distance[j + 1][Y_COORDINATE] = temp;
                }
            }
        }

        //Find The Three Shortest Distance
        for(int index = 0; index < MAX_DEVICE_COUNT - 2; index++)
        {
            if (beacon_distance[index][RADIUS] > 0)
            {
                min_coordinate[0][RADIUS] 		   	= beacon_distance[index][RADIUS];
                min_coordinate[0][X_COORDINATE] 	= beacon_distance[index][X_COORDINATE];
                min_coordinate[0][Y_COORDINATE] 	= beacon_distance[index][Y_COORDINATE];

                min_coordinate[1][RADIUS] 		  	= beacon_distance[index + 1][RADIUS];
                min_coordinate[1][X_COORDINATE] 	= beacon_distance[index + 1][X_COORDINATE];
                min_coordinate[1][Y_COORDINATE] 	= beacon_distance[index + 1][Y_COORDINATE];

                min_coordinate[2][RADIUS]			= beacon_distance[index + 2][RADIUS];
                min_coordinate[2][X_COORDINATE]	= beacon_distance[index + 2][X_COORDINATE];
                min_coordinate[2][Y_COORDINATE] 	= beacon_distance[index + 2][Y_COORDINATE];

                break;
            }
        }

        //Two Linear Equation Subtract
        //
        //X = (-2 * X1 + 2 * X2), Y = (-2 * Y1 + 2 * Y2), K = (R1 * R1 - R2 * R2 - X1 * X1 - Y1 * Y1 + X2 * X2 + Y2 * Y2)
        linear_equation[0][0] = (-2 * min_coordinate[0][X_COORDINATE]) + (2 * min_coordinate[1][X_COORDINATE]);
        linear_equation[0][1] = (-2 * min_coordinate[0][Y_COORDINATE]) + (2 * min_coordinate[1][Y_COORDINATE]);
        linear_equation[0][2] = (min_coordinate[0][RADIUS] * min_coordinate[0][RADIUS]) - (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS])
                - (min_coordinate[0][X_COORDINATE] * min_coordinate[0][X_COORDINATE]) - (min_coordinate[0][Y_COORDINATE] * min_coordinate[0][Y_COORDINATE])
                + (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) + (min_coordinate[1][Y_COORDINATE] * min_coordinate[1][Y_COORDINATE]);

        //X = (-2 * X2 + 2 * X3), Y = (-2 * Y2 + 2 * Y3), K = (R2 * R2 - R3 * R3 - X2 * X2 - Y2 * Y2 + X3 * X3 + Y3 * Y3)
        linear_equation[1][0] = (-2 * min_coordinate[1][X_COORDINATE]) + (2 * min_coordinate[2][X_COORDINATE]);
        linear_equation[1][1] = (-2 * min_coordinate[1][Y_COORDINATE]) + (2 * min_coordinate[2][Y_COORDINATE]);
        linear_equation[1][2] = (min_coordinate[1][RADIUS] * min_coordinate[1][RADIUS]) - (min_coordinate[2][RADIUS] * min_coordinate[2][RADIUS])
                - (min_coordinate[1][X_COORDINATE] * min_coordinate[1][X_COORDINATE]) - (min_coordinate[1][Y_COORDINATE] * min_coordinate[1][Y_COORDINATE])
                + (min_coordinate[2][X_COORDINATE] * min_coordinate[2][X_COORDINATE]) + (min_coordinate[2][Y_COORDINATE] * min_coordinate[2][Y_COORDINATE]);

        //Gaussian Elimination
        for(int i = 0; i < MATRIX_ROW; i++) {
			/* 如果此 ROW 的首項系數為零，則找尋下方的非零 ROW 做交換 */
            if((linear_equation[i][i] < ZERO) && (linear_equation[i][i] > -ZERO)) {
                for(int j = i + 1; j < MATRIX_ROW; j++) {
                    if(!((linear_equation[j][i] < ZERO) && (linear_equation[j][i] > -ZERO))) {
						/* 交換此 ROW 與下方 ROW */
                        for(int k = i; k < MATRIX_COLUMN; k++) {
                            double temp = linear_equation[i][k];
                            linear_equation[i][k] = linear_equation[j][k];
                            linear_equation[j][k] = temp;
                        }

                        break;
                    }
                }
            }

			/* 若此 COLUMN 都為零，跳至下一 ROW */
            if((linear_equation[i][i] < ZERO) && (linear_equation[i][i] > -ZERO)) continue;

			/* 把此 ROW 的首項系數調整成一，為了讓矩陣的對角線階為一 */
            double temp = linear_equation[i][i];
            for(int j = i; j < MATRIX_COLUMN; j++) {
                linear_equation[i][j] /= temp;
            }

			/* 消去下方所有的 ROW */
            for(int j = i + 1; j < MATRIX_ROW; j++) {
                if(!((linear_equation[j][i] < ZERO) && (linear_equation[j][i] > -ZERO))) {
                    double temp2 = linear_equation[j][i];

                    for (int k = i; k < MATRIX_COLUMN; k++) {
                        linear_equation[j][k] -= linear_equation[i][k] * temp2;
                    }
                }
            }
        }

        //Back Substitution
        for(int i = MATRIX_ROW - 1; i >= 0; i--) {
            double temp = 0.0;

			/* 因對角線為要求的未知數，故不需計算 */
            for(int j = i + 1; j < MATRIX_COLUMN - 1; j++) {
                temp += linear_equation[i][j] * linear_answer[j];
            }

			/* 防止因除零而造成的 Undefined Behavior */
            if(!((linear_equation[i][i] < ZERO) && (linear_equation[i][i] > -ZERO))) {
                linear_answer[i] = (linear_equation[i][MATRIX_COLUMN - 1] - temp) / linear_equation[i][i];
            }
        }

        if(Double.isNaN(linear_answer[0]) || Double.isNaN(linear_answer[1]) || Double.isInfinite(linear_answer[0]) || Double.isInfinite(linear_answer[1]))	return null;

        Log.e("CoordinateTest", String.format("%.5f, %.5f", linear_answer[0], linear_answer[1]));

        int [] coordinate = new int[2];

        coordinate[0] = (int) linear_answer[0];
        coordinate[1] = (int) linear_answer[1];

        return coordinate;
    }

    public String[] GetProductInformation(BluetoothDevice device, String[] uuid_check) {
        HashMap<String, String> btDevice = devices.get(deviceMacList.indexOf(device.getAddress()));

        if (btDevice.get("distance") == null) return null;
        if (Double.parseDouble(btDevice.get("distance")) > IBeaconGlobal.PRODUCT_BROADCAST_DISTANCE) return null;

        if ((null) == uuid_check) return null;

        String[] product_major_minor = new String[2];

        for (int i = 0; i < uuid_check.length; i++) {
            if (btDevice.get("uuid").equals(uuid_check[i])) {
                product_major_minor[0] = btDevice.get("major");
                product_major_minor[1] = btDevice.get("minor");

                return product_major_minor;
            }
        }

        return null;
    }

    public void GetBluetoothDeviceDetail(BluetoothDevice device, int rssi, byte[] scanRecord) {
        HashMap<String, String> btmap = new HashMap<String, String>();

        //Add The Empty Object
        int index = deviceMacList.indexOf(device.getAddress());
        if (index == -1) {
            // a new one
            devices.add(btmap);
            deviceMacList.add(device.getAddress());
        } else {
            // replace old one !
            devices.remove(index);
            devices.add(index, btmap);
        }

        int check = 0;

        //從最後面開始解析封包
        for (check = scanRecord.length - 33; check >= 0; check--) {
            if (scanRecord[check] != 0x00)
                break;
        }

        if(check < 20) return;

        txpower = (scanRecord[check] & 0x00FF) | 0xFFFFFF00;

        major = scanRecord[check - 4] & 0xFF;
        major <<= 8;
        major += (scanRecord[check - 3] & 0xFF);

        minor = scanRecord[check - 2] & 0xFF;
        minor <<= 8;
        minor += (scanRecord[check - 1] & 0xFF);

        btmap.put("name", device.getName());
        btmap.put("mac_address", device.getAddress());
        btmap.put("txpower", String.format("%d", txpower));
        btmap.put("major", String.format("%d", major));
        btmap.put("minor", String.format("%d", minor));

        //rssi_filter = KalmanFilter.filter(rssi, device.getAddress());
        //btmap.put("rssi", String.format("%.5f", rssi_filter));
        rssi_filter = GaussianFilter(device, rssi);
        btmap.put("rssi", String.format("%.5f", rssi_filter));
        //
        uuid = "";
        for (int i = check - 20; i <= check - 5; i++)
        {
            if ((i == check - 10) || (i == check - 12) || (i == check - 14) || (i == check - 16))
                uuid += "-";

            uuid += String.format("%02X", scanRecord[i]);
        }
        btmap.put("uuid", uuid);

        btmap.put("distance", getDistance(scanRecord[check], rssi_filter));

        //
        index = deviceMacList.indexOf(device.getAddress());
        if (index == -1) {
            // a new one
            devices.add(btmap);
            deviceMacList.add(device.getAddress());
        } else {
            // replace old one !
            devices.remove(index);
            devices.add(index, btmap);
        }
    }

    private int [][] beacon_rssi	     = new int [MAX_DEVICE_COUNT][MAX_STORE_RSSI_COUNT];		//[device_MaxCount][rssi_MaxCount]
    private int [] beacon_rssi_index     = new int [MAX_DEVICE_COUNT];			                    //儲存的值為該裝置的rssi儲存到哪個位置了
    public int GaussianFilter(BluetoothDevice device, int rssi) {
        int index = deviceMacList.indexOf(device.getAddress());
        int rssi_index = beacon_rssi_index[index] % MAX_STORE_RSSI_COUNT;

        beacon_rssi[index][rssi_index] = rssi;

        beacon_rssi_index[index]++;

        if (beacon_rssi_index[index] < MAX_STORE_RSSI_COUNT) return -999;

        double standard_deviation = 0.0, variance = 0.0;
        int gaussian = 0, count = 0;

        //Calculate Standard Deviation
        for(int i = 0; i < MAX_STORE_RSSI_COUNT; i++)
            standard_deviation += beacon_rssi[index][i];

        standard_deviation /= MAX_STORE_RSSI_COUNT;

        //Calculate Variance
        for(int i = 0; i < MAX_STORE_RSSI_COUNT; i++)
            variance += Math.pow((beacon_rssi[index][i] - standard_deviation), 2);

        variance = Math.pow((variance / MAX_STORE_RSSI_COUNT), 0.5);

        //Calculate Interval
        for(int i = 0; i < MAX_STORE_RSSI_COUNT; i++)
        {
            if((beacon_rssi[index][i] >= (standard_deviation - variance)) && (beacon_rssi[index][i] <= (standard_deviation + variance)))
            {
                gaussian += beacon_rssi[index][i];
                count++;
            }
        }

        //Exception Process
        count = (count == 0)  ? 1 : count;

        Log.e("gaussian", "" + gaussian / count);

        return gaussian / count;
    }
}
