package com.example.demo;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Manager {
    private static final int TRIM = 5;
    public static GraphicsContext gc;
    public static  int offsetX, offsetY, drag_originX, drag_originY, offset_initX, offset_initY, curX = -2, curY = -2;
    public static ArrayList<Button> buttons = new ArrayList<>();
    public static ArrayList<Component> components = new ArrayList<>();
    private static Component held = null;
    private static boolean dragging = false;

    private static final Color BACKGROUND_COLOR = Color.WHITE, GRID_COLOR = Color.LIGHTGRAY, BAR_TRIM = Color.DARKGRAY, BAR_FILL = Color.GRAY, DARK_HIGHLIGHT_COLOR = Color.color(0.8, 0.8, 0.8), LIGHT_HIGHLIGHT_COLOR = Color.color(0.92, 0.92, 0.92);

    static void init(GraphicsContext gc_in) {
        gc = gc_in;
        offsetX = 0;
        offsetY = 0;
    }

    private static void draw() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, GUI_FLOW.tx, GUI_FLOW.ty);
        drawComponents();
        drawBar();
        if (held != null) held.drawWith(gc);
    }

    private static void drawComponents() {//and grid....
        int x = curX, y = curY;
        if (held != null) {
            gc.setFill(DARK_HIGHLIGHT_COLOR);
            x = gridify(held.tlx + IMAGE_WIDTH / 2.0, offsetX, true);
            y = gridify(held.tly + IMAGE_WIDTH / 2.0, offsetY, true);
        } else gc.setFill(LIGHT_HIGHLIGHT_COLOR);
        //convert from random coords to square coords for the purpose of drawing the rect
        if ((x < 0 ? -x : x) % 2 == 1 && (y < 0 ? -y : y) % 2 == 1)
            gc.fillRect(offsetX + IMAGE_WIDTH *  (x - 1) / 2.0, offsetY + IMAGE_WIDTH * (y - 1) / 2.0, IMAGE_WIDTH, IMAGE_WIDTH);



        gc.setStroke(GRID_COLOR);
        for(int i = -1; i <= GUI_FLOW.cx / IMAGE_WIDTH; i++) gc.strokeLine((i + 1) * IMAGE_WIDTH + modW(offsetX),  0, (i + 1) * IMAGE_WIDTH + modW(offsetX), GUI_FLOW.cy);
        for(int i = -1; i <= GUI_FLOW.cy / IMAGE_WIDTH; i++) gc.strokeLine( 0, (i + 1) * IMAGE_WIDTH + modW(offsetY), GUI_FLOW.cx, (i + 1) * IMAGE_WIDTH + modW(offsetY));
        for (Component comp : components) if (comp != held) comp.drawWith(gc);
    }

    private static void drawBar() {
        gc.setFill(BAR_TRIM);
        gc.fillRect(0, GUI_FLOW.cy, GUI_FLOW.tx, GUI_FLOW.ty);
        gc.fillRect(GUI_FLOW.cx, 0, GUI_FLOW.tx, GUI_FLOW.ty);
        gc.setFill(BAR_FILL);
        gc.fillRect(TRIM, TRIM + GUI_FLOW.cy, GUI_FLOW.tx - TRIM, GUI_FLOW.ty - TRIM);
        gc.fillRect(GUI_FLOW.cx + TRIM, TRIM, GUI_FLOW.tx - TRIM, GUI_FLOW.ty - TRIM);

        for (Button button : buttons) button.drawWith(gc);
    }

    static void onMousePressed(javafx.scene.input.MouseEvent e) {
        //find component picked up.
        if (isCenter(e)) {//if in center of a square
            int gridX = gridify(e.getX(), offsetX, true), gridY = gridify(e.getY(), offsetY, true);
            for (Component comp : components)
                if (gridX == comp.gridx && gridY == comp.gridy) {//if in bounds
                    held = comp;
                    held.initx = IMAGE_WIDTH * ((held.gridx - 1) / 2) + offsetX;
                    held.inity = IMAGE_WIDTH * ((held.gridy - 1) / 2) + offsetY;
                    held.tlx = held.initx;
                    held.tly = held.inity;
                    held.inTransit = true;
                    break;
                }
        }
        drag_originX = (int) e.getX();
        drag_originY = (int) e.getY();
        offset_initX = offsetX;
        offset_initY = offsetY;
        dragging = e.getX() < GUI_FLOW.cx && e.getY() < GUI_FLOW.cy;
        draw();
    }

    static void onMouseReleased(javafx.scene.input.MouseEvent e) {
        if (held != null) {
            held.place();
            held = null;
        }

        curX = gridify(e.getX(), offsetX);
        curY = gridify(e.getY(), offsetY);
        draw();
    }
    //TODO: connect to nets
    static void onMouseDragged(javafx.scene.input.MouseEvent e) {
        if (held == null && dragging) {
            offsetX = offset_initX + (int) e.getX() - drag_originX;
            offsetY = offset_initY + (int) e.getY() - drag_originY;
        } else if (held != null) {//we have something selected
            held.tlx = held.initx + (int) e.getX() - drag_originX;
            held.tly = held.inity + (int) e.getY() - drag_originY;
            held.invalid_location = held.tlx + IMAGE_WIDTH / 2 > GUI_FLOW.cx || held.tly + IMAGE_WIDTH / 2 > GUI_FLOW.cy;
            held.blocked_location = held.tlx + IMAGE_WIDTH/2 < 0 || held.tly + IMAGE_WIDTH/2 < 0;
            final int potx = gridify(held.tlx + IMAGE_WIDTH / 2.0, offsetX, true),
                    poty = gridify(held.tly + IMAGE_WIDTH / 2.0, offsetY, true);
            for (Component comp : components) if (comp != held)
                if (comp.gridx == potx && comp.gridy == poty) {
                    held.blocked_location = true;
                    break;
                }
        }
        draw();
    }

    static void onMouseClicked(javafx.scene.input.MouseEvent e) {

        draw();
    }

    static void onMouseMoved(javafx.scene.input.MouseEvent e) {
        int gridX = gridify(e.getX(), offsetX), gridY = gridify(e.getY(), offsetY);
        curX = gridX;
        curY = gridY;

        if (isCenter(e)) //if in double odd coord (in square)
            for (Component comp : components) comp.highlighted = comp.gridx == gridX && comp.gridy == gridY;
        draw();
    }

    static int gridify (double coord, int offset) {
        return gridify(coord, offset, false);
    }

    static int gridify (double coord, int offset, boolean square_only) {
        final double edge_perc = 0.1;//ignore 1/10th on each side, so center 0.8x0.8
        int flattened = (int) (coord - offset) / IMAGE_WIDTH;
        if (coord - offset < 0) flattened--;//correction for integer division going to 0 instead of to next lowest int.
        flattened *= 2;
        if (square_only) return flattened + 1;//no correction to get to edge, ie we stay in square.

        double rem = modW(coord - offset) / IMAGE_WIDTH;//correction for inside being odd, and outside being even.
        if (rem > edge_perc) flattened ++;//move from edge to center
        if (rem >= 1 - edge_perc) flattened ++;//move from center to edge
        return flattened;
    }

    private static double modW (double input) {
        double temp = input % IMAGE_WIDTH;
        if (temp < 0) temp += IMAGE_WIDTH;
        return temp;
    }

    private static boolean isCenter(javafx.scene.input.MouseEvent e) {
        int c1 = gridify(e.getX(), offsetX);
        int c2 = gridify(e.getY(), offsetY);
        if (c1 < 0) c1 = -c1;
        if (c2 < 0) c2 = -c2;
        return c1 % 2 == 1 && c2 % 2 == 1;
    }
}
