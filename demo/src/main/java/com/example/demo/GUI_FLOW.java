package com.example.demo;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
public class GUI_FLOW extends Application {
    private String author;
    private String sourcepath;
    private Scene scene;
    private Canvas canvas;

    public int offsetX, offsetY, originX, originY, startX, startY;
    private boolean dragging = false;
    private static final int cx = 700, cy = 500;
    private GraphicsContext gc;
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().trim().startsWith("window");
    public static String pythonCommand = isWindows ? "python":"python3", join = isWindows ? "\\" : "/";

    public void start(Stage stage){

        BorderPane root = new BorderPane();
        scene = new Scene(root);
        stage.setScene(scene);
        canvas = new Canvas(cx, cy);
        gc = canvas.getGraphicsContext2D();
        offsetX = cx/3;
        offsetY = cy/2;
        startX = cx/3;
        startY = cy/2;
        originX = cx/3;
        originY = cy/2;

        canvas.setOnMouseReleased(e -> {
            startX = offsetX;
            startY = offsetY;
        });
        canvas.setOnMousePressed(e -> {
            originX = (int)e.getX();
            originY = (int)e.getY();
        });
        canvas.setOnMouseDragged(e -> {

        });
        canvas.setOnMouseClicked(e -> {

        });
        canvas.setOnMouseMoved(e -> {

        });

        stage.setResizable(false);
        stage.show();

        runWillTerminate(pythonCommand + " txt_class_converter.py\n");
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
        System.out.println(System.getProperty("user.dir") + String.format("%s..%spython%s", join, join, join));

        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new File(System.getProperty("user.dir") + String.format("%s..%spython/", join, join)));
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
}
