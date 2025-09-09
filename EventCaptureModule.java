import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Módulo responsável pela captura de eventos de mouse e teclado
 */
public class EventCaptureModule {
    
    private Robot robot;
    private VisualAnalysisModule visualAnalysis;
    private LoggerModule logger;
    private AtomicBoolean isCapturing = new AtomicBoolean(false);
    private List<RecordedAction> actions;
    private long lastEventTime = 0;
    private static final long MIN_EVENT_INTERVAL = 10; // Mínimo 10ms entre eventos
    
    public EventCaptureModule(Robot robot, VisualAnalysisModule visualAnalysis, LoggerModule logger) {
        this.robot = robot;
        this.visualAnalysis = visualAnalysis;
        this.logger = logger;
    }
    
    public void startCapture(List<RecordedAction> actions) {
        this.actions = actions;
        this.isCapturing.set(true);
        this.lastEventTime = 0;
        
        // Configurar captura global de eventos
        setupGlobalEventCapture();
        
        logger.log("Captura de eventos iniciada");
    }
    
    public void stopCapture() {
        this.isCapturing.set(false);
        logger.log("Captura de eventos finalizada");
    }
    
    private void setupGlobalEventCapture() {
        // Criar um frame invisível para capturar eventos
        Frame captureFrame = new Frame("Event Capture");
        captureFrame.setSize(1, 1);
        captureFrame.setLocation(-1000, -1000);
        captureFrame.setVisible(true);
        
        // Adicionar listeners de mouse
        captureFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isCapturing.get()) {
                    captureMouseClick(e, true);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isCapturing.get()) {
                    captureMouseClick(e, false);
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isCapturing.get()) {
                    captureMouseClick(e, true);
                }
            }
        });
        
        captureFrame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isCapturing.get()) {
                    captureMouseMove(e);
                }
            }
        });
        
        // Adicionar listener de teclado
        captureFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isCapturing.get()) {
                    captureKeyEvent(e, true);
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (isCapturing.get()) {
                    captureKeyEvent(e, false);
                }
            }
        });
        
        // Focar no frame para capturar eventos
        captureFrame.requestFocus();
        
        // Manter o frame ativo
        new Thread(() -> {
            while (isCapturing.get()) {
                try {
                    Thread.sleep(100);
                    if (!captureFrame.isFocused()) {
                        captureFrame.requestFocus();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            captureFrame.dispose();
        }).start();
    }
    
    private void captureMouseClick(MouseEvent e, boolean isPressed) {
        if (!shouldCaptureEvent()) return;
        
        try {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            MouseClickEvent clickEvent = new MouseClickEvent(
                mousePos.x, mousePos.y, e.getButton(), e.getClickCount()
            );
            
            // Capturar informações da janela ativa
            captureWindowInfo(clickEvent);
            
            // Capturar contexto visual
            visualAnalysis.captureVisualContext(clickEvent, mousePos.x, mousePos.y);
            
            // Adicionar delay se necessário
            addDelayIfNeeded();
            
            actions.add(clickEvent);
            lastEventTime = System.currentTimeMillis();
            
            logger.log("Mouse click capturado: " + clickEvent.getDescription());
            
        } catch (Exception ex) {
            logger.log("Erro ao capturar clique do mouse: " + ex.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    private void captureMouseMove(MouseEvent e) {
        if (!shouldCaptureEvent()) return;
        
        try {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            MouseMoveEvent moveEvent = new MouseMoveEvent(mousePos.x, mousePos.y);
            
            // Capturar informações da janela ativa
            captureWindowInfo(moveEvent);
            
            // Capturar contexto visual
            visualAnalysis.captureVisualContext(moveEvent, mousePos.x, mousePos.y);
            
            // Adicionar delay se necessário
            addDelayIfNeeded();
            
            actions.add(moveEvent);
            lastEventTime = System.currentTimeMillis();
            
        } catch (Exception ex) {
            logger.log("Erro ao capturar movimento do mouse: " + ex.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    private void captureKeyEvent(KeyEvent e, boolean isPressed) {
        if (!shouldCaptureEvent()) return;
        
        try {
            KeyboardEvent keyEvent = new KeyboardEvent(e.getKeyCode(), e.getKeyChar(), isPressed);
            keyEvent.setKeyLocation(e.getKeyLocation());
            
            // Capturar informações da janela ativa
            captureWindowInfo(keyEvent);
            
            // Adicionar delay se necessário
            addDelayIfNeeded();
            
            actions.add(keyEvent);
            lastEventTime = System.currentTimeMillis();
            
            logger.log("Tecla capturada: " + keyEvent.getDescription());
            
        } catch (Exception ex) {
            logger.log("Erro ao capturar evento de teclado: " + ex.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    private void captureWindowInfo(RecordedAction event) {
        try {
            Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (activeWindow != null) {
                String title = "";
                if (activeWindow instanceof Frame) {
                    title = ((Frame) activeWindow).getTitle();
                } else if (activeWindow instanceof Dialog) {
                    title = ((Dialog) activeWindow).getTitle();
                }
                event.setWindowTitle(title);
                event.setWindowClass(activeWindow.getClass().getSimpleName());
                event.setWindowId(activeWindow.hashCode());
            }
        } catch (Exception e) {
            logger.log("Erro ao capturar informações da janela: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
        }
    }
    
    private boolean shouldCaptureEvent() {
        long currentTime = System.currentTimeMillis();
        return isCapturing.get() && (currentTime - lastEventTime) >= MIN_EVENT_INTERVAL;
    }
    
    private void addDelayIfNeeded() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastEvent = currentTime - lastEventTime;
        
        if (timeSinceLastEvent > MIN_EVENT_INTERVAL && lastEventTime > 0) {
            DelayEvent delayEvent = new DelayEvent(timeSinceLastEvent);
            actions.add(delayEvent);
        }
    }
}