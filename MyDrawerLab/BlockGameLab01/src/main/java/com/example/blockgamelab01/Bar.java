package com.example.blockgamelab01;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public class Bar {
    int x, y;
    int w, h;
	private int viewWidth;
	private int viewHeight;    
	int color;
    Paint pen = new Paint();

    //
    public Bar(int x, int y, int w, int h, int cc) {
        this.x = x;
        this.y = y ;
        this.w = w;
        this.h = h;
        this.color = cc;
    }
    //
    public void moveLeft() {
        x -= 5;
        if (x < 0) 
            x = 0;
    }
    public void moveRight() {
        x += 5;
        if (x+w > viewWidth) 
            x = viewWidth - w;
    }
    public void moveCenterToPos( int pos) {
    	this.x = pos - w/2;
    }
    //
    public void draw(Canvas gr) {
        pen.setColor(color);
        pen.setStyle(Style.FILL);
        gr.drawRect(x, y, x+w, y+h, pen);
    }
    //
    Rect getRect() {
        return new Rect( x, y, x+w, y+h);
    }
    //
    public void setSize(int width, int height) {
    	this.viewWidth = width;
    	this.viewHeight = height;
    	y = viewHeight - h;
    }
    
}
