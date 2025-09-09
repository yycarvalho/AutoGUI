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
                    
                    // Aguardar delay antes da execução
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
                    listener.onErro("Erro durante reprodução: " + e.getMessage());
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
    
    private int getKeyCode(String tecla) {
        return switch (tecla.toUpperCase()) {
            case "A" -> KeyEvent.VK_A;
            case "B" -> KeyEvent.VK_B;
            case "C" -> KeyEvent.VK_C;
            case "D" -> KeyEvent.VK_D;
            case "E" -> KeyEvent.VK_E;
            case "F" -> KeyEvent.VK_F;
            case "G" -> KeyEvent.VK_G;
            case "H" -> KeyEvent.VK_H;
            case "I" -> KeyEvent.VK_I;
            case "J" -> KeyEvent.VK_J;
            case "K" -> KeyEvent.VK_K;
            case "L" -> KeyEvent.VK_L;
            case "M" -> KeyEvent.VK_M;
            case "N" -> KeyEvent.VK_N;
            case "O" -> KeyEvent.VK_O;
            case "P" -> KeyEvent.VK_P;
            case "Q" -> KeyEvent.VK_Q;
            case "R" -> KeyEvent.VK_R;
            case "S" -> KeyEvent.VK_S;
            case "T" -> KeyEvent.VK_T;
            case "U" -> KeyEvent.VK_U;
            case "V" -> KeyEvent.VK_V;
            case "W" -> KeyEvent.VK_W;
            case "X" -> KeyEvent.VK_X;
            case "Y" -> KeyEvent.VK_Y;
            case "Z" -> KeyEvent.VK_Z;
            case "0" -> KeyEvent.VK_0;
            case "1" -> KeyEvent.VK_1;
            case "2" -> KeyEvent.VK_2;
            case "3" -> KeyEvent.VK_3;
            case "4" -> KeyEvent.VK_4;
            case "5" -> KeyEvent.VK_5;
            case "6" -> KeyEvent.VK_6;
            case "7" -> KeyEvent.VK_7;
            case "8" -> KeyEvent.VK_8;
            case "9" -> KeyEvent.VK_9;
            case "SPACE" -> KeyEvent.VK_SPACE;
            case "ENTER" -> KeyEvent.VK_ENTER;
            case "TAB" -> KeyEvent.VK_TAB;
            case "ESCAPE" -> KeyEvent.VK_ESCAPE;
            case "BACKSPACE" -> KeyEvent.VK_BACK_SPACE;
            case "DELETE" -> KeyEvent.VK_DELETE;
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
            default -> -1;
        };
    }
}
