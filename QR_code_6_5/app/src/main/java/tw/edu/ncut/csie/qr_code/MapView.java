package tw.edu.ncut.csie.qr_code;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by user on 2017/2/27.
 */

public class MapView extends ImageView {
    /* 初始狀態的 Matrix */
    private Matrix mapMatrix = new Matrix();
    /* 進行變動狀況下的 Matrix */
    private Matrix mapChangeMatrix = new Matrix();
    /* 圖片的 Bitmap */
    private Bitmap map = null;
    /* 手機畫面尺寸資訊 */
    private DisplayMetrics mapDisplayMetrics;
    /* 設定縮放最小比例 */
    private float mapMinScale = 0.8f;
    /* 設定縮放最大比例 */
    private float mapMaxScale = 5.0f;

    /* 圖片狀態 - 初始狀態 */
    private  static final int STATE_NONE = 0;
    /* 圖片狀態 - 拖動狀態 */
    private static final int STATE_DRAG = 1;
    /* 圖片狀態 - 縮放狀態 */
    private static final int STATE_ZOOM = 2;
    /* 當下的狀態 */
    private int mapState = STATE_NONE;

    /* 第一點按下的座標 */
    private PointF mapFirstPointF = new PointF();
    /* 第二點按下的座標 */
    private PointF mapSecondPointF = new PointF();
    /* 兩點距離 */
    private float mapDistance = 1f;
    /* 圖片中心座標 */
    private float mapCenterX, mapCenterY;

