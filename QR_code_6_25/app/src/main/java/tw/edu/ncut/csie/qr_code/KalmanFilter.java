package tw.edu.ncut.csie.qr_code;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2017/4/10.
 */

public class KalmanFilter {
    public static final double COVARIANCE_Q = 1e-6;
    public static final double COVARIANCE_R = 1e-2;

    public static final Map<String, Double> mPreSignals = new HashMap<String, Double>();
    public static final Map<String, Double> mPreErrorCovariances = new HashMap<String, Double>();

    public static double filter(double measuredSingal, String identifier) {
        /* Predicted - Get Previously RSSI And Error Covariance */
        Double predictedSignal = mPreSignals.get(identifier);
        if (predictedSignal == null) {
            predictedSignal = -70D;
        }

        Double predictedErrorCovariance = mPreErrorCovariances.get(identifier);
        if (predictedErrorCovariance == null) {
            predictedErrorCovariance = 1D;
        }

        predictedErrorCovariance += COVARIANCE_Q;

        /* Correction - Calculate Kalman Gain And Present RSSI And Present Error Covariance */
        double kalmanGain = predictedErrorCovariance / (predictedErrorCovariance + COVARIANCE_R);

        double correctionSignal = predictedSignal + kalmanGain * (measuredSingal - predictedSignal);
        double correctionErrorCovariance = (1 - kalmanGain) * predictedErrorCovariance;

        /* Refresh RSSI And Error Covariance */
        mPreSignals.put(identifier, correctionSignal);
        mPreErrorCovariances.put(identifier, correctionErrorCovariance);

        return correctionSignal;
    }
}
