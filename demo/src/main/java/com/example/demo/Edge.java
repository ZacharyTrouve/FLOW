package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Edge {
    public static ArrayList<Edge> edges = new ArrayList<>();
    private static final Color
            EDGE_HIGHLIGHT = Color.color(0.3, 0.5, 0.3),
            EDGE_COLOR = Color.color(0.3, 0.3, 0.3),
            SHADOW_COLOR = Color.color(0.3, 0.3, 0.3, 0.4);
    private static int nextID = 0;

    private static final int STROKE_THICK = 5, STROKE_DEFAULT = 3;
    private final int start_x, start_y, end_x, end_y;
    private int node;
    private Edge parent_node;
    public Edge (int startx, int starty, int endx, int endy) {
        this.start_x = startx;
        this.start_y = starty;
        this.end_x = endx;
        this.end_y = endy;
    }

    public void drawWith(GraphicsContext gc) {
        if (this.getNode() == Manager.selected_node) {
            gc.setStroke(EDGE_HIGHLIGHT);
            gc.setLineWidth(STROKE_THICK);
        } else {
            gc.setStroke(EDGE_COLOR);
            gc.setLineWidth(STROKE_DEFAULT);
        }
        gc.strokeLine(this.start_x * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetX,
                this.start_y * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetY,
                this.end_x * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetX,
                this.end_y * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetY);
    }

    public static void shadowDraw(GraphicsContext gc, int x1, int y1, int x2, int y2) {
        gc.setStroke(SHADOW_COLOR);
        gc.setLineWidth(STROKE_DEFAULT);
        gc.strokeLine(x1 * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetX,
                y1 * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetY,
                x2 * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetX,
                y2 * GUI_FLOW.IMAGE_WIDTH / 2.0 + Manager.offsetY);
    }

    public static void tryAddEdgeFromCur() {
        if (Manager.cur_mouseX > GUI_FLOW.cx || Manager.cur_mouseY > GUI_FLOW.cy || Manager.cur_mouseX < 0 || Manager.cur_mouseY < 0) return;//out of bounds
        double tiebreakerX = Manager.modW(Manager.cur_mouseX - Manager.offsetX) / IMAGE_WIDTH,
               tiebreakerY = Manager.modW(Manager.cur_mouseY - Manager.offsetY) / IMAGE_WIDTH;
        if ((tiebreakerX > 1 - Manager.edge_perc || tiebreakerX < Manager.edge_perc) && (tiebreakerY > 1 - Manager.edge_perc || tiebreakerY < Manager.edge_perc))
            return;//corner creates an invalid edge

        int[] l = Manager.getEdgeCoordsFromCur();
        int sum = Math.abs(l[2] - l[0]) + Math.abs(l[3] - l[1]);
        if (sum != 1) return;
        if (Edge.getEdge(l[0], l[1], l[2], l[3]) == null) //create edge! and not diagonal or zero
            Edge.addEdge(l[0], l[1], l[2], l[3]);
        Manager.unhighlight_next = false;
    }

    private static void addEdge(int x1, int y1, int x2, int y2) {
        Edge newEdge = new Edge(x1, y1, x2, y2);
        integrateEdge(newEdge);
    }

    private static void integrateEdge (Edge input) {
        if (edges.contains(input)) throw new RuntimeException();
        Edge start = null, end = null;
        for (Edge edge : edges)
            if (edge.start_x == input.start_x && edge.start_y == input.start_y ||
                    edge.end_x == input.start_x && edge.end_y == input.start_y)
                start = edge;
            else if (edge.start_x == input.end_x && edge.start_y == input.end_y ||
                    edge.end_x == input.end_x && edge.end_y == input.end_y)
                end = edge;

        if (start == null && end == null) {
            input.node = nextID++;
            input.parent_node = input;
        } else if (end == null) input.parent_node = start.getParent();//only start is non-null
        else if (start == null) input.parent_node = end.getParent();//only end is non-null
        else {//both non-null
            input.parent_node = start.getParent();
            input.parent_node.parent_node = end.getParent();
        }
        Manager.selected_node = input.getNode();//update rippling down to start to start.getParent etc... to start.ancestor to end.getParent()
        edges.add(input);
    }

    public static void removeEdge (Edge tbr) {
        ArrayList<Edge> old_edges = new ArrayList<>(edges);
        edges.clear();
        nextID = 0;
        for (Edge edge : old_edges) if (edge != tbr)/*if (!(
                edge.start_x == x1 && edge.start_y == y1 ||
                edge.end_x == x1 && edge.end_y == y1 ||
                edge.start_x == x2 && edge.start_y == y2 ||
                edge.end_x == x2 && edge.end_y == y2))*///all edges except the one added
            integrateEdge(edge);
        //clear and restart.
        Manager.selected_node = -1;
        Manager.unhighlight_next = true;
    }

    public static void removeAll (ArrayList<Edge> tbr) {
        ArrayList<Edge> old_edges = new ArrayList<>(edges);
        edges.clear();
        nextID = 0;
        for (Edge edge : old_edges) if (!tbr.contains(edge))//all edges except the one added
            integrateEdge(edge);
        //clear and restart.
        Manager.selected_node = -1;
        Manager.unhighlight_next = true;
    }

    public int getNode() {
        if (parent_node != this) node = parent_node.getNode();
        return node;//either
    }

    private Edge getParent() {
        if (parent_node != this) parent_node = parent_node.getParent();
        return parent_node;
    }

    public static Edge getEdge (int x1, int y1, int x2, int y2) {
        for (Edge edge : edges) if (edge.start_x == x1 && edge.start_y == y1 && edge.end_x == x2 && edge.end_y == y2 ||
                edge.start_x == x2 && edge.start_y == y2 && edge.end_x == x1 && edge.end_y == y1) return edge;//in either direction
        return null;
    }

    public static void fill (HashMap<String, Integer> tbf) {
        for (Edge edge : edges) {
            tbf.put(edge.start_x + "." + edge.start_y, edge.getNode());
            tbf.put(edge.end_x + "." + edge.end_y, edge.getNode());
        }
    }
}
