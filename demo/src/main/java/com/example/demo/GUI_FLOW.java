package com.example.demo;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
public class GUI_FLOW extends Application {
    public static final int IMAGE_WIDTH = 80;
    public static final int cx = 900, cy = 700, tx = 1100, ty = 900;
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().trim().startsWith("window");
    public static String pythonCommand = isWindows ? "python":"python3", join = isWindows ? "\\" : "/",
        basedir = System.getProperty("user.dir") + join("..");

    public void start(Stage stage){
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        Canvas canvas = new Canvas(tx, ty);
        root.setCenter(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Manager.init(gc);


        canvas.setOnMouseReleased(Manager::onMouseReleased);
        canvas.setOnMousePressed(Manager::onMousePressed);
        canvas.setOnMouseDragged(Manager::onMouseDragged);
        canvas.setOnMouseClicked(Manager::onMouseClicked);
        canvas.setOnMouseMoved(Manager::onMouseMoved);

        stage.setResizable(false);
        stage.show();

        //runWillTerminate(pythonCommand + " txt_class_converter.py\n");
        Manager.components.add(new Component("origin", 30, 30));
        Manager.components.add(new Component("pit", 200, 30));
        Manager.components.add(new Component("pipeNS", 300, 80));
        Manager.components.add(new Component("pipeEW", 400, 180));
        Manager.components.add(new Component("gatevalve", 500, 400));
        for (Component comp : Manager.components) comp.place();
    }
    public void warning(String input){
        Stage temp = new Stage();
        temp.setResizable(false);
        BorderPane temper = new BorderPane();
        Scene TEMP = new Scene(temper);
        temp.setScene(TEMP);
        String[] total = input.trim().split("\n");
        StringBuilder tbc = new StringBuilder();
        for (String s : total) tbc.append("   ").append(s).append("   \n");
        Label tempiest = new Label(tbc.toString());
        tempiest.setFocusTraversable(false);
        tempiest.setStyle("-fx-text-fill: red");
        temper.setCenter(tempiest);
        temp.show();
    }


    public static void runWillTerminate(String... command){
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new File(join(basedir, "python")));
        try {
            Process process0 = processBuilder.start();
            process0.waitFor();
            System.out.println("Exited process");
        } catch (IOException e){
            System.out.println("Failure to run " + command[0]);
        } catch (InterruptedException ignored){
            throw new RuntimeException("Vincent, this must be your fault again!");
        }
    }

    public static String join(String... args) {
        StringBuilder base = new StringBuilder();
        for (String str : args) base.append(join).append(str);
        return base.toString();
    }
}
