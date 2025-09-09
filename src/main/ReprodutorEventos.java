package main;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

class ReprodutorEventos {
    private Robot robot;
    private boolean reproduzindo;
    private ReprodutorListener listener;
    
    public interface ReprodutorListener {
        void onAcaoExecutada(Acao acao, int progresso, int total);
        void onReproducaoCompleta();
        void onErro(String erro);
    }
    
    public ReprodutorEventos() throws Exception {
        this.robot = new Robot();
        this.reproduzindo = false;
    }
    
    public void setReprodutorListener(ReprodutorListener listener) {
        this.listener = listener;
    }
    
    public CompletableFuture<Void> reproduzirAcoes(List<Acao> acoes) {
        return CompletableFuture.runAsync(() -> {
            reproduzindo = true;
            
            try {
                for (int i = 0; i < acoes.size() && reproduzindo; i++) {
                    Acao acao = acoes.get(i);
                    
                    // Aguardar delay antes da execuÃ§Ã£o
                    if (acao.getDelay() > 0) {
                        Thread.sleep(acao.getDelay());
                    }
                    
                    executarAcao(acao);
                    
                    if (listener != null) {
                        listener.onAcaoExecutada(acao, i + 1, acoes.size());
                    }
                }
                
                if (listener != null) {
                    listener.onReproducaoCompleta();
                }
                
            } catch (Exception e) {
                if (listener != null) {
                    listener.onErro("Erro durante reproduÃ§Ã£o: " + e.getMessage());
                }
            } finally {
                reproduzindo = false;
            }
        });
    }
    
    public void pararReproducao() {
        reproduzindo = false;
    }
    
    private void executarAcao(Acao acao) throws InterruptedException {
        switch (acao.getTipo()) {
            case MOUSE_CLICK -> executarClickMouse(acao);
            case MOUSE_MOVE -> executarMovimentoMouse(acao);
            case SCROLL -> executarScroll(acao);
            case KEY_PRESS -> executarTeclaPressionada(acao);
            case KEY_RELEASE -> executarTeclaLiberada(acao);
            case KEY_TYPE -> executarTeclaDigitada(acao);
        }
    }
    
    private void executarClickMouse(Acao acao) {
        robot.mouseMove(acao.getX(), acao.getY());
        robot.delay(50);
        
        String[] partes = acao.getDetalhes().split("_");
        String botao = partes[0];
        int clicks = Integer.parseInt(partes[1]);
        
        int botaoMask = switch (botao) {
            case "ESQUERDO" -> InputEvent.BUTTON1_DOWN_MASK;
            case "DIREITO" -> InputEvent.BUTTON3_DOWN_MASK;
            case "MEIO" -> InputEvent.BUTTON2_DOWN_MASK;
            default -> InputEvent.BUTTON1_DOWN_MASK;
        };
        
        for (int i = 0; i < clicks; i++) {
            robot.mousePress(botaoMask);
            robot.delay(50);
            robot.mouseRelease(botaoMask);
            if (i < clicks - 1) robot.delay(100);
        }
    }
    
    private void executarMovimentoMouse(Acao acao) {
        robot.mouseMove(acao.getX(), acao.getY());
    }
    
    private void executarScroll(Acao acao) {
        robot.mouseMove(acao.getX(), acao.getY());
        robot.delay(50);
        
        String[] partes = acao.getDetalhes().split("_");
        String direcao = partes[0];
        int passos = Integer.parseInt(partes[1]);
        
        int scrollDirection = direcao.equals("CIMA") ? -1 : 1;
        for (int i = 0; i < passos; i++) {
            robot.mouseWheel(scrollDirection);
            robot.delay(50);
        }
    }
    
    private void executarTeclaPressionada(Acao acao) {
        pressionarTecla(acao.getDetalhes(), true);
    }
    
    private void executarTeclaLiberada(Acao acao) {
        pressionarTecla(acao.getDetalhes(), false);
    }
    
