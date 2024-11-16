package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Component {
    public final Image image, invalidimage, highlightedimage, blockedimage;
    public int gridx, gridy, tlx, tly, initx, inity, IN, OUT;
    public boolean invalid_location = false, higher_highlighted = false, highlighted = false, inTransit = true, blocked_location;
    public String name;
    public Component(String name, int initx, int inity, int IN, int OUT) {
        this.name = name;
        this.image = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + ".png"));
        this.invalidimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "INVALID.png"));
        this.blockedimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "BLOCKED.png"));
        this.highlightedimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "HIGHLIGHTED.png"));
        this.tlx = initx;
        this.tly = inity;
        this.IN = IN;
        this.OUT = OUT;
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
}
