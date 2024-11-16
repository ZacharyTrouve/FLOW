package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import static com.example.demo.GUI_FLOW.IMAGE_WIDTH;

public class Component {
    private final Image image, invalidimage, highlightedimage;
    public int gridx, gridy, tlx, tly;
    public boolean invalid_location = false, highlighted = false, inTransit = true;
    public String name;
    public Component(String name, int initx, int inity) {
        this.name = name;
        image = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + ".png"));
        invalidimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "INVALID.png"));
        highlightedimage = new Image("file:" + GUI_FLOW.basedir + GUI_FLOW.join("data", "components", name + "HIGHLIGHTED.png"));
        this.tlx = initx;
        this.tly = inity;
    }

    public void drawWith(GraphicsContext gc) {
        final int x = inTransit ? tlx : gridx * GUI_FLOW.IMAGE_WIDTH + Manager.offsetX,
                y = inTransit ? tly : gridy * GUI_FLOW.IMAGE_WIDTH + Manager.offsetY;
        Image tbu = image;
        if (highlighted) tbu = highlightedimage;
        else if (invalid_location) tbu = invalidimage;
        gc.drawImage(tbu, x, y);
    }

    public void place () {
        this.inTransit = false;
        gridx = (this.tlx + Manager.offsetX) / IMAGE_WIDTH;//snap!
        gridy = (this.tly + Manager.offsetY) / IMAGE_WIDTH;
        if (invalid_location)
            Manager.components.remove(this);//remove self, no longer will exist
    }
}
