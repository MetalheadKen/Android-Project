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
    public static final int MAX_COORDINATE_COUNT = 3;
    public static final int MATRIX_ROW = 2;
    public static final int MATRIX_COLUMN = 3;

    public static final int RADIUS = 0;
    public static final int X_COORDINATE = 1;
    public static final int Y_COORDINATE = 2;

    public static final double ZERO = 0.00001;

    public ArrayList<HashMap<String, String>> devices = new ArrayList<HashMap<String, String>>();
    public ArrayList<String> deviceMacList = new ArrayList<String>();

    private int rssi_filter = 0;

    public String GetCoordinate(BluetoothDevice device, int rssi, byte[] scanRecord) {
        int check = 0;
        //從最後面開始解析封包
        for (check = scanRecord.length - 33; check >= 0; check--) {
            if (scanRecord[check] != 0x00)
                break;
        }

        if(check < 20) return "";

        HashMap<String, String> btmap = new HashMap<String, String>();
        btmap.put("name", device.getName());

        btmap.put("mac_address", device.getAddress());
        btmap.put("discovery_time", new Date().toString());
        btmap.put("txpower", "TxPower：" + String.format("%02X", scanRecord[check]));
        btmap.put("major", "Major-Minor：" + String.format("%02d", (scanRecord[check - 3] + scanRecord[check - 4]) & 0x0FFF));
        btmap.put("minor", String.format("%02d", (scanRecord[check - 2] + scanRecord[check - 1]) & 0x0FFF));

        rssi_filter = (int) KalmanFilter.filter(rssi, device.getAddress());
        btmap.put("rssi", String.format("Rssi：%d", rssi));

        //
        String uuid = "";
        for (int i = check - 20; i <= check - 5; i++)
        {
            if ((i == check - 10) || (i == check - 12) || (i == check - 14) || (i == check - 16))
                uuid += "-";

            uuid += String.format("%02X", scanRecord[i]);
        }
        btmap.put("uuid", uuid);

        btmap.put("distance", "Dist：" + getDistance(scanRecord[check], rssi_filter));

        //
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

        //
        return TraingularPosition(device, getDistance(scanRecord[check], rssi_filter), (scanRecord[check - 3] + scanRecord[check - 4]) & 0x0FFF, (scanRecord[check - 2] + scanRecord[check - 1]) & 0x0FFF);
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
    private String TraingularPosition(BluetoothDevice device, String distances, int x_point, int y_point) {
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

        if(Double.isNaN(linear_answer[0]) || Double.isNaN(linear_answer[1]) || Double.isInfinite(linear_answer[0]) || Double.isInfinite(linear_answer[1]))	return "";

        Log.e("CoordinateTest", String.format("%.5f, %.5f", linear_answer[0], linear_answer[1]));
        return String.format("%.0f,%.0f", linear_answer[0], linear_answer[1]);
    }
}
