package com.example.demo;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Manager {
    private static final int TRIM = 5;
    public static GraphicsContext gc;
    public static  int offsetX, offsetY, prevX, prevY;
    public static ArrayList<Button> buttons = new ArrayList<>();
    public static ArrayList<Component> components = new ArrayList<>();
    private static Component held = null;
    private static boolean dragging = false;

    private static final Color BACKGROUND_COLOR = Color.WHITE, GRID_COLOR = Color.LIGHTGRAY, BAR_TRIM = Color.DARKGRAY, BAR_FILL = Color.GRAY;

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
    }

    private static void drawComponents() {//and grid....
        gc.setStroke(GRID_COLOR);
        for(int i = -1; i < GUI_FLOW.cx / IMAGE_WIDTH; i++) gc.strokeLine((i + 1) * IMAGE_WIDTH + offsetX % IMAGE_WIDTH,  -IMAGE_WIDTH + offsetY % IMAGE_WIDTH, (i + 1) * IMAGE_WIDTH + offsetX % IMAGE_WIDTH, GUI_FLOW.cy + offsetY % IMAGE_WIDTH);
        for(int i = -1; i < GUI_FLOW.cy / IMAGE_WIDTH; i++) gc.strokeLine( -IMAGE_WIDTH + offsetX % IMAGE_WIDTH, (i + 1) * IMAGE_WIDTH + offsetY % IMAGE_WIDTH, GUI_FLOW.cx + offsetX % IMAGE_WIDTH, (i + 1) * IMAGE_WIDTH + offsetY % IMAGE_WIDTH);
        for (Component comp : components) comp.drawWith(gc);
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
        int gridX = gridify(e.getX() - offsetX), gridY = gridify(e.getY() - offsetY);
        for (Component comp : components)
            if (gridX == comp.gridx && gridY == comp.gridy) {//if in bounds
                held = comp;
                break;
            }
        prevX = (int) e.getX();
        prevY = (int) e.getY();
        dragging = e.getX() < GUI_FLOW.cx && e.getY() < GUI_FLOW.cy;
        draw();
    }

    static void onMouseReleased(javafx.scene.input.MouseEvent e) {
        if (held != null) {
            held.place();
            held = null;
        }
        draw();
    }
    //TODO: connect to nets
    static void onMouseDragged(javafx.scene.input.MouseEvent e) {
        //System.out.printf("%d, %d \n\t%d, %d —— dragging: %b,   %s\n", offsetX, offsetY, (int)e.getX(), (int)e.getY(), dragging, held);
        if (held == null && dragging) {
            offsetX += (int) e.getX() - prevX;
            offsetY += (int) e.getY() - prevY;
        } else if (held != null) {//we have something selected
            held.tlx += (int) e.getX() - prevX;
            held.tly += (int) e.getY() - prevY;
            held.invalid_location = held.tlx + IMAGE_WIDTH > GUI_FLOW.cx || held.tly + IMAGE_WIDTH > GUI_FLOW.cy || held.tlx < 0 || held.tly < 0;
        }
        prevX = (int) e.getX();
        prevY = (int) e.getY();
        draw();
    }

    static void onMouseClicked(javafx.scene.input.MouseEvent e) {
        
        draw();
    }

    static void onMouseMoved(javafx.scene.input.MouseEvent e) {

        draw();
    }

    private static int gridify (double input) {
        return IMAGE_WIDTH * (int)(input / IMAGE_WIDTH);
    }
}
