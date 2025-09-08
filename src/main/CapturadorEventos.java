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
    private EventoListener listener;
    
    public interface EventoListener {
        void onNovoEvento(Acao acao);
    }
    
    public CapturadorEventos() {
        this.acoes = new CopyOnWriteArrayList<>();
        this.gravando = false;
        this.contadorId = 1;
        this.teclasPressionadas = new boolean[256];
        
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
        String botao = switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> "ESQUERDO";
            case NativeMouseEvent.BUTTON2 -> "MEIO";
            case NativeMouseEvent.BUTTON3 -> "DIREITO";
            default -> "DESCONHECIDO";
        };
        
        String detalhes = String.format("%s_%d_CLICKS", botao, e.getClickCount());
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.MOUSE_CLICK, detalhes, e.getX(), e.getY());
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        // Implementação específica se necessário
    }
    
    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        // Implementação específica se necessário
    }
    
    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        // Filtrar movimentos muito frequentes para evitar spam
        if (acoes.isEmpty() || 
            java.time.Duration.between(acoes.get(acoes.size()-1).getTimestamp(), 
                                     java.time.LocalDateTime.now()).toMillis() > 100) {
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
        String tecla = NativeKeyEvent.getKeyText(e.getKeyCode());
        String modificadores = getModificadores(e);
        String detalhes = modificadores.isEmpty() ? tecla : modificadores + "+" + tecla;
        
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.KEY_PRESS, detalhes, -1, -1);
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        String tecla = NativeKeyEvent.getKeyText(e.getKeyCode());
        String modificadores = getModificadores(e);
        String detalhes = modificadores.isEmpty() ? tecla : modificadores + "+" + tecla;
        
        Acao acao = new Acao(contadorId++, Acao.TipoAcao.KEY_RELEASE, detalhes, -1, -1);
        adicionarAcao(acao);
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        if (e.getKeyChar() != NativeKeyEvent.CHAR_UNDEFINED) {
            String detalhes = String.valueOf(e.getKeyChar());
            Acao acao = new Acao(contadorId++, Acao.TipoAcao.KEY_TYPE, detalhes, -1, -1);
            adicionarAcao(acao);
        }
    }
    
    private String getModificadores(NativeKeyEvent e) {
        List<String> mods = new ArrayList<>();
        if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) mods.add("CTRL");
        if ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) mods.add("ALT");
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) mods.add("SHIFT");
        if ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0) mods.add("META");
        return String.join("+", mods);
    }
}