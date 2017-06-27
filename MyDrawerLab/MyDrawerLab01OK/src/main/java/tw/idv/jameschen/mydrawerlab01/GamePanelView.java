package tw.idv.jameschen.mydrawerlab01;

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

	// 至少需要一個建構子.
	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// 主要作為繪圖區域大小的確認(調整)--只能調小、不可能變大. [不一定需要!]
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// MeasureSpec : combination of Mode + Size;
		int w_mode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int h_mode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		//
		Log.i("View", String.format("onMeasure(...); init (w,h)=(%d,%d)", widthSize, heightSize));

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

	// onDraw(...) 是系統認為有需要時，才會進行呼叫，以進行畫面更新。
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 利用 Canvas 進行畫面繪製. 通常會搭配 Paint 與 Color 元件.
		Paint gPen = new Paint();
		gPen.setColor(Color.GREEN);
		canvas.drawRect( 100, 100, 200, 200, gPen);
	}
}
