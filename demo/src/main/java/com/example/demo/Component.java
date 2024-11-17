package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Component {
    public final Image image, invalidimage, highlightedimage, blockedimage;
    public int gridx, gridy, tlx, tly, initx, inity, IN, OUT;
    public boolean invalid_location = false, higher_highlighted = false, highlighted = false, inTransit = true, blocked_location;
    public double[] mins, maxes, stored_values;
    public String[] parameters;
    public String name;
    public Component(String name, int initx, int inity, int IN, int OUT, String[] parameters, double[] mins, double[] maxes) {
        this.name = name;
        this.image = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + ".png"));
        this.invalidimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "INVALID.png"));
        this.blockedimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "BLOCKED.png"));
        this.highlightedimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "HIGHLIGHTED.png"));
        this.tlx = initx;
        this.tly = inity;
        this.IN = IN;
        this.OUT = OUT;
        this.parameters = parameters;
        this.mins = mins;
        this.maxes = maxes;
        this.stored_values = new double[mins.length];
        //System.out.println(name);
        //System.out.printf("\t%s\n\t%s\n\t%s\n\t%s\n", image, invalidimage, blockedimage, highlightedimage);

    }

    public Component(Component template) {
        this.name = template.name;
        this.image = template.image;
        this.invalidimage = template.invalidimage;
        this.blockedimage = template.blockedimage;
        this.highlightedimage = template.highlightedimage;
        this.tlx = template.tlx;
        this.tly = template.tly;
        this.IN = template.IN;
        this.OUT = template.OUT;
        this.parameters = template.parameters;
        this.mins = template.mins;
        this.maxes = template.maxes;
        this.stored_values = new double[this.mins.length];
    }

    public void drawWith(GraphicsContext gc) {
        final double x = inTransit ? tlx : (gridx - 1) / 2.0 * GUI_FLOW.IMAGE_WIDTH + Manager.offsetX,
                y = inTransit ? tly : (gridy - 1) / 2.0 * GUI_FLOW.IMAGE_WIDTH + Manager.offsetY;
        Image tbu = image;
        if (invalid_location) tbu = invalidimage;
        else if (blocked_location) tbu = blockedimage;
        else if (higher_highlighted || highlighted || inTransit) tbu = highlightedimage;
        gc.drawImage(tbu, x, y, 80, 80);
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
        int in_id = nets[this.IN];
        int out_id = nets[this.OUT];
        if (in_id == -1 || out_id == -1) throw new RuntimeException(this.name + " at (" + this.gridx + ", " + this.gridy + ") is not connected to a complete number of nets!");

        StringBuilder str = new StringBuilder(this.name + "," + this.name + id + "," + in_id + "," + out_id + ",{");
        for (int i = 0; i < this.stored_values.length; i++) str.append("'").append(this.parameters[i]).append("':'").append(this.stored_values[i]).append("',");
        str.append("}");
        return str.toString().replaceAll(",}", "}");
    }
}
