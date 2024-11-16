package com.example.demo;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

abstract public class Button {
    private static final int BUTTON_TRIM = 3;
    private static final Color
            EDGE_COLOR = Color.color(0.23, 0.28, 0.25),
            FULL_COLOR = Color.color(0.95, 0.95, 0.95),
            TEXT_COLOR = Color.color(0.1, 0.1, 0.1),
            HOVERED_FILL = Color.color(0.87, 0.87, 0.87),
            PRESSED_FILL = Color.color(0.7, 0.7, 0.7);
    private boolean hovered = false, pressed = false;
    private String text;
    private int tlx, tly, w, h;
    public Button (String text, int tlx, int tly, int w, int h) {
        this.text = text;
        this.tlx = tlx;
        this.tly = tly;
        this.w = w;
        this.h = h;
    }

    public void drawWith(GraphicsContext gc) {
        gc.setFill(EDGE_COLOR);
        gc.fillRect(this.tlx, this.tly, this.w, this.h);
        if (pressed) gc.setFill(PRESSED_FILL);
        else if (hovered) gc.setFill(HOVERED_FILL);
        else gc.setFill(FULL_COLOR);
        gc.fillRect(this.tlx + BUTTON_TRIM, this.tly + BUTTON_TRIM, this.w - 2 * BUTTON_TRIM, this.h - 2 * BUTTON_TRIM);
        gc.setStroke(TEXT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.strokeText(this.text, this.tlx + this.w/2.0, this.tly + this.h/2.0);
    }

    abstract public void run_action ();

    public void update (double mX, double mY, boolean down) {
        if (pressed && !down) this.run_action();//was pressed, now released!

        hovered = mX > this.tlx && mX < this.tlx + this.w && mY > this.tly && mY < this.tly + this.h;
        pressed = hovered && down;
    }
}
