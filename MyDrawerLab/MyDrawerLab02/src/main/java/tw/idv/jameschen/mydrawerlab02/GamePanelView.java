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
	// 0202. �ŧi���n�ݩ�
	private int x = 100, y = 100, r = 30;

	// �غc�l
	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 0203. ���ͨñҰʧ�s�e���������
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
		�Y�ϥΦۭq�վ��סA super.onMeasure(...) �N���ݭn�F!
		// Example: �վ�e�׻P����
		int width = widthSize, height = heightSize;
		if (w_mode == MeasureSpec.AT_MOST) {
		   	// �u���ܤp, ���i�W�L�ӭ�!
		   	with = min(VIEW_DEFAULT_WIDTH, widthSize);
		}
		else if (w_mode == MeasureSpec.EXACTLY) {
		    // ���n
			width = widthSize;
		}
		else {
			width = VIEW_DEFAULT_WIDTH;
		}
		// ���׽վ�: �P�W�����W�h!
		 
		// �̫�A�ץ�View���̲ת��e�סB����.
	    setMeasuredDimension(width, height);
	    
	    */
	}
	
	// the method onDraw(...) will be called on demand !
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(...) : �|��ø�򥻩���
		super.onDraw(canvas);
		//
		Paint gPen = new Paint();
		gPen.setColor(Color.GREEN);
		// 0204. �H�ܼ� (x, y) ��ø�s�y����m(���)
		canvas.drawCircle( x, y, r, gPen);
	}
	
	// 0205. �мg onAttachedToWindow �M  onDetachedFromWindow �[�J���n����l�ƻP����B�z�ʧ@.

	// ���Ѩ�L�Ҳեi�H�I�s�G���e�������s
	public void stopNow() {
		myThread.stop();
	}
	
	// 0201. �ۭq�@�ӧ�s�e���������
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
