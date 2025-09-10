package main;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import main.interfaces.InputSimulator;
import main.interfaces.ScreenCaptureProvider;
import main.impl.RobotInputSimulator;
import main.impl.RobotScreenCaptureProvider;

class ReprodutorEventos {
    private InputSimulator inputSimulator;
    private ScreenCaptureProvider screenCaptureProvider;
    private VerificadorElementos verificador;
    private boolean reproduzindo;
    private ReprodutorListener listener;
    private static final Logger logger = Logger.getLogger(ReprodutorEventos.class.getName());
    
    public interface ReprodutorListener {
        void onAcaoExecutada(Acao acao, int progresso, int total);
        void onReproducaoCompleta();
        void onErro(String erro);
        void onValidacaoFalhou(Acao acao, String motivo);
    }
    
    public ReprodutorEventos() throws Exception {
        this.inputSimulator = new RobotInputSimulator();
        this.screenCaptureProvider = new RobotScreenCaptureProvider();
        this.verificador = new VerificadorElementos();
        this.reproduzindo = false;
    }
    
    public ReprodutorEventos(InputSimulator inputSimulator, ScreenCaptureProvider screenCaptureProvider) {
        this.inputSimulator = inputSimulator;
        this.screenCaptureProvider = screenCaptureProvider;
        this.verificador = new VerificadorElementos();
        this.reproduzindo = false;
    }
    
    public void setReprodutorListener(ReprodutorListener listener) {
        this.listener = listener;
    }
    
    public CompletableFuture<ReplayResult> reproduzirAcoes(List<Acao> acoes) {
        return CompletableFuture.supplyAsync(() -> {
            reproduzindo = true;
            ReplayResult result = new ReplayResult();
            
            try {
                for (int i = 0; i < acoes.size() && reproduzindo; i++) {
                    Acao acao = acoes.get(i);
                    
                    // Aguardar delay antes da execução
                    if (acao.getDelay() > 0) {
                        Thread.sleep(acao.getDelay());
                    }
                    
                    // Validar elemento antes da execução (se tiver dados visuais)
                    if (acao.hasVisualData()) {
                        if (!validarElemento(acao)) {
                            String motivo = "Elemento não encontrado na tela";
                            logger.warning("Validação falhou para ação " + acao.getId() + ": " + motivo);
                            
                            if (listener != null) {
                                listener.onValidacaoFalhou(acao, motivo);
                            }
                            
                            // Aplicar política de retry
                            if (!aplicarPoliticaRetry(acao, result)) {
                                break;
                            }
                        }
                    }
                    
                    // Executar ação com coordenadas ajustadas (ruído controlado)
                    executarAcaoComRuido(acao);
                    
                    if (listener != null) {
                        listener.onAcaoExecutada(acao, i + 1, acoes.size());
                    }
                    
                    result.incrementarAcoesExecutadas();
                }
                
                if (listener != null) {
                    listener.onReproducaoCompleta();
                }
                
                result.setSucesso(true);
                
            } catch (Exception e) {
                logger.severe("Erro durante reprodução: " + e.getMessage());
                if (listener != null) {
                    listener.onErro("Erro durante reprodução: " + e.getMessage());
                }
                result.setSucesso(false);
                result.setErro(e.getMessage());
            } finally {
                reproduzindo = false;
            }
            
            return result;
        });
    }
    
    public void pararReproducao() {
        reproduzindo = false;
    }
    
