package com.example.demo;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Manager {
    private static final double step_size = 1, step_number = 100, fluid_viscosity = 1, fluid_density = 1;
    public static final double edge_perc = 0.1;//ignore 1/10th on each side, so center 0.8x0.8
    private static final int TRIM = 5, COMP_RANGE = 10, COMP_HEIGHT = 2, GRID_WIDTH = 1;
    public static GraphicsContext gc;
    public static int offsetX, offsetY, drag_originX, drag_originY, offset_initX, offset_initY, curX = -2, curY = -2, selected_node = -1;
    public static double cur_mouseX, cur_mouseY;
    public static ArrayList<Button> buttons = new ArrayList<>();
    public static ArrayList<Component> components = new ArrayList<>(), templates = new ArrayList<>();
    private static Component held = null, selected = null;
    private static boolean dragging = false, right_held = false;
    public static boolean unhighlight_next = false, graphs = false;
    private static HashMap<Integer, double[]> node_data_map = new HashMap<>();
    private static final Color
            BACKGROUND_COLOR = Color.WHITE,
            GRID_COLOR = Color.LIGHTGRAY,
            BAR_TRIM = Color.DARKGRAY,
            BAR_FILL = Color.GRAY,
            DARK_HIGHLIGHT_COLOR = Color.color(0.8, 0.8, 0.8),
            LIGHT_HIGHLIGHT_COLOR = Color.color(0.92, 0.92, 0.92),
            DARK_TRIM = Color.color(0.3, 0.3, 0.3),
            GRAPH_COLOR = Color.color(0.95, 0.95, 0.95),
            GRAPH_TRIM = Color.color(0.7, 0.7, 0.7),
            GRAPH_LINE = Color.color(0.4, 0.4, 0.8);


    static void init(GraphicsContext gc_in, String path) {
        gc = gc_in;
        offsetX = 0;
        offsetY = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(GUI_FLOW.join(path, "index.txt")));
            final int offsetx = (GUI_FLOW.cx - COMP_RANGE * 80)/2, offsety = (GUI_FLOW.ty - GUI_FLOW.cy - COMP_HEIGHT * 80) / 2;
            AtomicInteger i = new AtomicInteger();
            reader.lines().forEach(str -> {
                String[] split = str.split(",");
                //System.out.println(Arrays.toString(split));
                String[] param = new String[(split.length - 3) / 3];
                double[] mins = new double[(split.length - 3) / 3], maxes = new double[(split.length - 3) / 3];
                for (int q = 4; q < split.length; q += 3) {
                    param[q/3 - 1] = split[q];
                    if (split[q + 1].equals("?")) split[q + 1] = "-1";
                    if (split[q + 2].equals("?")) split[q + 2] = "-1";
                    mins[q/3 - 1] = Double.parseDouble(split[q + 1]);
                    maxes[q/3 - 1] = Double.parseDouble(split[q + 2]);
                }
                Component temp = new Component(split[1],
                        split[0],offsetx + 80 * (i.get() % COMP_RANGE),
                        GUI_FLOW.cy + offsety + 80 * (i.get() / COMP_RANGE),
                        Integer.parseInt(split[2]),
                        Integer.parseInt(split[3]),
                        param, mins, maxes);
                templates.add(temp);
                i.getAndIncrement();
                if (i.get() == COMP_RANGE * COMP_HEIGHT) throw new RuntimeException("Too many elements configured!");
            });
        } catch (IOException e) {}
    }

    public static void save () {
        HashMap<String, Integer> coord_to_id = new HashMap<>();
        Edge.fill(coord_to_id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");
        final String path = GUI_FLOW.join(GUI_FLOW.basedir, "data", "inputs", "input" + LocalDateTime.now().format(formatter) + ".txt");
        try {
            int[] ids = new int[templates.size()];
            System.out.println(path);
            PrintWriter writer = new PrintWriter(new FileWriter(path));
            for (Component comp : components) {
                int id = -1;
                for (int i = 0; i < templates.size(); i++)
                    if (templates.get(i).name.equals(comp.name))
                        id = ids[i]++;
                if (id == -1) throw new RuntimeException(comp.name);
                writer.write(comp.to_be_processed(coord_to_id, id) + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {}
        //GUI_FLOW.runWillTerminate(GUI_FLOW.pythonCommand + String.format(" %s %s %.8f %f %.2f %.2f\n", "/Users/zachary/Documents/FLOW/python/txt_class_converter.py", "/Users/zachary/Documents/FLOW/data/inputs/input2024-11-17-1119.txt", step_size, step_number, fluid_density, fluid_viscosity));
        load(path);
    }

    public static void load(String path) {
        path = path.replace("input", "result");
        path = "/Users/zachary/Documents/FLOW/data/outputs/outputZ.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String curLine = reader.readLine();
            int comps = Integer.parseInt(curLine.split(" ")[3]),
                    nodes = Integer.parseInt(curLine.split(" ")[2]);
            double step_count = Double.parseDouble(curLine.split(" ")[1]),
                    step_size = Double.parseDouble(curLine.split(" ")[0]);
            node_data_map = new HashMap<>();
            for (int j = 0; j < nodes; j++) {
                curLine = reader.readLine();
                String[] split = curLine.split(",", 2);
                int node = Integer.parseInt(split[0]);
                split = split[1].split(",");
                double[] values = new double[split.length];
                for (int i = 0; i < split.length; i++) values[i] = Double.parseDouble(split[i]);
                node_data_map.put(node, values);
            }
            for (int j = 0; j < comps; j++){
                curLine = reader.readLine();
                String[] split = curLine.split(",", 2);
                Component cq = null;
                for (Component comp : components)
                    if (comp.uniquename.equals(split[0]))  cq = comp;
                if (cq == null) {
                    System.out.println("No match for " + split[0]);
                    if (1 < 2) continue;
                    throw new RuntimeException("No match for " + split[0]);
                }
                split = split[1].split(",");
                double[] values = new double[split.length];
                for (int i = 0; i < split.length; i++) values[i] = Double.parseDouble(split[i]);
                cq.recorded_values = values;
                System.out.println(Arrays.toString(values));
            }
        } catch (IOException ignored) {}
    }

    private static void draw() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, GUI_FLOW.tx, GUI_FLOW.ty);
        drawComponents();
        drawBar();
        if (held != null) held.drawWith(gc, 80, false);
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
        else if (!right_held){
            double tiebreakerX = modW(cur_mouseX - offsetX) / IMAGE_WIDTH,
                    tiebreakerY = modW(cur_mouseY - offsetY) / IMAGE_WIDTH;
            if (!((tiebreakerX > 1 - edge_perc || tiebreakerX < edge_perc) && (tiebreakerY > 1 - edge_perc || tiebreakerY < edge_perc))) {
                int[] l = getEdgeCoordsFromCur();
                Edge.shadowDraw(gc, l[0], l[1], l[2], l[3]);
            }
        }

        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(GRID_WIDTH);
        for(int i = -1; i <= GUI_FLOW.cx / IMAGE_WIDTH; i++) gc.strokeLine((i + 1) * IMAGE_WIDTH + modW(offsetX),  0, (i + 1) * IMAGE_WIDTH + modW(offsetX), GUI_FLOW.cy);
        for(int i = -1; i <= GUI_FLOW.cy / IMAGE_WIDTH; i++) gc.strokeLine( 0, (i + 1) * IMAGE_WIDTH + modW(offsetY), GUI_FLOW.cx, (i + 1) * IMAGE_WIDTH + modW(offsetY));
        for (Component comp : components) if (comp != held) comp.drawWith(gc, 80, false);
        for (Edge edge : Edge.edges) edge.drawWith(gc);
        if (graphs) {
            ArrayList<Integer> already_drawn = new ArrayList<>();
            for (Edge edge : Edge.edges) {
                int node = edge.getNode();
                if (already_drawn.contains(node)) continue;
                if (!node_data_map.containsKey(node)) continue;
                if (selected_node != node) continue;
                already_drawn.add(node);
                drawGraph(gc, 200, node_data_map.get(node), edge.start_x * IMAGE_WIDTH / 2.0 + offsetX, edge.start_y * IMAGE_WIDTH / 2.0 + offsetY);
            }

            for (Component comp : components)
                if (comp.inTransit) drawGraph(gc, 200, comp.recorded_values, comp.tlx, comp.tly);
                else if(comp.highlighted || comp.higher_highlighted) drawGraph(gc, 200, comp.recorded_values, (comp.gridx - 1) * IMAGE_WIDTH / 2.0 + offsetX, (comp.gridy - 1) * IMAGE_WIDTH / 2.0 + offsetY);
        }
    }

    private static void drawBar() {
        gc.setFill(BAR_TRIM);
        gc.fillRect(0, GUI_FLOW.cy, GUI_FLOW.tx, GUI_FLOW.ty);
        gc.fillRect(GUI_FLOW.cx, 0, GUI_FLOW.tx, GUI_FLOW.ty);
        gc.setFill(BAR_FILL);
        gc.fillRect(TRIM, TRIM + GUI_FLOW.cy, GUI_FLOW.tx - 2 * TRIM, GUI_FLOW.ty - GUI_FLOW.cy - 2 * TRIM);
        gc.fillRect(GUI_FLOW.cx + TRIM, TRIM, GUI_FLOW.tx - GUI_FLOW.cx - 2 * TRIM, GUI_FLOW.ty - 2 * TRIM);

        gc.setFill(DARK_TRIM);
        for (Component template : templates) {
            gc.fillRect(template.tlx - TRIM, template.tly - TRIM, 80 + 2 * TRIM, 80 + 2 * TRIM);
        }
        gc.setFill(BACKGROUND_COLOR);
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(GRID_WIDTH);
        for (Component template : templates) {
            gc.setFill(BACKGROUND_COLOR);
            gc.fillRect(template.tlx, template.tly, 80, 80);
            gc.strokeLine(template.tlx, template.tly, template.tlx, template.tly + 80);
            gc.strokeLine(template.tlx + 80, template.tly, template.tlx + 80, template.tly + 80);
            template.drawWith(gc, 80, false);
        }

        final double border = (GUI_FLOW.tx - GUI_FLOW.cx - 160.0) / 2;
        gc.setFill(DARK_TRIM);
        gc.fillRect(GUI_FLOW.cx + border - TRIM, border - TRIM, 160 + 2 * TRIM, 160 + 2 * TRIM);
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(GUI_FLOW.cx + border, border, 160, 160);
        if (selected != null) selected.drawAt(gc, 160, GUI_FLOW.cx + border, border, true);

        for (Button button : buttons) button.drawWith(gc);
    }

    static void onMousePressed(javafx.scene.input.MouseEvent e) {
        right_held = e.getButton() == MouseButton.SECONDARY;
        updateCur(e);
        //find component picked up.
        held = getComponentAtMouse(e);
        if (e.getX() < GUI_FLOW.cx && e.getY() > GUI_FLOW.cy) {//check if pressed over one of the bins
            for (Component template : templates)
                if (template.tlx < e.getX() && template.tlx + 80 > e.getX() &&
                        template.tly < e.getY() && template.tly + 80 > e.getY()) {//if hovering over a template
                    //System.out.println("match: " + template.name);
                    held = new Component(template);
                    //System.out.println("match: " + held.name);
                    components.add(held);
                    break;
                }
            if (held != null) {
                held.initx = held.tlx;
                held.inity = held.tly;
                //inTransit is true by default
            }
        } else if (held != null) {
            held.initx = IMAGE_WIDTH * ((held.gridx - 1) / 2) + offsetX;
            held.inity = IMAGE_WIDTH * ((held.gridy - 1) / 2) + offsetY;
            held.tlx = held.initx;
            held.tly = held.inity;
            held.inTransit = true;
        }
        drag_originX = (int) e.getX();
        drag_originY = (int) e.getY();
        offset_initX = offsetX;
        offset_initY = offsetY;
        dragging = held != null || isCenter(e) && e.getX() < GUI_FLOW.cx && e.getY() < GUI_FLOW.cy;
        draw();
    }

    static void onMouseReleased(javafx.scene.input.MouseEvent e) {
        right_held = false;
        updateCur(e);
        if (held != null) {
            held.place();
            held = null;
        }
        draw();
    }
    //TODO: connect to nets
    static void onMouseDragged(javafx.scene.input.MouseEvent e) {
        updateCur(e);
        if (held == null && dragging) {
            offsetX = offset_initX + (int) e.getX() - drag_originX;
            offsetY = offset_initY + (int) e.getY() - drag_originY;
        } else if (held == null) {
            if (e.getButton() == MouseButton.SECONDARY){
                int[] l = getEdgeCoordsFromCur();
                Edge cur = Edge.getEdge(l[0], l[1], l[2], l[3]);
                if (cur != null) Edge.removeEdge(cur);
            } else Edge.tryAddEdgeFromCur();
        } else if (dragging) {//we have something selected
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
        //for (Component comp : components) System.out.println(comp.name);
        updateCur(e);//update cur

        if (e.getX() < GUI_FLOW.cx && e.getY() < GUI_FLOW.cy) {
            if (selected != null) selected.higher_highlighted = false;

            Component old = selected;
            selected = getComponentAtMouse(e);
            if (selected != null && selected != old) {
                selected.higher_highlighted = true;
                open_edit_window();
            }
        }
        if (!isCenter(e)) {
            if (e.getButton() == MouseButton.SECONDARY) {
                int[] l = getEdgeCoordsFromCur();
                Edge cur = Edge.getEdge(l[0], l[1], l[2], l[3]);
                if (cur != null) {
                    if (!GUI_FLOW.shift) Edge.removeEdge(cur);
                    else {
                        int cur_node = cur.getNode();
                        ArrayList<Edge> tbr = new ArrayList<>();
                        for (Edge edge : Edge.edges) if (edge.getNode() == cur_node) tbr.add(edge);
                        Edge.removeAll(tbr);
                    }
                }
            } else {
                Edge.tryAddEdgeFromCur();
                int[] l = getEdgeCoordsFromCur();
                Edge cur = Edge.getEdge(l[0], l[1], l[2], l[3]);
                if (cur != null) {
                    int old = selected_node;
                    selected_node = cur.getNode();
                    if (old == selected_node && unhighlight_next) selected_node = -1;
                    unhighlight_next = true;
                }
            }
        } else selected_node = -1;

        draw();
    }

    static void onMouseMoved(javafx.scene.input.MouseEvent e) {
        updateCur(e);

        if (isCenter(e)) //if in double odd coord (in square)
            for (Component comp : components) comp.highlighted = comp.gridx == curX && comp.gridy == curY;
        draw();
    }

    private static void updateCur(javafx.scene.input.MouseEvent e) {
        curX = gridify(e.getX(), offsetX);
        curY = gridify(e.getY(), offsetY);
        cur_mouseX = e.getX();
        cur_mouseY = e.getY();
        for (Button button : buttons) button.update(e.getX(), e.getY(), e.isPrimaryButtonDown());

    }

    static int gridify (double coord, int offset) {
        return gridify(coord, offset, false);
    }

    static int gridify (double coord, int offset, boolean square_only) {
        int flattened = (int) (coord - offset) / IMAGE_WIDTH;
        if (coord - offset < 0) flattened--;//correction for integer division going to 0 instead of to next lowest int.
        flattened *= 2;
        if (square_only) return flattened + 1;//no correction to get to edge, ie we stay in square.

        double rem = modW(coord - offset) / IMAGE_WIDTH;//correction for inside being odd, and outside being even.
        if (rem > edge_perc) flattened ++;//move from edge to center
        if (rem >= 1 - edge_perc) flattened ++;//move from center to edge
        return flattened;
    }

    static double modW (double input) {
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

    private static Component getComponentAtMouse(javafx.scene.input.MouseEvent e) {
        if (isCenter(e)) {//if in center of a square
            int gridX = gridify(e.getX(), offsetX, true), gridY = gridify(e.getY(), offsetY, true);
            for (Component comp : components)
                if (gridX == comp.gridx && gridY == comp.gridy) //if in bounds
                    return comp;
        }
        return null;
    }

    static int[] getEdgeCoordsFromCur() {
        int tempx = (curX < 0 ? -curX : curX) % 2, tempy = (curY < 0 ? -curY : curY) % 2;
        double tiebreakerX = modW(cur_mouseX - offsetX) / IMAGE_WIDTH, tiebreakerY = modW(cur_mouseY - offsetY) / IMAGE_WIDTH;
        if (tempx == 0 && tempy == 0) {//corner
            if (tiebreakerX > 0.5) tiebreakerX -= 1;
            if (tiebreakerY > 0.5) tiebreakerY -= 1;
            double angle = Math.atan2(tiebreakerY, tiebreakerX) + Math.PI;//[0, 2π)

            if (angle > Math.PI / 4 && angle < 3 * Math.PI / 4) tempy = -1;//go "up" //if angle is between π/4 and 3π/4, we go up
            else if (angle > 3 * Math.PI / 4 && angle < 5 * Math.PI / 4) tempx = 1;//go left
            else if (angle > 5 * Math.PI / 4 && angle < 7 * Math.PI / 4) tempy = 1;//go "down"
            else tempx = -1;//go right
        } else if (tempx == 1) {//between two vertical boxes. Make horizontal.
            if (tiebreakerX < 0.5) tempx = -1;
            //else tempx = 1;//redundant
        } else {//between two horizontal boxes. Make vertical.
            if (tiebreakerY < 0.5) tempy = -1;
            //else tempy = 1;//redundant
        }
        return new int[]{curX, curY, curX + tempx, curY + tempy};
    }

    public static void drawGraph (GraphicsContext gc, int size, double[] points, double tlx, double tly) {
        //points = new double[]{0.2, 0.8, 0.9, 1.2, 1.3, 1.8};
        if (points == null || points.length <= 1) return;

        gc.setFill(GRAPH_TRIM);
        gc.fillRect(tlx, tly - size, size, size);
        gc.setFill(GRAPH_COLOR);
        gc.fillRect(tlx + TRIM, tly - size + TRIM, size - 2 * TRIM, size - 2 * TRIM);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        final int offsetl = 30, offset = 5;
        gc.strokeLine(tlx + TRIM + offsetl, tly - size + TRIM + offset, tlx + TRIM + offsetl, tly - TRIM - offset);
        gc.strokeLine(tlx + TRIM + offsetl, tly - TRIM - offset, tlx + size - TRIM - offset, tly - TRIM - offset);

        double min = points[0], max = points[0];
        for (double point : points) {
            if (point < min) min = point;
            if (point > max) max = point;
        }
        gc.setStroke(GRAPH_LINE);
        gc.setLineWidth(1);
        double step_size = (double) (size - 2 * TRIM - offsetl - offset) / (points.length - 1);
        for (int i = 0; i < points.length - 1; i++) {
            double scaled1 = (points[i] - min) / (max - min),
                    scaled2 = (points[i + 1] - min) / (max - min);
            gc.strokeLine(
                    tlx + TRIM + offsetl + i * step_size,
                    tly - TRIM - offset - scaled1 * (size - 2 * TRIM - 2 * offset),
                    tlx + TRIM + offsetl + (i + 1) * step_size,
                    tly - TRIM - offset - scaled2 * (size - 2 * TRIM - 2 * offset)
            );
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setStroke(Color.BLACK);
        gc.strokeText(String.format("%.2f", max), tlx + TRIM + offsetl - 3, tly - size + TRIM + offset + 5);
        gc.strokeText(String.format("%.2f", min), tlx + TRIM + offsetl - 3, tly - TRIM - offset - 5);
    }

    private static void open_edit_window() {
        if (selected == null) return;
        final Component q = selected;
        Stage temp = new Stage();
        temp.setResizable(false);
        BorderPane temper = new BorderPane();
        Scene TEMP = new Scene(temper);
        temp.setScene(TEMP);
        VBox box = new VBox();
        temper.setCenter(box);
        TextField[] inputs = new TextField[q.parameters.length];
        for (int i = 0; i < q.parameters.length; i++) {
            Label tempiest = new Label(q.parameters[i]);
            //tempiest.setStyle("-fx-text-fill: red");
            tempiest.setFocusTraversable(false);
            inputs[i] = new TextField("" + q.stored_values[i]);
            inputs[i].setEditable(true);
            box.getChildren().add(tempiest);
            box.getChildren().add(inputs[i]);
            final int index = i;
            inputs[i].setOnMouseExited(e -> {
                double val;
                try {
                   val = Double.parseDouble(inputs[index].getText());
                   if (val < q.mins[index] || val > q.maxes[index])
                       throw new NumberFormatException(String.format(q.parameters[index] + " must be in range %.2f to %.2f!", q.mins[index], q.maxes[index]));
                } catch (NumberFormatException f) {
                    Stage error = new Stage();
                    temp.setResizable(false);
                    BorderPane holder = new BorderPane();
                    Scene scenic = new Scene(holder);
                    error.setScene(scenic);
                    holder.setCenter(new Label(f.getMessage()));
                    error.show();
                }
            });
        }
        //Label tempiest = new Label(tbc.toString());
        //tempiest.setFocusTraversable(false);

        //temper.setCenter(tempiest);
        temp.show();
        temp.setOnCloseRequest(e -> {
            for (int i = 0; i < inputs.length; i++)
                q.stored_values[i] = Double.parseDouble("0" + inputs[i].getText());
        });
    }
}
