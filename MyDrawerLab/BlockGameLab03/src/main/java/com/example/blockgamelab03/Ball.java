package com.example.blockgamelab03;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Iterator;

public class Ball {
	private int x, y; // 圓心坐標
	private int r; // 半徑
	private int vx, vy; // v = (vx, vy); tan(x) = vy/vx;
	private int color; // 顏色

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
		if (x <= r || x+r >= width) {
			vx = - vx;
		}
		if (y <= r || y+r >= height) {
			vy = -vy;
		}
	}

	Rect getRect() {
		return new Rect(x - r, y - r, x + r, y + r);
	}

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
