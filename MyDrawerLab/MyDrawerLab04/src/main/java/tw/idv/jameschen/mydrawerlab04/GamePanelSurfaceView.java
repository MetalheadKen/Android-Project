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

	// 0402. �ŧi���n�ݩ�
	private int x = 100, y = 100, r = 30;

	//
	public GamePanelSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// ���U�B�z�{��
		this.getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 0403. ���ͨñҰʧ�s�e��������� (���G�ݶǻ� SurfaceHolder �������)
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
		// 0404. �Ȱ������ (���G�ݶǻ� SurfaceHolder �������)
	}

	// 0405. ���Ѩ�L�Ҳեi�H�I�s�G���e�������s
	public void stopNow() {


	}

	// ø�ϼҲ�
	private void drawCanvas(Canvas canvas) {
		Paint pen = new Paint();
		pen.setColor(Color.GREEN);
		// 0406. �H�ܼ� (x, y) ��ø�s�y����m(���)
		canvas.drawCircle( 100, 100, 30, pen);
	}

	// 0401. �ۭq�@�ӧ�s�e��������� (UpdatingThread extends Thread)
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
				// �Q�� sfHolder ���o(��w) Canvas
				Canvas canvas = sfHolder.lockCanvas();
				if(canvas == null) continue;

				// ��s��� // ���� CPU: sleep(�K);
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