    private void executarTeclaDigitada(Acao acao) {
        char c = acao.getDetalhes().charAt(0);
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (keyCode != KeyEvent.VK_UNDEFINED) {
            robot.keyPress(keyCode);
            robot.delay(50);
            robot.keyRelease(keyCode);
        }
    }
    
    private void pressionarTecla(String detalhes, boolean pressionar) {
        String[] partes = detalhes.split("\\+");
        
        // Processar modificadores
        boolean ctrl = false, alt = false, shift = false, meta = false;
        String teclaFinal = detalhes;
        
        for (String parte : partes) {
            switch (parte) {
                case "CTRL" -> ctrl = true;
                case "ALT" -> alt = true;
                case "SHIFT" -> shift = true;
                case "META" -> meta = true;
                default -> teclaFinal = parte;
            }
        }
        
        // Aplicar modificadores
        if (ctrl) {
            if (pressionar) robot.keyPress(KeyEvent.VK_CONTROL);
            else robot.keyRelease(KeyEvent.VK_CONTROL);
        }
        if (alt) {
            if (pressionar) robot.keyPress(KeyEvent.VK_ALT);
            else robot.keyRelease(KeyEvent.VK_ALT);
        }
        if (shift) {
            if (pressionar) robot.keyPress(KeyEvent.VK_SHIFT);
            else robot.keyRelease(KeyEvent.VK_SHIFT);
        }
        if (meta) {
            if (pressionar) robot.keyPress(KeyEvent.VK_META);
            else robot.keyRelease(KeyEvent.VK_META);
        }
        
        // Aplicar tecla principal
        int keyCode = getKeyCode(teclaFinal);
        if (keyCode != -1) {
            if (pressionar) {
                robot.keyPress(keyCode);
            } else {
                robot.keyRelease(keyCode);
            }
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
            switch (c) {
                case ' ' -> { return KeyEvent.VK_SPACE; }
                case '-' -> { return KeyEvent.VK_MINUS; }
                case '=' -> { return KeyEvent.VK_EQUALS; }
                case '[' -> { return KeyEvent.VK_OPEN_BRACKET; }
                case ']' -> { return KeyEvent.VK_CLOSE_BRACKET; }
                case '\\' -> { return KeyEvent.VK_BACK_SLASH; }
                case ';' -> { return KeyEvent.VK_SEMICOLON; }
                case '\'' -> { return KeyEvent.VK_QUOTE; }
                case ',' -> { return KeyEvent.VK_COMMA; }
                case '.' -> { return KeyEvent.VK_PERIOD; }
                case '/' -> { return KeyEvent.VK_SLASH; }
                case '`' -> { return KeyEvent.VK_BACK_QUOTE; }
                case '*' -> { return KeyEvent.VK_MULTIPLY; }
                case '+' -> { return KeyEvent.VK_PLUS; }
                
                // Caracteres acentuados e especiais do português
                case 'ç' -> { return KeyEvent.VK_C; } // Será tratado com combinação
                case 'Ç' -> { return KeyEvent.VK_C; }
                case 'á', 'à', 'ã', 'â' -> { return KeyEvent.VK_A; }
                case 'Á', 'À', 'Ã', 'Â' -> { return KeyEvent.VK_A; }
                case 'é', 'ê' -> { return KeyEvent.VK_E; }
                case 'É', 'Ê' -> { return KeyEvent.VK_E; }
                case 'í' -> { return KeyEvent.VK_I; }
                case 'Í' -> { return KeyEvent.VK_I; }
                case 'ó', 'ô', 'õ' -> { return KeyEvent.VK_O; }
                case 'Ó', 'Ô', 'Õ' -> { return KeyEvent.VK_O; }
                case 'ú' -> { return KeyEvent.VK_U; }
                case 'Ú' -> { return KeyEvent.VK_U; }
                case '~' -> { return KeyEvent.VK_DEAD_TILDE; }
                case '?' -> { return KeyEvent.VK_SLASH; } // Com shift
                default -> { return KeyEvent.getExtendedKeyCodeForChar(c); }
            }
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
}