package com.example.blockgamelab01;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView( new GamePanelView( this, null));
    }

}
