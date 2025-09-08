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
    
    // Controle de clicks para detectar duplo-clique e suportar botão direito
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
            case NativeMouseEvent.BUTTON2 -> "MEIO";
            case NativeMouseEvent.BUTTON3 -> "DIREITO";
            default -> "DESCONHECIDO";
        };
        long agora = System.currentTimeMillis();
        long ultimoClickMs;
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> ultimoClickMs = ultimoClickEsqMs;
            case NativeMouseEvent.BUTTON2 -> ultimoClickMs = ultimoClickMeioMs;
            case NativeMouseEvent.BUTTON3 -> ultimoClickMs = ultimoClickDirMs;
            default -> ultimoClickMs = 0L;
        }
        int clicks = 1;
        if (ultimoClickMs > 0
                && (agora - ultimoClickMs) <= INTERVALO_DUPOLO_CLique_MS
                && distancia(e.getX(), e.getY(), ultimoClickX, ultimoClickY) <= DISTANCIA_MAX_DUPOLO_CLique_PX) {
            clicks = 2;
        }
        // Atualizar estado do último click para o botão correspondente
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> ultimoClickEsqMs = agora;
            case NativeMouseEvent.BUTTON2 -> ultimoClickMeioMs = agora;
            case NativeMouseEvent.BUTTON3 -> ultimoClickDirMs = agora;
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
                return; // já registrada como pressionada, evita repetição (auto-repeat)
            }
            teclasPressionadas[keyCode] = true;
        }
        String tecla = NativeKeyEvent.getKeyText(keyCode);
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
        String tecla = NativeKeyEvent.getKeyText(keyCode);
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
}