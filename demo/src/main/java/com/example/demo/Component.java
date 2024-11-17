package com.example.demo;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Component {
    private static final int TRIM = 3;
    private static final Color
        BASE_COLOR = Color.color(0.1, 0.1, 0.1),
        INVALID_COLOR = Color.color(0.9, 0.2, 0.1),
        BLOCKED_COLOR = Color.color(0.8, 0.8, 0.22),
        HIGHLIGHT_COLOR = Color.color(0.9, 0.9, 0.9),
        HIGHLIGHT_INVALID_COLOR = Color.color(0.5, 0.1, 0.1),
        HIGHLIGHT_BLOCKED_COLOR = Color.color(0.6, 0.6, 0.12);

    //public final Image image, invalidimage, highlightedimage, blockedimage;
    //private int[][] redList, greenList, blueList;
    private int[][] grayList;
    private boolean[][] drawList;
    public int gridx, gridy, tlx, tly, initx, inity, IN, OUT;
    public boolean invalid_location = false, higher_highlighted = false, highlighted = false, inTransit = true, blocked_location;
    public double[] mins, maxes, stored_values, recorded_values;
    public String[] parameters;
    public String name, compname, uniquename;
    private static final int dark = 0, light = 180;
    public Component(String compname, String name, int initx, int inity, int IN, int OUT, String[] parameters, double[] mins, double[] maxes) {
        this.compname = compname;
        this.name = name;
        this.tlx = initx;
        this.tly = inity;
        this.IN = IN;
        this.OUT = OUT;
        this.parameters = parameters;
        this.mins = mins;
        this.maxes = maxes;
        this.stored_values = new double[mins.length];

        this.listsFromString(GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + ".png"));
    }

    public Component(Component template) {
        this.compname = template.compname;
        this.name = template.name;
        this.tlx = template.tlx;
        this.tly = template.tly;
        this.IN = template.IN;
        this.OUT = template.OUT;
        this.parameters = template.parameters;
        this.mins = template.mins;
        this.maxes = template.maxes;
        this.stored_values = new double[this.mins.length];

        //this.redList = template.redList;
        //this.greenList = template.greenList;
        //this.blueList = template.blueList;
        this.grayList = template.grayList;
        this.drawList = template.drawList;

    }

    public void place () {
        //System.out.println("placed!");
        this.inTransit = false;
        if (blocked_location) {
            blocked_location = false;
            this.tlx = this.initx;
            this.tly = this.inity;
            if (this.inity > GUI_FLOW.cy) {//blocked, but is brand new (has no initial location)
                Manager.components.remove(this);
                return;
            }
        }
        gridx = Manager.gridify(this.tlx + IMAGE_WIDTH / 2.0, Manager.offsetX, true);//snap!
        gridy = Manager.gridify(this.tly + IMAGE_WIDTH / 2.0, Manager.offsetY, true);
        if (invalid_location)
            Manager.components.remove(this);//remove self, no longer will exist
    }

    public String to_be_processed(HashMap<String, Integer> map, int id) {
        int[] nets = new int[]{//0 1 2 3 is top right bottom left
                map.getOrDefault(gridx + "." + (gridy - 1), -1),
                map.getOrDefault((gridx + 1) + "." + gridy, -1),
                map.getOrDefault(gridx + "." + (gridy + 1), -1),
                map.getOrDefault((gridx - 1) + "." + gridy, -1)
        };

        /*if (this.compname.equals("pit")) {
            this.uniquename = "pit";
            int net1 = -1, net2 = -1;
            if (this.IN != -1) net1 = nets[this.IN];
            if (this.OUT != -1) net2 = nets[this.OUT];
            if (net1 == -1) net1 = net2;
            //System.out.println(Arrays.toString(nets));
            if (net1 == -1) throw new Error();
            return String.format("SINK," + net1);
        }*/

        int in_id = -1, out_id = -1;
        if (this.IN != -1) {
            in_id = nets[this.IN];
            if (in_id == -1) throw new RuntimeException(this.name + " at (" + this.gridx + ", " + this.gridy + ") is not connected to a complete number of nets!");
        }
        if (this.OUT != -1) {
            out_id = nets[this.OUT];
            if (out_id == -1) throw new RuntimeException(this.name + " at (" + this.gridx + ", " + this.gridy + ") is not connected to a complete number of nets!");
        }
        String[] spl = this.compname.split("\\.");
        this.uniquename = spl[0] + id;

        StringBuilder str = new StringBuilder(spl[0] + "," + this.uniquename + "," + in_id + "," + out_id + ",{");
        for (int i = 0; i < this.stored_values.length; i++) str.append("'").append(this.parameters[i]).append("':'").append(this.stored_values[i]).append("',");
        if (spl.length > 1) str.append(String.format("'type':'%s'}", spl[1]));
        else str.append("}");
        return str.toString().replaceAll(",}", "}");
    }

    private void listsFromString(String path){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException ignored) {
            throw new RuntimeException("You screwed up! " + path);
        }
        int w = img.getWidth();
        if(w != img.getHeight()) throw new IllegalArgumentException("Image is not square!");
        //int[][] red = new int[w][w], green = new int[w][w], blue = new int[w][w];
        int[][] gray = new int[w][w];
        boolean[][] draw = new boolean[w][w];
        for(int j = 0; j < w; j++){
            for(int i = 0; i < w; i++){
                int value = img.getRGB(i, j);//four bytes, which is a signed int.
                //System.out.println(value);
                if(value == 0) {
                    draw[j][i] = false;
                    continue;
                }
                draw[j][i] = true;
                final int red = ((byte)(value >> 16) + 256) % 256;
                final int green = ((byte)(value >> 8) + 256) % 256;
                final int blue = ((byte)(value) + 256) % 256;
                if (red != green || green != blue) throw new RuntimeException("Not grayscale! " + path);
                gray[j][i] = red;
            }
        }
        //redList = red;
        //greenList = green;
        //blueList = blue;
        grayList = gray;
        drawList = draw;
    }

    public void drawWith(GraphicsContext gc, int size, boolean override) {
        final double tlX = inTransit ? tlx : (gridx - 1) / 2.0 * GUI_FLOW.IMAGE_WIDTH + Manager.offsetX,
                tlY = inTransit ? tly : (gridy - 1) / 2.0 * GUI_FLOW.IMAGE_WIDTH + Manager.offsetY;
        drawAt(gc, size, tlX, tlY, override);
    }
    /**dim is how wide the SQUARE image is. Ideally a multiple of the pixel width.*/
    public void drawAt(GraphicsContext gc, double dim, double tlX, double tlY, boolean override){
        //int[][] red = redList, green = greenList, blue = blueList;
        boolean[][] draw = drawList;//just copying memory addresses so everything is fine
        double pPP = (dim/draw.length);//pPP = pixelsPerPixel, images must be square but that's handled above.
        int w = draw.length;//pixels wide overall
        for(int j = 0; j < w; j++){
            for(int i = 0; i < w; i++){
                if(!draw[j][i]) continue;
                //gc.setFill(Color.rgb(red[j][i], green[j][i], blue[j][i]));
                Color temp = getPixelColor(grayList[j][i], override);
                if (temp.equals(Color.LIMEGREEN)) continue;
                gc.setFill(temp);
                gc.fillRect(tlX + i * pPP - 0.5, tlY + j * pPP - 0.5, pPP + 1, pPP + 1);
            }
        }
    }

    private Color getPixelColor (int grayVal, boolean override) {
        //180 for highlighted, 0 for dark.//System.out.println(grayVal);

        if (grayVal == 30) {
            if (override) return BASE_COLOR;
            if (invalid_location) return INVALID_COLOR;
            else if (blocked_location) return BLOCKED_COLOR;
            else return BASE_COLOR;
        } else if (grayVal == 180) {
            if (override) return Color.LIMEGREEN;//BASE_COLOR;
            if (invalid_location) return HIGHLIGHT_INVALID_COLOR;
            else if (blocked_location) return HIGHLIGHT_BLOCKED_COLOR;
            //if (higher_highlighted || highlighted) return HIGHLIGHT_COLOR;
            /*else */return Color.LIMEGREEN;//gets filtered out
        }
        return Color.LIMEGREEN;//gets filtered out
        //throw new RuntimeException("Unknown color! " + grayVal);
    }
}
