package tw.idv.jameschen.mydrawerlab04;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GamePanelSurfaceView extends SurfaceView implements Callback {
	private UpdatingThread myThread;

	// 0402. 宣告必要屬性
	private int x = 100, y = 100, r = 30;

	//
	public GamePanelSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 註冊處理程序
		this.getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 0403. 產生並啟動更新畫面的執行緒 (註：需傳遞 SurfaceHolder 給執行緒)
		//Canvas canvas = holder.lockCanvas();
		//drawCanvas(canvas);
		//holder.unlockCanvasAndPost(canvas);
		myThread = new UpdatingThread( holder );
		myThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// ??

	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 0404. 暫停執行緒 (註：需傳遞 SurfaceHolder 給執行緒)
	}

	// 0405. 提供其他模組可以呼叫：讓畫面停止更新
	public void stopNow() {


	}

	// 繪圖模組
	private void drawCanvas(Canvas canvas) {
		Paint pen = new Paint();
		pen.setColor(Color.GREEN);
		// 0406. 以變數 (x, y) 來繪製球的位置(圓心)
		canvas.drawCircle( 100, 100, 30, pen);
	}

	// 0401. 自訂一個更新畫面的執行緒 (UpdatingThread extends Thread)
	class UpdatingThread extends Thread {
		private boolean running = true;
		private SurfaceHolder sfHolder;
		//
		public UpdatingThread(SurfaceHolder sfHolder) {
			this.sfHolder = sfHolder;
		}
		public void stopRunning( ) {
			running = false;
		}
		public void run( ) {
			while (running) {
				// 利用 sfHolder 取得(鎖定) Canvas
				Canvas canvas = sfHolder.lockCanvas();
				if(canvas == null) continue;

				// 更新資料 // 釋放 CPU: sleep(…);
				x += 3;
				y += 5;
				//
				canvas.drawColor(Color.WHITE);
				//
				Paint pen = new Paint();
				pen.setColor(Color.RED);
				canvas.drawCircle( x, y, r, pen);
				//
				sfHolder.unlockCanvasAndPost(canvas);
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