    /**
     * Valida se o elemento está presente na tela
     */
    private boolean validarElemento(Acao acao) {
        try {
            if (acao.getPixelSample() != null) {
                // Capturar região atual da tela
                Point coords = acao.getAdjustedCoordinates();
                var currentSample = screenCaptureProvider.captureAround(
                    coords.x, coords.y, 21, 21);
                
                if (currentSample != null) {
                    return verificador.matches(
                        acao.getPixelSample(), 
                        currentSample, 
                        acao.getValidationTolerancePct()
                    );
                }
            }
            return true; // Se não tem dados visuais, assume que está OK
        } catch (Exception e) {
            logger.warning("Erro na validação: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Aplica política de retry baseada na configuração da ação
     */
    private boolean aplicarPoliticaRetry(Acao acao, ReplayResult result) {
        String policy = acao.getRetryPolicy();
        
        switch (policy.toLowerCase()) {
            case "abort":
                logger.severe("Política ABORT: interrompendo reprodução");
                result.setSucesso(false);
                result.setErro("Validação falhou e política é abort");
                return false;
                
            case "skip":
                logger.warning("Política SKIP: pulando ação " + acao.getId());
                result.incrementarAcoesPuladas();
                return true;
                
            case "continue":
                logger.warning("Política CONTINUE: continuando apesar da falha");
                result.incrementarAcoesComFalha();
                return true;
                
            default:
                logger.warning("Política desconhecida: " + policy + ", usando ABORT");
                return false;
        }
    }
    
    /**
     * Executa ação com ruído controlado aplicado às coordenadas
     */
    private void executarAcaoComRuido(Acao acao) throws InterruptedException {
        Point coords = acao.getAdjustedCoordinates();
        
        switch (acao.getTipo()) {
            case MOUSE_CLICK -> executarClickMouse(acao, coords);
            case MOUSE_MOVE -> executarMovimentoMouse(acao, coords);
            case SCROLL -> executarScroll(acao, coords);
            case KEY_PRESS -> executarTeclaPressionada(acao);
            case KEY_RELEASE -> executarTeclaLiberada(acao);
            case KEY_TYPE -> executarTeclaDigitada(acao);
            case PAUSE -> Thread.sleep(acao.getDelay());
        }
    }
    
    private void executarClickMouse(Acao acao, Point coords) {
        inputSimulator.moveMouse(coords);
        inputSimulator.delay(100);
        
        String[] partes = acao.getDetalhes().split("_");
        String botao = partes[0];
        int clicks = Integer.parseInt(partes[1]);
        
        int buttonCode = switch (botao) {
            case "ESQUERDO" -> 1;
            case "DIREITO" -> 3;
            case "MEIO" -> 2;
            default -> 1;
        };
        
        inputSimulator.click(coords, buttonCode, clicks);
    }
    
    private void executarMovimentoMouse(Acao acao, Point coords) {
        inputSimulator.moveMouse(coords);
    }
    
    private void executarScroll(Acao acao, Point coords) {
        inputSimulator.moveMouse(coords);
        inputSimulator.delay(50);
        
        String[] partes = acao.getDetalhes().split("_");
        String direcao = partes[0];
        int passos = Integer.parseInt(partes[1]);
        
        int scrollDirection = direcao.equals("CIMA") ? -1 : 1;
        inputSimulator.mouseWheel(coords, scrollDirection, passos);
    }
    
    private void executarTeclaPressionada(Acao acao) {
        int keyCode = getKeyCode(acao.getDetalhes());
        if (keyCode != -1) {
            inputSimulator.keyPress(keyCode);
        }
    }
    
    private void executarTeclaLiberada(Acao acao) {
        int keyCode = getKeyCode(acao.getDetalhes());
        if (keyCode != -1) {
            inputSimulator.keyRelease(keyCode);
        }
    }
    
    private void executarTeclaDigitada(Acao acao) {
        char c = acao.getDetalhes().charAt(0);
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (keyCode != KeyEvent.VK_UNDEFINED) {
            inputSimulator.keyPress(keyCode);
            inputSimulator.delay(50);
            inputSimulator.keyRelease(keyCode);
        }
    }
    
    /**
     * Converte string da tecla para keyCode do Java AWT
     */
    private int getKeyCode(String tecla) {
        // Para caracteres únicos, usar o próprio caractere
        if (tecla.length() == 1) {
            char c = tecla.charAt(0);
            
            // Letras (converter para maiúscula para o keyCode)
            if (c >= 'a' && c <= 'z') {
                return KeyEvent.VK_A + (c - 'a');
            }
            if (c >= 'A' && c <= 'Z') {
                return KeyEvent.VK_A + (c - 'A');
            }
            
            // Números
            if (c >= '0' && c <= '9') {
                return KeyEvent.VK_0 + (c - '0');
            }
            
            // Símbolos e caracteres especiais
            return switch (c) {
                case ' ' -> KeyEvent.VK_SPACE;
                case '-' -> KeyEvent.VK_MINUS;
                case '=' -> KeyEvent.VK_EQUALS;
                case '[' -> KeyEvent.VK_OPEN_BRACKET;
                case ']' -> KeyEvent.VK_CLOSE_BRACKET;
                case '\\' -> KeyEvent.VK_BACK_SLASH;
                case ';' -> KeyEvent.VK_SEMICOLON;
                case '\'' -> KeyEvent.VK_QUOTE;
                case ',' -> KeyEvent.VK_COMMA;
                case '.' -> KeyEvent.VK_PERIOD;
                case '/' -> KeyEvent.VK_SLASH;
                case '`' -> KeyEvent.VK_BACK_QUOTE;
                case '*' -> KeyEvent.VK_MULTIPLY;
                case '+' -> KeyEvent.VK_PLUS;
                default -> KeyEvent.getExtendedKeyCodeForChar(c);
            };
        }
        
        // Para nomes de teclas especiais
        return switch (tecla.toUpperCase()) {
            case "ENTER" -> KeyEvent.VK_ENTER;
            case "TAB" -> KeyEvent.VK_TAB;
            case "ESCAPE" -> KeyEvent.VK_ESCAPE;
            case "BACKSPACE" -> KeyEvent.VK_BACK_SPACE;
            case "DELETE" -> KeyEvent.VK_DELETE;
            case "INSERT" -> KeyEvent.VK_INSERT;
            case "HOME" -> KeyEvent.VK_HOME;
            case "END" -> KeyEvent.VK_END;
            case "PAGE_UP" -> KeyEvent.VK_PAGE_UP;
            case "PAGE_DOWN" -> KeyEvent.VK_PAGE_DOWN;
            case "UP" -> KeyEvent.VK_UP;
            case "DOWN" -> KeyEvent.VK_DOWN;
            case "LEFT" -> KeyEvent.VK_LEFT;
            case "RIGHT" -> KeyEvent.VK_RIGHT;
            case "F1" -> KeyEvent.VK_F1;
            case "F2" -> KeyEvent.VK_F2;
            case "F3" -> KeyEvent.VK_F3;
            case "F4" -> KeyEvent.VK_F4;
            case "F5" -> KeyEvent.VK_F5;
            case "F6" -> KeyEvent.VK_F6;
            case "F7" -> KeyEvent.VK_F7;
            case "F8" -> KeyEvent.VK_F8;
            case "F9" -> KeyEvent.VK_F9;
            case "F10" -> KeyEvent.VK_F10;
            case "F11" -> KeyEvent.VK_F11;
            case "F12" -> KeyEvent.VK_F12;
            case "CAPS_LOCK" -> KeyEvent.VK_CAPS_LOCK;
            case "NUM_LOCK" -> KeyEvent.VK_NUM_LOCK;
            case "SCROLL_LOCK" -> KeyEvent.VK_SCROLL_LOCK;
            case "PRINT_SCREEN" -> KeyEvent.VK_PRINTSCREEN;
            case "PAUSE" -> KeyEvent.VK_PAUSE;
            default -> -1;
        };
    }
    
    /**
     * Classe para resultado da reprodução
     */
    public static class ReplayResult {
        private boolean sucesso = false;
        private int acoesExecutadas = 0;
        private int acoesPuladas = 0;
        private int acoesComFalha = 0;
        private String erro = null;
        
        public void incrementarAcoesExecutadas() { acoesExecutadas++; }
        public void incrementarAcoesPuladas() { acoesPuladas++; }
        public void incrementarAcoesComFalha() { acoesComFalha++; }
        
        // Getters e Setters
        public boolean isSucesso() { return sucesso; }
        public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }
        public int getAcoesExecutadas() { return acoesExecutadas; }
        public int getAcoesPuladas() { return acoesPuladas; }
        public int getAcoesComFalha() { return acoesComFalha; }
        public String getErro() { return erro; }
        public void setErro(String erro) { this.erro = erro; }
        
        @Override
        public String toString() {
            return String.format("ReplayResult{sucesso=%s, executadas=%d, puladas=%d, falhas=%d, erro='%s'}", 
                sucesso, acoesExecutadas, acoesPuladas, acoesComFalha, erro);
        }
    }
}