    /* MapView 類別，用 XML 呼叫來運用 */
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /* 取得地圖 */
        BitmapDrawable mapDrawable = (BitmapDrawable) this.getDrawable();
        if(mapDrawable != null)
        {
            map = mapDrawable.getBitmap();
            ScaleMap();
        }
    }

    /* 圖片縮放層級設定 */
    private void Scale() {
        /* 取得圖片縮放的層級 */
        float level[] = new float[9];
        mapMatrix.getValues(level);

        /* 狀態為縮放時進入 */
        if (mapState == STATE_ZOOM) {
            /* 若層級小於 1 則縮放至原始大小 */
            if (level[0] < mapMinScale) {
                mapMatrix.setScale(mapMinScale, mapMinScale);
                mapMatrix.postTranslate(mapCenterX, mapCenterY);
            }

            /* 若縮放層級大於最大層級則顯示最大層級 */
            if (level[0] > mapMaxScale) mapMatrix.set(mapChangeMatrix);
        }
    }

    /* 兩點距離 */
    private float Spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /* 兩點中心 */
    private void MidPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /* 地圖縮放設定 */
    public void ScaleMap()
    {
        /* 取得 Context */
        Context mapContext = getContext();
        /* 取得手機畫面尺寸資訊 */
        mapDisplayMetrics = mapContext.getResources().getDisplayMetrics();

        /* 設置縮放的型態 */
        this.setScaleType(ScaleType.MATRIX);
        /* 將 Bitmap 帶入 */
        this.setImageBitmap(map);

        /* 將圖片放置畫面中央 */
        mapCenterX = 0.0f;
        mapCenterY = 0.0f;
        mapMatrix.setScale(mapMinScale, mapMinScale);
        mapMatrix.postTranslate(mapCenterX, mapCenterY);

        /* 將 mapMatrix 帶入 */
        this.setImageMatrix(mapMatrix);

        /* 設置Touch觸發的Listener動作 */
        this.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                /* 判斷觸控的動作 */
                switch(event.getAction() & MotionEvent.ACTION_MASK)
                {
                    /* 第一點按下時 */
                    case MotionEvent.ACTION_DOWN :
                        mapChangeMatrix.set(mapMatrix);
                        mapFirstPointF.set(event.getX(), event.getY());
                        mapState = STATE_DRAG;
                        break;

                    /* 當第一點按下，再按下其他點時 */
                    case MotionEvent.ACTION_POINTER_DOWN :
                        mapDistance = Spacing(event);
                        /* 只要兩點距離大於 10 就判定為地圖縮放 */
                        if (Spacing(event) > 10f) {
                            mapChangeMatrix.set(mapMatrix);
                            MidPoint(mapSecondPointF, event);
                            mapState = STATE_ZOOM;
                        }
                        break;

                    /* 全部點離開觸碰時 */
                    case MotionEvent.ACTION_UP :
                        break;

                    /* 其中一個點離開觸碰時，狀態恢復 */
                    case MotionEvent.ACTION_POINTER_UP :
                        mapState = STATE_NONE;
                        break;

                    /* 移動地圖時 */
                    case MotionEvent.ACTION_MOVE :
                        if (mapState == STATE_DRAG) {
                            mapMatrix.set(mapChangeMatrix);
                            mapMatrix.postTranslate(event.getX() - mapFirstPointF.x, event.getY() - mapFirstPointF.y);
                        }
                        else if (mapState == STATE_ZOOM) {
                            float NewDistance = Spacing(event);
                            if (NewDistance > 10f) {
                                mapMatrix.set(mapChangeMatrix);
                                float NewScale = NewDistance / mapDistance;
                                mapMatrix.postScale(NewScale, NewScale, mapSecondPointF.x, mapSecondPointF.y);
                            }
                        }
                        break;
                }

                /* 將 mapMatrix 滑動縮放控制帶入 */
                MapView.this.setImageMatrix(mapMatrix);
                /* 限制縮放大小 */
                Scale();

                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* 未傳送路徑 */
        if (pointLength == 0) return;

        /* 儲存地圖的長與高 */
        imgWidth = map.getWidth();
        imgHeight = map.getHeight();

        /* 在 ImageView 畫出地圖以利之後的儲存圖片之動作 */
        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.drawable.map).copy(Bitmap.Config.ARGB_8888, true);
        temp = Bitmap.createScaledBitmap(temp, imgWidth, imgHeight, false);

        /* 在 Matrix 上儲存箭頭以利之後的旋轉之動作 */
        Matrix matrix = new Matrix();
        matrix.postRotate(arrowDegree);

        Bitmap arrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow).copy(Bitmap.Config.ARGB_8888, true);
        arrow = Bitmap.createScaledBitmap(arrow, 100, 100, false);
        arrow = Bitmap.createBitmap(arrow, 0, 0, 100, 100, matrix, true);

        Paint paint = new Paint();
        Canvas routeCanvas = new Canvas(temp);

        /* 畫出地圖 */
        routeCanvas.drawBitmap(temp, 0, 0, paint);

        /* 設置畫筆 */
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);

        /* 畫出路徑 */
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < pointLength - 1; i++) {
            /* 修正轉角Bug */
            if (coordinate[i][0] == 999 && coordinate[i][1] == 999)
                coordinate[i] = coordinate[i - 1];
            else if (coordinate[i + 1][0] == 999 && coordinate[i + 1][1] == 999)
                coordinate[i + 1] = coordinate[i + 2];

            routeCanvas.drawLine(coordinate[i][0] * imgWidth / 1199, coordinate[i][1] * imgWidth / 1199, coordinate[i + 1][0] * imgWidth / 1199, coordinate[i + 1][1] * imgWidth / 1199, paint);
        }

        /* 畫出終點 */
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        routeCanvas.drawCircle(coordinate[pointLength - 1][0] * imgWidth / 1199, coordinate[pointLength - 1][1] * imgWidth / 1199, 10, paint);

        /* 畫出箭頭 */
        routeCanvas.drawBitmap(arrow, coordinate[0][0] * imgWidth / 1199 - 50, coordinate[0][1] * imgWidth / 1199 - 50, paint);

        /* 更新地圖 */
        this.setImageBitmap(temp);
    }

    private int imgWidth, imgHeight, pointLength, arrowDegree;

    private int [][] coordinate = new int[64][2];

    public void DrawRoute(int [][] point, int length) {
        coordinate  = point;
        pointLength = length;

        //MapView.this.postInvalidate();
    }

    public void DrawArrow(int orientation) {
        arrowDegree = orientation;

        MapView.this.postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        this.imgWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.imgHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(imgWidth, imgHeight);
    }

    public MapView(Context context) {
        super(context);
    }

}
