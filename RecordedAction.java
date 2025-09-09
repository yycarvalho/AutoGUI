import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Classe base para representar uma ação capturada
 */
public abstract class RecordedAction {
    protected LocalDateTime timestamp;
    protected String windowTitle;
    protected String windowClass;
    protected int windowId;
    protected Map<String, Object> metadata;
    
    public RecordedAction() {
        this.timestamp = LocalDateTime.now();
        this.metadata = new java.util.HashMap<>();
    }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getWindowTitle() { return windowTitle; }
    public void setWindowTitle(String windowTitle) { this.windowTitle = windowTitle; }
    
    public String getWindowClass() { return windowClass; }
    public void setWindowClass(String windowClass) { this.windowClass = windowClass; }
    
    public int getWindowId() { return windowId; }
    public void setWindowId(int windowId) { this.windowId = windowId; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public abstract String getActionType();
    public abstract String getDescription();
}

/**
 * Representa um evento de clique do mouse
 */
class MouseClickEvent extends RecordedAction {
    private int x, y;
    private int button; // 1=esquerda, 2=meio, 3=direita
    private int clickCount;
    private int[] surroundingColors; // Cores dos pixels ao redor
    private BufferedImage screenshot;
    private String screenshotBase64;
    
    public MouseClickEvent(int x, int y, int button, int clickCount) {
        super();
        this.x = x;
        this.y = y;
        this.button = button;
        this.clickCount = clickCount;
    }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getButton() { return button; }
    public void setButton(int button) { this.button = button; }
    
    public int getClickCount() { return clickCount; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
    
    public int[] getSurroundingColors() { return surroundingColors; }
    public void setSurroundingColors(int[] surroundingColors) { this.surroundingColors = surroundingColors; }
    
    public BufferedImage getScreenshot() { return screenshot; }
    public void setScreenshot(BufferedImage screenshot) { this.screenshot = screenshot; }
    
    public String getScreenshotBase64() { return screenshotBase64; }
    public void setScreenshotBase64(String screenshotBase64) { this.screenshotBase64 = screenshotBase64; }
    
    @Override
    public String getActionType() { return "MOUSE_CLICK"; }
    
    @Override
    public String getDescription() {
        return String.format("Clique do mouse em (%d, %d) - Botão: %d, Contagem: %d", 
                           x, y, button, clickCount);
    }
}

/**
 * Representa um evento de movimento do mouse
 */
class MouseMoveEvent extends RecordedAction {
    private int x, y;
    private int[] surroundingColors;
    private BufferedImage screenshot;
    private String screenshotBase64;
    
    public MouseMoveEvent(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int[] getSurroundingColors() { return surroundingColors; }
    public void setSurroundingColors(int[] surroundingColors) { this.surroundingColors = surroundingColors; }
    
    public BufferedImage getScreenshot() { return screenshot; }
    public void setScreenshot(BufferedImage screenshot) { this.screenshot = screenshot; }
    
    public String getScreenshotBase64() { return screenshotBase64; }
    public void setScreenshotBase64(String screenshotBase64) { this.screenshotBase64 = screenshotBase64; }
    
    @Override
    public String getActionType() { return "MOUSE_MOVE"; }
    
    @Override
    public String getDescription() {
        return String.format("Movimento do mouse para (%d, %d)", x, y);
    }
}

/**
 * Representa um evento de teclado
 */
class KeyboardEvent extends RecordedAction {
    private int keyCode;
    private char keyChar;
    private boolean isKeyPressed; // true = pressionada, false = solta
    private String keyText;
    private long keyLocation;
    
    public KeyboardEvent(int keyCode, char keyChar, boolean isKeyPressed) {
        super();
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.isKeyPressed = isKeyPressed;
        this.keyText = java.awt.event.KeyEvent.getKeyText(keyCode);
        this.keyLocation = 0; // Será definido pelo sistema
    }
    
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    
    public char getKeyChar() { return keyChar; }
    public void setKeyChar(char keyChar) { this.keyChar = keyChar; }
    
    public boolean isKeyPressed() { return isKeyPressed; }
    public void setKeyPressed(boolean isKeyPressed) { this.isKeyPressed = isKeyPressed; }
    
    public String getKeyText() { return keyText; }
    public void setKeyText(String keyText) { this.keyText = keyText; }
    
    public long getKeyLocation() { return keyLocation; }
    public void setKeyLocation(long keyLocation) { this.keyLocation = keyLocation; }
    
    @Override
    public String getActionType() { return "KEYBOARD"; }
    
    @Override
    public String getDescription() {
        return String.format("Tecla %s %s (código: %d, char: '%c')", 
                           keyText, isKeyPressed ? "pressionada" : "solta", keyCode, keyChar);
    }
}

/**
 * Representa um evento de delay/pausa
 */
class DelayEvent extends RecordedAction {
    private long delayMillis;
    
    public DelayEvent(long delayMillis) {
        super();
        this.delayMillis = delayMillis;
    }
    
    public long getDelayMillis() { return delayMillis; }
    public void setDelayMillis(long delayMillis) { this.delayMillis = delayMillis; }
    
    @Override
    public String getActionType() { return "DELAY"; }
    
    @Override
    public String getDescription() {
        return String.format("Pausa de %d ms", delayMillis);
    }
}