package com.example.blockgamelab03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GamePanelView extends View implements SensorEventListener {
	private final Context context;
	//
	UpdatingThread updatingThread;
	//
	private Paint bgPen = new Paint();
	private int width=320;
	private int height=480;
	private Ball b1, b2;
	private Bar bar;

	// 本文
	public GamePanelView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//
		//記下Context，以利Sensor註冊
		this.context = context;
		//
		b1 = new Ball( 160, 10, 15, 0, 10, Color.BLUE);
		b2 = new Ball( 200, 10, 10, -3, 10, Color.GREEN);
		//
		bar = new Bar( getWidth() / 2 - 40, getHeight() - 30, 150, 30, Color.BLUE);
		updatingThread = new UpdatingThread(this);
		updatingThread.start();
		//
		//Sensor 註冊
		SensorManager sMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sMgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
		sMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

	}

	//

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		bgPen.setColor(Color.WHITE);
		canvas.drawColor( Color.WHITE);
		// 畫出物件
		b1.draw(canvas);
		b2.draw(canvas);
		bar.draw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//int specWMode = MeasureSpec.getMode(widthMeasureSpec);
		//int specHMode = MeasureSpec.getMode(heightMeasureSpec);

		this.width = MeasureSpec.getSize(widthMeasureSpec);
		this.height = MeasureSpec.getSize(heightMeasureSpec);
		//
		bar.y = height - 30;
		bar.x = width / 2 - 40;
		bar.setSize( width, height );
		//
		setMeasuredDimension( width, height);
	}

	int x1, y1;
	int x2, y2;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch ( event.getAction() ) {
			case MotionEvent.ACTION_DOWN: // touch down
				x1 = (int) event.getX();
				y1 = (int) event.getY();
				break;
			case MotionEvent.ACTION_MOVE: // touch drag with the ball
				x2 = (int) event.getX();
				y2 = (int) event.getY();
				//bar.moveCenterToPos(x2);
				this.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				x1 = x2 = (int) event.getX();
				y1 = y2 = (int) event.getY();
				break;
		}
		//
		return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d("SENSOR", String.format("%.2f %.2f %.2f", event.values[0], event.values[1], event.values[2]));
		//
		if(event.values[0] < 0)
			bar.moveRight();
		else
			bar.moveLeft();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	//
	class UpdatingThread extends Thread {
		//
		boolean running = true;
		GamePanelView gameView;
		//
		public UpdatingThread(GamePanelView view) {
			this.gameView = view;
		}
		//
		public void stopRunning() {
			running = false;
		}
		//
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				// move ball
				b1.move(GamePanelView.this.getHeight(), GamePanelView.this.getWidth());
				b2.move(GamePanelView.this.getHeight(), GamePanelView.this.getWidth());

				GamePanelView.this.postInvalidate();
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
