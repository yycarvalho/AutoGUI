package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Acao {
    private int id;
    private TipoAcao tipo;
    private String detalhes;
    private int x, y;
    private LocalDateTime timestamp;
    private long delay; // tempo em milissegundos até a próxima ação
    
    public enum TipoAcao {
        MOUSE_CLICK, MOUSE_MOVE, SCROLL, KEY_PRESS, KEY_RELEASE, KEY_TYPE
    }
    
    public Acao(int id, TipoAcao tipo, String detalhes, int x, int y) {
        this.id = id;
        this.tipo = tipo;
        this.detalhes = detalhes;
        this.x = x;
        this.y = y;
        this.timestamp = LocalDateTime.now();
        this.delay = 0;
    }
    
    // Getters e Setters
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
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public long getDelay() { return delay; }
    public void setDelay(long delay) { this.delay = delay; }
    
    public String getTimestampFormatted() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
    
    @Override
    public String toString() {
        return String.format("Acao[%d]: %s - %s (%d,%d) - %s", 
            id, tipo, detalhes, x, y, getTimestampFormatted());
    }
}