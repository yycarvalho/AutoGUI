import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/**
 * Módulo responsável pela análise visual e captura de contexto
 */
public class VisualAnalysisModule {
    
    private Robot robot;
    private LoggerModule logger;
    private static final int CAPTURE_RADIUS = 10; // Raio para captura de contexto visual
    private static final int SCREENSHOT_SIZE = 50; // Tamanho da screenshot para validação
    
    public VisualAnalysisModule(Robot robot, LoggerModule logger) {
        this.robot = robot;
        this.logger = logger;
    }
    
    /**
     * Captura o contexto visual para um evento
     */
    public void captureVisualContext(RecordedAction event, int x, int y) {
        try {
            // Capturar cores ao redor do ponto
            int[] colors = captureSurroundingColors(x, y);
            
            // Capturar screenshot da região
            BufferedImage screenshot = captureRegionScreenshot(x, y);
            
            // Aplicar contexto baseado no tipo de evento
            if (event instanceof MouseClickEvent) {
                MouseClickEvent clickEvent = (MouseClickEvent) event;
                clickEvent.setSurroundingColors(colors);
                clickEvent.setScreenshot(screenshot);
                clickEvent.setScreenshotBase64(imageToBase64(screenshot));
            } else if (event instanceof MouseMoveEvent) {
                MouseMoveEvent moveEvent = (MouseMoveEvent) event;
                moveEvent.setSurroundingColors(colors);
                moveEvent.setScreenshot(screenshot);
                moveEvent.setScreenshotBase64(imageToBase64(screenshot));
            }
            
            logger.log("Contexto visual capturado para posição (" + x + ", " + y + ")");
            
        } catch (Exception e) {
            logger.log("Erro ao capturar contexto visual: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    /**
     * Captura as cores dos pixels ao redor de um ponto
     */
    private int[] captureSurroundingColors(int centerX, int centerY) {
        int[] colors = new int[(CAPTURE_RADIUS * 2 + 1) * (CAPTURE_RADIUS * 2 + 1)];
        int index = 0;
        
        try {
            for (int dy = -CAPTURE_RADIUS; dy <= CAPTURE_RADIUS; dy++) {
                for (int dx = -CAPTURE_RADIUS; dx <= CAPTURE_RADIUS; dx++) {
                    int x = centerX + dx;
                    int y = centerY + dy;
                    
                    // Verificar se a posição está dentro da tela
                    if (x >= 0 && y >= 0 && x < GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDisplayMode().getWidth() &&
                        y < GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDisplayMode().getHeight()) {
                        
                        Color pixelColor = robot.getPixelColor(x, y);
                        colors[index] = pixelColor.getRGB();
                    } else {
                        colors[index] = 0; // Cor preta para pixels fora da tela
                    }
                    index++;
                }
            }
        } catch (Exception e) {
            logger.log("Erro ao capturar cores: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
        }
        
        return colors;
    }
    
    /**
     * Captura uma screenshot da região ao redor de um ponto
     */
    private BufferedImage captureRegionScreenshot(int centerX, int centerY) {
        try {
            int x = Math.max(0, centerX - SCREENSHOT_SIZE / 2);
            int y = Math.max(0, centerY - SCREENSHOT_SIZE / 2);
            int width = SCREENSHOT_SIZE;
            int height = SCREENSHOT_SIZE;
            
            // Ajustar para não sair da tela
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            int screenWidth = ge.getDefaultScreenDevice().getDisplayMode().getWidth();
            int screenHeight = ge.getDefaultScreenDevice().getDisplayMode().getHeight();
            
            if (x + width > screenWidth) width = screenWidth - x;
            if (y + height > screenHeight) height = screenHeight - y;
            
            Rectangle captureArea = new Rectangle(x, y, width, height);
            return robot.createScreenCapture(captureArea);
            
        } catch (Exception e) {
            logger.log("Erro ao capturar screenshot: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    /**
     * Valida se o contexto visual ainda é compatível
     */
    public boolean validateVisualContext(RecordedAction event, int x, int y, double tolerance) {
        try {
            if (event instanceof MouseClickEvent) {
                return validateMouseClickContext((MouseClickEvent) event, x, y, tolerance);
            } else if (event instanceof MouseMoveEvent) {
                return validateMouseMoveContext((MouseMoveEvent) event, x, y, tolerance);
            }
            return true; // Para outros tipos de evento, assumir válido
        } catch (Exception e) {
            logger.log("Erro na validação visual: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
            return false;
        }
    }
    
    private boolean validateMouseClickContext(MouseClickEvent event, int x, int y, double tolerance) {
        // Validar cores ao redor
        int[] currentColors = captureSurroundingColors(x, y);
        int[] originalColors = event.getSurroundingColors();
        
        if (originalColors == null || currentColors == null) {
            return true; // Se não há contexto original, assumir válido
        }
        
        double colorMatch = calculateColorSimilarity(originalColors, currentColors);
        logger.log("Similaridade de cores: " + (colorMatch * 100) + "%");
        
        return colorMatch >= tolerance;
    }
    
    private boolean validateMouseMoveContext(MouseMoveEvent event, int x, int y, double tolerance) {
        // Para movimento do mouse, validação mais relaxada
        int[] currentColors = captureSurroundingColors(x, y);
        int[] originalColors = event.getSurroundingColors();
        
        if (originalColors == null || currentColors == null) {
            return true;
        }
        
        double colorMatch = calculateColorSimilarity(originalColors, currentColors);
        return colorMatch >= (tolerance * 0.8); // 20% mais tolerante para movimento
    }
    
    /**
     * Calcula a similaridade entre dois arrays de cores
     */
    private double calculateColorSimilarity(int[] colors1, int[] colors2) {
        if (colors1.length != colors2.length) {
            return 0.0;
        }
        
        int matches = 0;
        int total = colors1.length;
        
        for (int i = 0; i < colors1.length; i++) {
            if (colors1[i] == colors2[i]) {
                matches++;
            } else {
                // Verificar similaridade com tolerância
                Color c1 = new Color(colors1[i]);
                Color c2 = new Color(colors2[i]);
                
                if (isColorSimilar(c1, c2, 0.1)) { // 10% de tolerância para cores similares
                    matches++;
                }
            }
        }
        
        return (double) matches / total;
    }
    
    /**
     * Verifica se duas cores são similares dentro de uma tolerância
     */
    private boolean isColorSimilar(Color c1, Color c2, double tolerance) {
        double rDiff = Math.abs(c1.getRed() - c2.getRed()) / 255.0;
        double gDiff = Math.abs(c1.getGreen() - c2.getGreen()) / 255.0;
        double bDiff = Math.abs(c1.getBlue() - c2.getBlue()) / 255.0;
        
        double avgDiff = (rDiff + gDiff + bDiff) / 3.0;
        return avgDiff <= tolerance;
    }
    
    /**
     * Converte uma imagem para Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            logger.log("Erro ao converter imagem para Base64: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
            return "";
        }
    }
    
    /**
     * Converte Base64 para imagem
     */
    public BufferedImage base64ToImage(String base64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            return ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            logger.log("Erro ao converter Base64 para imagem: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    /**
     * Aguarda até que o contexto visual seja válido ou timeout
     */
    public boolean waitForValidContext(RecordedAction event, int x, int y, long timeoutMs, double tolerance) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (validateVisualContext(event, x, y, tolerance)) {
                return true;
            }
            
            try {
                Thread.sleep(100); // Verificar a cada 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        logger.log("Timeout na validação do contexto visual", LoggerModule.LogLevel.WARNING);
        return false;
    }
}