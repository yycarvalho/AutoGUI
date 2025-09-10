package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public class Acao {
    private int id;
    private TipoAcao tipo;
    private String detalhes;
    private int x, y; // coordenadas absolutas
    private int relativeX, relativeY; // coordenadas relativas à janela
    private LocalDateTime timestamp;
    private long delay; // tempo em milissegundos até a próxima ação
    
    // Metadados da janela
    private String windowTitle;
    private long windowPid;
    private Rectangle windowBounds;
    
    // Dados visuais para validação
    private BufferedImage pixelSample; // amostra de pixels ao redor do ponto
    private BufferedImage screenshot; // screenshot recortado
    private String pixelSampleBase64; // amostra em Base64 para XML
    private String screenshotBase64; // screenshot em Base64 para XML
    private String screenshotPath; // caminho do arquivo de screenshot
    
    // Configurações de validação
    private double validationTolerancePct = 95.0;
    private long maxWaitMs = 60000;
    private String retryPolicy = "abort";
    
    // Ruído controlado
    private double uncertaintyPct = 1.5;
    
    public enum TipoAcao {
        MOUSE_CLICK, MOUSE_MOVE, SCROLL, KEY_PRESS, KEY_RELEASE, KEY_TYPE, PAUSE
    }
    
    public Acao(int id, TipoAcao tipo, String detalhes, int x, int y) {
        this.id = id;
        this.tipo = tipo;
        this.detalhes = detalhes;
        this.x = x;
        this.y = y;
        this.relativeX = x;
        this.relativeY = y;
        this.timestamp = LocalDateTime.now();
        this.delay = 0;
        this.windowTitle = "";
        this.windowPid = -1;
        this.windowBounds = new Rectangle(0, 0, 0, 0);
    }
    
    public Acao(int id, TipoAcao tipo, String detalhes, int x, int y, int relativeX, int relativeY) {
        this.id = id;
        this.tipo = tipo;
        this.detalhes = detalhes;
        this.x = x;
        this.y = y;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.timestamp = LocalDateTime.now();
        this.delay = 0;
        this.windowTitle = "";
        this.windowPid = -1;
        this.windowBounds = new Rectangle(0, 0, 0, 0);
    }
    
    // Getters e Setters básicos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public TipoAcao getTipo() { return tipo; }
    public void setTipo(TipoAcao tipo) { this.tipo = tipo; }
    
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getRelativeX() { return relativeX; }
    public void setRelativeX(int relativeX) { this.relativeX = relativeX; }
    
    public int getRelativeY() { return relativeY; }
    public void setRelativeY(int relativeY) { this.relativeY = relativeY; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public long getDelay() { return delay; }
    public void setDelay(long delay) { this.delay = delay; }
    
    // Getters e Setters para metadados da janela
    public String getWindowTitle() { return windowTitle; }
    public void setWindowTitle(String windowTitle) { this.windowTitle = windowTitle; }
    
    public long getWindowPid() { return windowPid; }
    public void setWindowPid(long windowPid) { this.windowPid = windowPid; }
    
    public Rectangle getWindowBounds() { return windowBounds; }
    public void setWindowBounds(Rectangle windowBounds) { this.windowBounds = windowBounds; }
    
    // Getters e Setters para dados visuais
    public BufferedImage getPixelSample() { return pixelSample; }
    public void setPixelSample(BufferedImage pixelSample) { this.pixelSample = pixelSample; }
    
    public BufferedImage getScreenshot() { return screenshot; }
    public void setScreenshot(BufferedImage screenshot) { this.screenshot = screenshot; }
    
    public String getPixelSampleBase64() { return pixelSampleBase64; }
    public void setPixelSampleBase64(String pixelSampleBase64) { this.pixelSampleBase64 = pixelSampleBase64; }
    
    public String getScreenshotBase64() { return screenshotBase64; }
    public void setScreenshotBase64(String screenshotBase64) { this.screenshotBase64 = screenshotBase64; }
    
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    
    // Getters e Setters para configurações de validação
    public double getValidationTolerancePct() { return validationTolerancePct; }
    public void setValidationTolerancePct(double validationTolerancePct) { this.validationTolerancePct = validationTolerancePct; }
    
    public long getMaxWaitMs() { return maxWaitMs; }
    public void setMaxWaitMs(long maxWaitMs) { this.maxWaitMs = maxWaitMs; }
    
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    
    public double getUncertaintyPct() { return uncertaintyPct; }
    public void setUncertaintyPct(double uncertaintyPct) { this.uncertaintyPct = uncertaintyPct; }
    
    public String getTimestampFormatted() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
    
    @Override
    public String toString() {
        return String.format("Acao[%d]: %s - %s (%d,%d) [rel: %d,%d] - %s - Win: %s", 
            id, tipo, detalhes, x, y, relativeX, relativeY, getTimestampFormatted(), windowTitle);
    }
    
    /**
     * Aplica ruído controlado às coordenadas baseado no uncertaintyPct
     * @return Point com coordenadas ajustadas
     */
    public Point getAdjustedCoordinates() {
        if (uncertaintyPct <= 0) {
            return new Point(x, y);
        }
        
        // Calcular deslocamento aleatório baseado na porcentagem de incerteza
        double maxOffset = Math.max(1, Math.min(uncertaintyPct / 100.0 * 10, 5)); // Máximo 5 pixels
        double offsetX = (Math.random() - 0.5) * 2 * maxOffset;
        double offsetY = (Math.random() - 0.5) * 2 * maxOffset;
        
        return new Point((int)(x + offsetX), (int)(y + offsetY));
    }
    
    /**
     * Verifica se a ação tem dados visuais para validação
     * @return true se tem pixel sample ou screenshot
     */
    public boolean hasVisualData() {
        return pixelSample != null || screenshot != null || 
               (pixelSampleBase64 != null && !pixelSampleBase64.isEmpty()) ||
               (screenshotBase64 != null && !screenshotBase64.isEmpty());
    }
}