package com.example.blockgamelab01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GamePanelView extends View {
	private Paint bgPen = new Paint();
	private int width=320;
	private int height=480;

	// Lab01 宣告並產生必要物件 : Ball ? Bar ? Block ?
	Ball b1 = new Ball(500, 500, 15, 10, 16, Color.BLUE);
	Bar bar;
	ArrayList<Block> blocks = new ArrayList<Block>();

	//
	// 建構子
	public GamePanelView(Context context) {
		this(context, null);
	}

	public GamePanelView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		// 更新畫面 -- 執行緒
		//
		bar = new Bar( getWidth() / 2 - 40, getHeight() - 30, 150, 30, Color.BLUE);
		for(int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (row % 2 == 0) {
					Block bb = new Block(40 + col * 130, 40 + row * 40, 120, 30, Color.GRAY);
					blocks.add(bb);
				} else {
					Block bb = new Block(40 + col * 130, 40 + row * 40, 120, 30, Color.GREEN);
					blocks.add(bb);
				}
			}
		}

		new Thread() {
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					// move ball
					b1.move2(GamePanelView.this.getHeight(), GamePanelView.this.getWidth(), bar, blocks);
					GamePanelView.this.postInvalidate();

					try {
						sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	//

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		//洗掉畫面
		bgPen.setColor(Color.WHITE);
		canvas.drawColor( Color.WHITE);

		// 畫出物件
		b1.draw(canvas);

		//畫出檔板
		bar.draw(canvas);

		//畫出方塊
		for(Block bb:blocks)
			bb.draw(canvas);

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
		//重新檢查及放置檔板
		bar.y = height - 30;
		bar.x = width / 2 - 40;
		//
		setMeasuredDimension( width, height);
	}

	///碰觸的事件處理
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
				bar.moveCenterToPos(x2);
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
}
