package com.example.blockgamelab01;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

public class Ball {
	private int x, y;     // 圓心坐標
	private int r;        // 半徑
	private int vx, vy;   // v = (vx, vy); tan(x) = vy/vx;
	private int color;    // 顏色

	// 球的初始資訊
	public Ball(int x, int y, int r, int vx, int vy, int cc) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.vx = vx;
		this.vy = vy;
		this.color = cc;
	}

	// 畫球
	public void draw(Canvas canvas) {
		Paint paint = new Paint();// 新增畫筆
		paint.setColor(color);// 設定畫筆顏色
		canvas.drawCircle(x, y, r, paint);
	}

	// 移動
	public void move(int height, int width) {
		x += vx;
		y += vy;
		//
		if(x <= r || x + r >= width)
			vx = -vx;
		if(y <= r || y + r >= height)
			vy = -vy;
	}

	public void move2(int height, int width, Bar bar, ArrayList<Block> blocks) {
		x += vx;
		y += vy;
		//
		//判斷是否超出邊界
		if(x <= r || x + r >= width)
			vx = -vx;
		if(y <= r || y + r >= height)
			vy = -vy;
		//
		//判斷是否在畫面最下面
		if(y + r >= height )
		{
			//Toast.makeText(context, "GAME OVER！！！", Toast.LENGTH_SHORT).show();
			vy = -vy;
			Log.e("GAME OVER！！！", y + r + "");
		}

		//
		//判斷是否碰到檔板
		if(isCollision(bar))
			vy = -vy;
		//
		//判斷是否碰到磚塊
		if(isCollision(blocks))
			vy = -vy;
	}

	Rect getRect() {
		return new Rect(x - r, y - r, x + r, y + r);
	}

	//判斷是否碰撞
	private boolean isCollision( Block bb) {
		Rect r1 = this.getRect();
		Rect r2 = bb.getRect();
		if (r1.intersect(r2) == true) {
			return true;
		}
		return false;
	}

	private boolean isCollision( Bar bar) {
		Rect r1 = this.getRect();
		Rect r2 = bar.getRect();
		if (r1.intersect(r2) == true) {
			return true;
		}
		return false;
	}

	private boolean isCollision(ArrayList<Block> blocks) {
		Rect r1 = this.getRect();
		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block bb = it.next();
			Rect r2 = bb.getRect();
			if (r1.intersect(r2) == true) {
				blocks.remove(bb);
				return true;
			}
		}
		return false;
	}
}
