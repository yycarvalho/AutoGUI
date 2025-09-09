import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;

/**
 * Módulo responsável pela reprodução das ações gravadas
 */
public class PlaybackModule {
    
    private Robot robot;
    private VisualAnalysisModule visualAnalysis;
    private LoggerModule logger;
    private Random random;
    private static final double DEFAULT_TOLERANCE = 0.95;
    private static final long MAX_WAIT_TIME = 60000; // 60 segundos
    private static final double UNCERTAINTY_FACTOR = 0.02; // 2% de incerteza
    
    public PlaybackModule(Robot robot, VisualAnalysisModule visualAnalysis, LoggerModule logger) {
        this.robot = robot;
        this.visualAnalysis = visualAnalysis;
        this.logger = logger;
        this.random = new Random();
    }
    
    /**
     * Reproduz uma lista de ações
     */
    public void playActions(List<RecordedAction> actions) throws Exception {
        logger.log("Iniciando reprodução de " + actions.size() + " ações");
        
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < actions.size(); i++) {
            RecordedAction action = actions.get(i);
            
            try {
                logger.log("Executando ação " + (i + 1) + "/" + actions.size() + ": " + action.getDescription());
                
                if (executeAction(action)) {
                    successCount++;
                    logger.log("Ação executada com sucesso");
                } else {
                    errorCount++;
                    logger.log("Falha na execução da ação", LoggerModule.LogLevel.WARNING);
                }
                
                // Pequena pausa entre ações para estabilidade
                Thread.sleep(50);
                
            } catch (Exception e) {
                errorCount++;
                logger.log("Erro ao executar ação: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
                
                // Decidir se deve continuar ou parar
                if (shouldAbortOnError()) {
                    logger.log("Abortando reprodução devido a erro crítico", LoggerModule.LogLevel.ERROR);
                    break;
                }
            }
        }
        
        logger.log("Reprodução finalizada. Sucessos: " + successCount + ", Erros: " + errorCount);
    }
    
    /**
     * Executa uma ação específica
     */
    private boolean executeAction(RecordedAction action) throws Exception {
        if (action instanceof MouseClickEvent) {
            return executeMouseClick((MouseClickEvent) action);
        } else if (action instanceof MouseMoveEvent) {
            return executeMouseMove((MouseMoveEvent) action);
        } else if (action instanceof KeyboardEvent) {
            return executeKeyboard((KeyboardEvent) action);
        } else if (action instanceof DelayEvent) {
            return executeDelay((DelayEvent) action);
        }
        
        return false;
    }
    
    /**
     * Executa um clique do mouse
     */
    private boolean executeMouseClick(MouseClickEvent event) throws Exception {
        int x = event.getX();
        int y = event.getY();
        
        // Aplicar incerteza se configurado
        if (UNCERTAINTY_FACTOR > 0) {
            x = applyUncertainty(x);
            y = applyUncertainty(y);
        }
        
        // Validar contexto visual antes de clicar
        if (!validateBeforeAction(event, x, y)) {
            logger.log("Contexto visual inválido para clique em (" + x + ", " + y + ")", LoggerModule.LogLevel.WARNING);
            return false;
        }
        
        // Mover mouse para a posição
        robot.mouseMove(x, y);
        Thread.sleep(100); // Aguardar movimento do mouse
        
        // Executar clique
        int button = event.getButton();
        int clickCount = event.getClickCount();
        
        for (int i = 0; i < clickCount; i++) {
            if (button == 1) { // Botão esquerdo
                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(50);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            } else if (button == 2) { // Botão do meio
                robot.mousePress(java.awt.event.InputEvent.BUTTON2_DOWN_MASK);
                Thread.sleep(50);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON2_DOWN_MASK);
            } else if (button == 3) { // Botão direito
                robot.mousePress(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);
                Thread.sleep(50);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON3_DOWN_MASK);
            }
            
