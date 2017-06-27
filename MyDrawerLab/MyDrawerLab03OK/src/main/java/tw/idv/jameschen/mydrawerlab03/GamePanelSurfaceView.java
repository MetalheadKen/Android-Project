package tw.idv.jameschen.mydrawerlab03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GamePanelSurfaceView extends SurfaceView implements Callback {
	//
	public GamePanelSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 註冊處理程序
		this.getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 此處，可以利用  SurfaceHolder取得繪圖區控制物件(Canvas)
		// 1. 繪圖前，必須先鎖定Canvas. 卻確認是否完成鎖定?
		Canvas canvas = holder.lockCanvas();

		// 2. 繪圖處理動作: 利用  Canvas 元件進行畫面內容繪製.
		drawCanvas(canvas);

		// 3. 繪圖完成後，必須釋放鎖定.
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// ??
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	// 繪圖模組
	private void drawCanvas(Canvas canvas) {
		Paint pen = new Paint();
		pen.setColor(Color.BLUE);
		canvas.drawCircle( 150, 150, 100, pen);
		pen.setColor(Color.RED);
		canvas.drawCircle( 200, 300, 50, pen);
	}
}
