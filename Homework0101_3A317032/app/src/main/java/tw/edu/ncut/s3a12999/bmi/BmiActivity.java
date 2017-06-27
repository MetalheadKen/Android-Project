package tw.edu.ncut.s3a12999.bmi;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BmiActivity extends AppCompatActivity {

    Button btnComput, btnClear;
    EditText etHeight, etWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        btnComput = (Button) findViewById(R.id.btnCompute);
        btnClear = (Button) findViewById(R.id.btnClear);
        etHeight = (EditText) findViewById(R.id.etHeight);
        etWeight = (EditText) findViewById(R.id.etWeight);
    }

    public void computeBMI(View v) {
        if (etHeight.getText().toString().equals("") || etWeight.getText().toString().equals("")) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(BmiActivity.this);
            dialog.setTitle("輸入錯誤")
                  .setMessage("身高或體重未輸入，請輸入後再進行計算")
                  .show();

            return;
        }

        double h = Double.parseDouble(etHeight.getText().toString()) / 100;
        double w = Double.parseDouble(etWeight.getText().toString());
        double bmi = w / Math.pow(h, 2);

        if (bmi < 20) {
            String string = String.format("%.2f", bmi);

            Toast.makeText(this, "BMI為：" + string + "; 過輕，要多吃一點！", Toast.LENGTH_LONG).show();
        }
        else if (bmi > 25) {
            String string = String.format("%.2f", bmi);

            Toast.makeText(this, "BMI為：" + string + "; 過重，要少吃多運動！！", Toast.LENGTH_LONG).show();
        }
        else {
            String string = String.format("%.2f", bmi);

            Toast.makeText(this, "BMI為：" + string + "; 標準體重，恭喜你！！！", Toast.LENGTH_LONG).show();
        }
    }

    public void clearText(View v) {
        etHeight.setText("");
        etWeight.setText("");
    }
}