            if (i < clickCount - 1) {
                Thread.sleep(100); // Pausa entre múltiplos cliques
            }
        }
        
        return true;
    }
    
    /**
     * Executa movimento do mouse
     */
    private boolean executeMouseMove(MouseMoveEvent event) throws Exception {
        int x = event.getX();
        int y = event.getY();
        
        // Aplicar incerteza se configurado
        if (UNCERTAINTY_FACTOR > 0) {
            x = applyUncertainty(x);
            y = applyUncertainty(y);
        }
        
        // Validar contexto visual (mais relaxado para movimento)
        if (!validateBeforeAction(event, x, y)) {
            logger.log("Contexto visual inválido para movimento para (" + x + ", " + y + ")", LoggerModule.LogLevel.WARNING);
            // Para movimento, continuar mesmo com contexto inválido
        }
        
        // Mover mouse suavemente
        Point currentPos = MouseInfo.getPointerInfo().getLocation();
        smoothMouseMove(currentPos.x, currentPos.y, x, y);
        
        return true;
    }
    
    /**
     * Executa evento de teclado
     */
    private boolean executeKeyboard(KeyboardEvent event) throws Exception {
        int keyCode = event.getKeyCode();
        boolean isKeyPressed = event.isKeyPressed();
        
        if (isKeyPressed) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
        
        return true;
    }
    
    /**
     * Executa delay/pausa
     */
    private boolean executeDelay(DelayEvent event) throws Exception {
        long delay = event.getDelayMillis();
        
        // Aplicar incerteza no delay
        if (UNCERTAINTY_FACTOR > 0) {
            double variation = 1.0 + (random.nextGaussian() * UNCERTAINTY_FACTOR);
            delay = (long) (delay * variation);
            delay = Math.max(10, delay); // Mínimo 10ms
        }
        
        Thread.sleep(delay);
        return true;
    }
    
    /**
     * Valida o contexto visual antes de executar uma ação
     */
    private boolean validateBeforeAction(RecordedAction event, int x, int y) {
        // Aguardar contexto válido com timeout
        return visualAnalysis.waitForValidContext(event, x, y, MAX_WAIT_TIME, DEFAULT_TOLERANCE);
    }
    
    /**
     * Aplica incerteza a uma coordenada
     */
    private int applyUncertainty(int value) {
        double variation = 1.0 + (random.nextGaussian() * UNCERTAINTY_FACTOR);
        return (int) (value * variation);
    }
    
    /**
     * Move o mouse suavemente de uma posição para outra
     */
    private void smoothMouseMove(int fromX, int fromY, int toX, int toY) throws InterruptedException {
        int steps = Math.max(10, Math.abs(toX - fromX) + Math.abs(toY - fromY) / 10);
        
        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            int currentX = (int) (fromX + (toX - fromX) * ratio);
            int currentY = (int) (fromY + (toY - fromY) * ratio);
            
            robot.mouseMove(currentX, currentY);
            Thread.sleep(10);
        }
    }
    
    /**
     * Decide se deve abortar a reprodução em caso de erro
     */
    private boolean shouldAbortOnError() {
        // Implementar lógica de decisão baseada em configurações
        // Por enquanto, sempre continuar
        return false;
    }
    
    /**
     * Valida se uma janela específica está ativa
     */
    private boolean validateActiveWindow(RecordedAction event) {
        try {
            Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (activeWindow == null) {
                return false;
            }
            
            // Verificar se a janela ativa corresponde à janela esperada
            if (event.getWindowTitle() != null && !event.getWindowTitle().isEmpty()) {
                String currentTitle = "";
                if (activeWindow instanceof Frame) {
                    currentTitle = ((Frame) activeWindow).getTitle();
                } else if (activeWindow instanceof Dialog) {
                    currentTitle = ((Dialog) activeWindow).getTitle();
                }
                return event.getWindowTitle().equals(currentTitle);
            }
            
            return true; // Se não há informação de janela, assumir válido
            
        } catch (Exception e) {
            logger.log("Erro ao validar janela ativa: " + e.getMessage(), LoggerModule.LogLevel.WARNING);
            return true; // Em caso de erro, assumir válido
        }
    }
    
    /**
     * Aguarda até que uma condição seja atendida ou timeout
     */
    private boolean waitForCondition(java.util.function.BooleanSupplier condition, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition.getAsBoolean()) {
                return true;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }
}