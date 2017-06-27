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
	// 0402 宣告必要屬性
	private UpdatingThread updatingThread;
	int x=100, y=100;
	//
	public GamePanelSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 註冊處理程序
		this.getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 0403. 覆寫 surfaceCreated(...) 產生並啟動更新畫面的執行緒
		updatingThread = new UpdatingThread(holder);
		updatingThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 0404. 覆寫 surfaceDestroyed(...) 加入必要的善後處理動作
		updatingThread.stopRunning();
	}

	// 0405. 提供其他模組可以呼叫：讓畫面停止更新
	public void stopNow() {
		updatingThread.stopRunning();
	}

	// 繪圖模組
	private void drawCanvas(Canvas canvas) {
		Paint pen = new Paint();
		pen.setColor(Color.GREEN);
		// 0406.以變數 (x, y) 來繪製球的位置(圓心)
		canvas.drawCircle( x, y, 30, pen);
	}

	// 0401. 自訂一個更新畫面的執行緒 (UpdatingThread extends Thread)
	class UpdatingThread extends Thread {
		//
		boolean running = true;
		SurfaceHolder holder;
		//
		public UpdatingThread(SurfaceHolder holder) {
			this.holder = holder;
		}
		public void stopRunning() {
			running = false;
		}
		//
		@Override
		public void run() {
			while (running) {
				// Your drawing code
				Canvas canvas = holder.lockCanvas();
				drawCanvas(canvas);
				x += 30;
				holder.unlockCanvasAndPost(canvas);
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
