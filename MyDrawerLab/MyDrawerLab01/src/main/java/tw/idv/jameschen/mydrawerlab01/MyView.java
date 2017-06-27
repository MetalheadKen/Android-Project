package tw.idv.jameschen.mydrawerlab01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by user on 2016/12/13.
 */
public class MyView extends View {

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    //

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //
        Paint pen = new Paint();
        pen.setColor(Color.RED);
        canvas.drawCircle(400, 400, 50, pen);
    }
}
