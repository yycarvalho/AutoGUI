package main.impl;

import main.interfaces.InputSimulator;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * Implementação de InputSimulator usando Robot do Java AWT
 */
public class RobotInputSimulator implements InputSimulator {
    
    private static final Logger logger = Logger.getLogger(RobotInputSimulator.class.getName());
    private final Robot robot;
    
    public RobotInputSimulator() throws AWTException {
        this.robot = new Robot();
    }
    
    @Override
    public void moveMouse(Point point) {
        try {
            robot.mouseMove(point.x, point.y);
        } catch (Exception e) {
            logger.severe("Erro ao mover mouse: " + e.getMessage());
        }
    }
    
    @Override
    public void click(Point point, int button, int clickCount) {
        try {
            robot.mouseMove(point.x, point.y);
            robot.delay(50); // Pequena pausa antes do clique
            
            int buttonMask = switch (button) {
                case 1 -> InputEvent.BUTTON1_DOWN_MASK;
                case 2 -> InputEvent.BUTTON2_DOWN_MASK;
                case 3 -> InputEvent.BUTTON3_DOWN_MASK;
                default -> InputEvent.BUTTON1_DOWN_MASK;
            };
            
            for (int i = 0; i < clickCount; i++) {
                robot.mousePress(buttonMask);
                robot.delay(50);
                robot.mouseRelease(buttonMask);
                if (i < clickCount - 1) {
                    robot.delay(100);
                }
            }
        } catch (Exception e) {
            logger.severe("Erro ao clicar: " + e.getMessage());
        }
    }
    
    @Override
    public void keyPress(int keyCode) {
        try {
            robot.keyPress(keyCode);
        } catch (Exception e) {
            logger.severe("Erro ao pressionar tecla: " + e.getMessage());
        }
    }
    
    @Override
    public void keyRelease(int keyCode) {
        try {
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            logger.severe("Erro ao liberar tecla: " + e.getMessage());
        }
    }
    
    @Override
    public void mouseWheel(Point point, int direction, int clicks) {
        try {
            robot.mouseMove(point.x, point.y);
            robot.delay(50);
            
            for (int i = 0; i < clicks; i++) {
                robot.mouseWheel(direction);
                robot.delay(50);
            }
        } catch (Exception e) {
            logger.severe("Erro ao executar scroll: " + e.getMessage());
        }
    }
    
    @Override
    public void delay(int milliseconds) {
        try {
            robot.delay(milliseconds);
        } catch (Exception e) {
            logger.severe("Erro ao aplicar delay: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isAvailable() {
        return robot != null;
    }
}