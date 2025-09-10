package main.impl;

import main.interfaces.ScreenCaptureProvider;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Implementação de ScreenCaptureProvider usando Robot do Java AWT
 */
public class RobotScreenCaptureProvider implements ScreenCaptureProvider {
    
    private static final Logger logger = Logger.getLogger(RobotScreenCaptureProvider.class.getName());
    private final Robot robot;
    
    public RobotScreenCaptureProvider() throws AWTException {
        this.robot = new Robot();
    }
    
    @Override
    public BufferedImage capture(Rectangle bounds) {
        try {
            return robot.createScreenCapture(bounds);
        } catch (Exception e) {
            logger.severe("Erro ao capturar tela: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public BufferedImage captureAround(int centerX, int centerY, int width, int height) {
        int x = centerX - width / 2;
        int y = centerY - height / 2;
        Rectangle bounds = new Rectangle(x, y, width, height);
        return capture(bounds);
    }
    
    @Override
    public Rectangle getActiveWindowBounds() {
        try {
            // Usar GraphicsEnvironment para obter a tela ativa
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            return gd.getDefaultConfiguration().getBounds();
        } catch (Exception e) {
            logger.warning("Erro ao obter limites da janela ativa: " + e.getMessage());
            return new Rectangle(0, 0, 1920, 1080); // Fallback
        }
    }
    
    @Override
    public String getActiveWindowTitle() {
        try {
            // Usar SystemTray para obter informações da janela ativa
            // Nota: Esta é uma implementação simplificada
            // Em um ambiente real, seria necessário usar JNI ou bibliotecas nativas
            return "Active Window"; // Placeholder
        } catch (Exception e) {
            logger.warning("Erro ao obter título da janela ativa: " + e.getMessage());
            return "Unknown Window";
        }
    }
    
    @Override
    public long getActiveWindowPid() {
        try {
            // Implementação simplificada - em produção seria necessário JNI
            return -1; // Indica que PID não está disponível
        } catch (Exception e) {
            logger.warning("Erro ao obter PID da janela ativa: " + e.getMessage());
            return -1;
        }
    }
    
    @Override
    public boolean isWindowActive(String windowTitle) {
        try {
            String currentTitle = getActiveWindowTitle();
            return currentTitle != null && currentTitle.contains(windowTitle);
        } catch (Exception e) {
            logger.warning("Erro ao verificar janela ativa: " + e.getMessage());
            return false;
        }
    }
}