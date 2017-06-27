package tw.idv.jameschen.mydrawerlab02;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GamePanelView extends View {
	// Desired Height, Width;
	static final int VIEW_DEFAULT_HEIGHT = 60;
	static final int VIEW_DEFAULT_WIDTH = 120;
	private final UpdatingThread myThread;
	// 0202. 宣告必要屬性
	private int x = 100, y = 100, r = 30;

	// 建構子
	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 0203. 產生並啟動更新畫面的執行緒
		myThread = new UpdatingThread();
		myThread.start();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		// MeasureSpec : combination of Mode + Size;
		int w_mode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int h_mode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		//
		Log.i("onMeasure(...)", String.format("init (w,h)=(%d,%d)", widthSize, heightSize));

		/*
		若使用自訂調整方案， super.onMeasure(...) 就不需要了!
		// Example: 調整寬度與高度
		int width = widthSize, height = heightSize;
		if (w_mode == MeasureSpec.AT_MOST) {
		   	// 只能變小, 不可超過該值!
		   	with = min(VIEW_DEFAULT_WIDTH, widthSize);
		}
		else if (w_mode == MeasureSpec.EXACTLY) {
		    // 剛剛好
			width = widthSize;
		}
		else {
			width = VIEW_DEFAULT_WIDTH;
		}
		// 高度調整: 同上類似規則!
		 
		// 最後，修正View為最終的寬度、高度.
	    setMeasuredDimension(width, height);
	    
	    */
	}
	
	// the method onDraw(...) will be called on demand !
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(...) : 會重繪基本底色
		super.onDraw(canvas);
		//
		Paint gPen = new Paint();
		gPen.setColor(Color.GREEN);
		// 0204. 以變數 (x, y) 來繪製球的位置(圓心)
		canvas.drawCircle( x, y, r, gPen);
	}
	
	// 0205. 覆寫 onAttachedToWindow 和  onDetachedFromWindow 加入必要的初始化與善後處理動作.

	// 提供其他模組可以呼叫：讓畫面停止更新
	public void stopNow() {
		myThread.stop();
	}
	
	// 0201. 自訂一個更新畫面的執行緒
    class UpdatingThread extends Thread {
		@Override
		public void run() {
			while (true) {
				x += 3;
				y += 5;
				//

				postInvalidate();
				//
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
