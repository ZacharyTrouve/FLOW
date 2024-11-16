package com.example.demo;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

public class IsKeyPressed {
    private static volatile boolean shiftPressed = false;
    public static boolean isShiftPressed() {
        synchronized (IsKeyPressed.class) {
            return shiftPressed;
        }
    }

    public static void main(String[] args) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            synchronized (IsKeyPressed.class) {
                switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED -> {
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
                            shiftPressed = true;
                        }
                    }
                    case KeyEvent.KEY_RELEASED -> {
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
                            shiftPressed = false;
                        }
                    }
                }
                return false;
            }
        });
    }
}