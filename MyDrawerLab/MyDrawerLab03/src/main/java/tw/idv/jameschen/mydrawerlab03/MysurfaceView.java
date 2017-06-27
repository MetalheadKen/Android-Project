package tw.idv.jameschen.mydrawerlab03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by user on 2016/12/13.
 */
public class MysurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder myHolder;

    public MysurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        myHolder = getHolder();
        myHolder.addCallback( this );
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if(canvas == null)
            return;
        else
            drawCanvas(canvas);

        holder.unlockCanvasAndPost(canvas);
    }

    protected void drawCanvas(Canvas canvas) {
        Paint pen = new Paint();
        pen.setColor(Color.RED);
        canvas.drawCircle( 400, 400, 50, pen);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    //

}
