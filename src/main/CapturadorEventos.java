package main;


import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;




class CapturadorEventos implements NativeKeyListener, NativeMouseListener, 
                                 NativeMouseMotionListener, NativeMouseWheelListener {
    
    private List<Acao> acoes;
    private boolean gravando;
    private int contadorId;
    private boolean teclasPressionadas[];
    private long ultimoMovimentoTimestampMs;
    private static final long INTERVALO_MIN_MOVIMENTO_MS = 100;
    
    // Controle de clicks para detectar duplo-clique e suportar botÃ£o direito
    private long ultimoClickEsqMs;
    private long ultimoClickDirMs;
    private long ultimoClickMeioMs;
    private int ultimoClickX;
    private int ultimoClickY;
    private static final long INTERVALO_DUPOLO_CLique_MS = 400;
    private static final int DISTANCIA_MAX_DUPOLO_CLique_PX = 3;
    private EventoListener listener;
    
    public interface EventoListener {
        void onNovoEvento(Acao acao);
    }
    
    public CapturadorEventos() {
        this.acoes = new CopyOnWriteArrayList<>();
        this.gravando = false;
        this.contadorId = 1;
        this.teclasPressionadas = new boolean[256];
        this.ultimoMovimentoTimestampMs = 0L;
        this.ultimoClickEsqMs = 0L;
        this.ultimoClickDirMs = 0L;
        this.ultimoClickMeioMs = 0L;
        this.ultimoClickX = -1;
        this.ultimoClickY = -1;
        
    }
    
    public void setEventoListener(EventoListener listener) {
        this.listener = listener;
    }
    
    public void iniciarCaptura() throws com.github.kwhat.jnativehook.NativeHookException {
        if (!GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.registerNativeHook();
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeMouseWheelListener(this);
        
        this.gravando = true;
        this.acoes.clear();
        this.contadorId = 1;
    }
    
    public void pararCaptura() {
        this.gravando = false;
        calcularDelays();
    }
    
    public void limparCaptura() throws NativeHookException {
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);
        GlobalScreen.removeNativeMouseWheelListener(this);
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook();
        }
    }
    
    private void calcularDelays() {
        for (int i = 0; i < acoes.size() - 1; i++) {
            Acao atual = acoes.get(i);
            Acao proxima = acoes.get(i + 1);
            
            long delay = java.time.Duration.between(atual.getTimestamp(), proxima.getTimestamp()).toMillis();
            atual.setDelay(delay);
        }
        
        if (!acoes.isEmpty()) {
            acoes.get(acoes.size() - 1).setDelay(0);
        }
    }
    
    private void adicionarAcao(Acao acao) {
        if (gravando) {
            acoes.add(acao);
            if (listener != null) {
                listener.onNovoEvento(acao);
            }
        }
    }
    
    public List<Acao> getAcoes() {
        return new ArrayList<>(acoes);
    }
    
    // ============ MOUSE LISTENERS ============
    
    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        // Ignorado: iremos construir clicks com base em pressed/released para maior confiabilidade
    }
    
    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        // Nada a fazer no press; aguardamos o release para consolidar o(s) clique(s)
    }
    
    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        String botao = switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> "ESQUERDO";
            case NativeMouseEvent.BUTTON3 -> "MEIO";
            case NativeMouseEvent.BUTTON2 -> "DIREITO";
            default -> "DESCONHECIDO";
        };
        long agora = System.currentTimeMillis();
        long ultimoClickMs;
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> ultimoClickMs = ultimoClickEsqMs;
            case NativeMouseEvent.BUTTON3 -> ultimoClickMs = ultimoClickMeioMs;
            case NativeMouseEvent.BUTTON2 -> ultimoClickMs = ultimoClickDirMs;
            default -> ultimoClickMs = 0L;
        }
        int clicks = 1;
        if (ultimoClickMs > 0
                && (agora - ultimoClickMs) <= INTERVALO_DUPOLO_CLique_MS
                && distancia(e.getX(), e.getY(), ultimoClickX, ultimoClickY) <= DISTANCIA_MAX_DUPOLO_CLique_PX) {
            clicks = 2;
        }
        // Atualizar estado do Ãºltimo click para o botÃ£o correspondente
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> ultimoClickEsqMs = agora;
            case NativeMouseEvent.BUTTON3 -> ultimoClickMeioMs = agora;
            case NativeMouseEvent.BUTTON2 -> ultimoClickDirMs = agora;
            default -> { /* noop */ }
        }
        ultimoClickX = e.getX();
        ultimoClickY = e.getY();
        String detalhes = String.format("%s_%d", botao, clicks);
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.MOUSE_CLICK, detalhes, e.getX(), e.getY());
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        long agora = System.currentTimeMillis();
        if ((agora - ultimoMovimentoTimestampMs) >= INTERVALO_MIN_MOVIMENTO_MS) {
            ultimoMovimentoTimestampMs = agora;
            Acao acao = new Acao(contadorId++, Acao.TipoAcao.MOUSE_MOVE, "MOVE", e.getX(), e.getY());
            adicionarAcao(acao);
        }
    }
    
    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.MOUSE_MOVE, "DRAG", e.getX(), e.getY());
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        String direcao = e.getWheelRotation() < 0 ? "CIMA" : "BAIXO";
        String detalhes = String.format("%s_%d", direcao, Math.abs(e.getWheelRotation()));
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.SCROLL, detalhes, e.getX(), e.getY());
        adicionarAcao(acao);
    }
    
    // ============ KEYBOARD LISTENERS ============
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < teclasPressionadas.length) {
            if (teclasPressionadas[keyCode]) {
                return; // jÃ¡ registrada como pressionada, evita repetiÃ§Ã£o (auto-repeat)
            }
            teclasPressionadas[keyCode] = true;
        }
        
        String tecla = obterTeclaReal(keyCode);
        String modificadores = getModificadores(e);
        String detalhes = modificadores.isEmpty() ? tecla : modificadores + "+" + tecla;
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.KEY_PRESS, detalhes, -1, -1);
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < teclasPressionadas.length) {
            teclasPressionadas[keyCode] = false;
        }
        String tecla = obterTeclaReal(keyCode);
        String modificadores = getModificadores(e);
        String detalhes = modificadores.isEmpty() ? tecla : modificadores + "+" + tecla;
        
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.KEY_RELEASE, detalhes, -1, -1);
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Desabilitado para evitar duplicidade com KEY_PRESS/KEY_RELEASE
    }
    
    private String getModificadores(NativeKeyEvent e) {
        List<String> mods = new ArrayList<>();
        if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) mods.add("CTRL");
        if ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) mods.add("ALT");
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) mods.add("SHIFT");
        if ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0) mods.add("META");
        return String.join("+", mods);
    }

    private int distancia(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Converte keyCode do NativeKeyEvent para representação real da tecla
     */
    private String obterTeclaReal(int keyCode) {
        // Mapeamento direto dos keyCodes para caracteres reais
        return switch (keyCode) {
            // Letras
            case NativeKeyEvent.VC_A -> "a";
            case NativeKeyEvent.VC_B -> "b";
            case NativeKeyEvent.VC_C -> "c";
            case NativeKeyEvent.VC_D -> "d";
            case NativeKeyEvent.VC_E -> "e";
            case NativeKeyEvent.VC_F -> "f";
            case NativeKeyEvent.VC_G -> "g";
            case NativeKeyEvent.VC_H -> "h";
            case NativeKeyEvent.VC_I -> "i";
            case NativeKeyEvent.VC_J -> "j";
            case NativeKeyEvent.VC_K -> "k";
            case NativeKeyEvent.VC_L -> "l";
            case NativeKeyEvent.VC_M -> "m";
            case NativeKeyEvent.VC_N -> "n";
            case NativeKeyEvent.VC_O -> "o";
            case NativeKeyEvent.VC_P -> "p";
            case NativeKeyEvent.VC_Q -> "q";
            case NativeKeyEvent.VC_R -> "r";
            case NativeKeyEvent.VC_S -> "s";
            case NativeKeyEvent.VC_T -> "t";
            case NativeKeyEvent.VC_U -> "u";
            case NativeKeyEvent.VC_V -> "v";
            case NativeKeyEvent.VC_W -> "w";
            case NativeKeyEvent.VC_X -> "x";
            case NativeKeyEvent.VC_Y -> "y";
            case NativeKeyEvent.VC_Z -> "z";
            
            // Números
            case NativeKeyEvent.VC_0 -> "0";
            case NativeKeyEvent.VC_1 -> "1";
            case NativeKeyEvent.VC_2 -> "2";
            case NativeKeyEvent.VC_3 -> "3";
            case NativeKeyEvent.VC_4 -> "4";
            case NativeKeyEvent.VC_5 -> "5";
            case NativeKeyEvent.VC_6 -> "6";
            case NativeKeyEvent.VC_7 -> "7";
            case NativeKeyEvent.VC_8 -> "8";
            case NativeKeyEvent.VC_9 -> "9";
            
            // Símbolos e caracteres especiais
            case NativeKeyEvent.VC_SPACE -> " ";
            case NativeKeyEvent.VC_MINUS -> "-";
            case NativeKeyEvent.VC_EQUALS -> "=";
            case NativeKeyEvent.VC_OPEN_BRACKET -> "[";
            case NativeKeyEvent.VC_CLOSE_BRACKET -> "]";
            case NativeKeyEvent.VC_BACK_SLASH -> "\\";
            case NativeKeyEvent.VC_SEMICOLON -> ";";
            case NativeKeyEvent.VC_QUOTE -> "'";
            case NativeKeyEvent.VC_COMMA -> ",";
            case NativeKeyEvent.VC_PERIOD -> ".";
            case NativeKeyEvent.VC_SLASH -> "/";
            case NativeKeyEvent.VC_BACKQUOTE -> "`";
            
            // Teclas especiais que mantemos como nomes
            case NativeKeyEvent.VC_ENTER -> "ENTER";
            case NativeKeyEvent.VC_TAB -> "TAB";
            case NativeKeyEvent.VC_BACKSPACE -> "BACKSPACE";
            case NativeKeyEvent.VC_DELETE -> "DELETE";
            case NativeKeyEvent.VC_ESCAPE -> "ESCAPE";
            case NativeKeyEvent.VC_INSERT -> "INSERT";
            case NativeKeyEvent.VC_HOME -> "HOME";
            case NativeKeyEvent.VC_END -> "END";
            case NativeKeyEvent.VC_PAGE_UP -> "PAGE_UP";
            case NativeKeyEvent.VC_PAGE_DOWN -> "PAGE_DOWN";
            case NativeKeyEvent.VC_UP -> "UP";
            case NativeKeyEvent.VC_DOWN -> "DOWN";
            case NativeKeyEvent.VC_LEFT -> "LEFT";
            case NativeKeyEvent.VC_RIGHT -> "RIGHT";
            
            // Teclas de função
            case NativeKeyEvent.VC_F1 -> "F1";
            case NativeKeyEvent.VC_F2 -> "F2";
            case NativeKeyEvent.VC_F3 -> "F3";
            case NativeKeyEvent.VC_F4 -> "F4";
            case NativeKeyEvent.VC_F5 -> "F5";
            case NativeKeyEvent.VC_F6 -> "F6";
            case NativeKeyEvent.VC_F7 -> "F7";
            case NativeKeyEvent.VC_F8 -> "F8";
            case NativeKeyEvent.VC_F9 -> "F9";
            case NativeKeyEvent.VC_F10 -> "F10";
            case NativeKeyEvent.VC_F11 -> "F11";
            case NativeKeyEvent.VC_F12 -> "F12";
            
         // Numpad - valores corretos
            case 96 -> "0";      // Numpad 0
            case 97 -> "1";      // Numpad 1
            case 98 -> "2";      // Numpad 2
            case 99 -> "3";      // Numpad 3
            case 100 -> "4";     // Numpad 4
            case 101 -> "5";     // Numpad 5
            case 102 -> "6";     // Numpad 6
            case 103 -> "7";     // Numpad 7
            case 104 -> "8";     // Numpad 8
            case 105 -> "9";     // Numpad 9
            case 106 -> "*";     // Numpad *
            case 107 -> "+";     // Numpad +
            case 109 -> "-";     // Numpad -
            case 110 -> ".";     // Numpad .
            case 111 -> "/";     // Numpad /
            
            // Caso não seja mapeado, usar o texto original
            default -> NativeKeyEvent.getKeyText(keyCode);
        };
    }
}