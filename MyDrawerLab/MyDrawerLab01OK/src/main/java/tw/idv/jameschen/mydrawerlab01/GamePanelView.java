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

	// �ܤֻݭn�@�ӫغc�l.
	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// �D�n�@��ø�ϰϰ�j�p���T�{(�վ�)--�u��դp�B���i���ܤj. [���@�w�ݭn!]
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

	// onDraw(...) �O�t�λ{�����ݭn�ɡA�~�|�i��I�s�A�H�i��e����s�C
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// �Q�� Canvas �i��e��ø�s. �q�`�|�f�t Paint �P Color ����.
		Paint gPen = new Paint();
		gPen.setColor(Color.GREEN);
		canvas.drawRect( 100, 100, 200, 200, gPen);
	}
}
