package com.example.blockgamelab03;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

public class Block {
    int x, y;
    int w, h;
    int color;
    Paint pen = new Paint();
    
    public Block(int x, int y, int w, int h, int cc) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = cc;
    }
    //
    public void draw(Canvas gr) {
        pen.setColor(color);
        pen.setStyle(Style.FILL);
        gr.drawRect(x, y, x+w, y+h, pen);
        pen.setColor(color);
        pen.setStyle(Style.FILL);
        gr.drawRect(x, y, x+w, y+h, pen); 
    }

    public Rect getRect() {
        return new Rect( x, y, x+w, y+h);
    }
}