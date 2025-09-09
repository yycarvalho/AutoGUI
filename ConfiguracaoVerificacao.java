package main;

/**
 * Configurações para o sistema de verificação de elementos
 */
public class ConfiguracaoVerificacao {
    
    // Configurações de timeout
    private int timeoutPadrao = 60; // segundos
    private int intervaloVerificacaoMs = 500; // milissegundos
    
    // Configurações de tolerância
    private int toleranciaPosicao = 5; // pixels
    private int toleranciaCor = 10; // 0-255
    private int limiarTransparencia = 50; // 0-255
    
    // Configurações de captura
    private int larguraCapturaTexto = 200; // pixels
    private int alturaCapturaTexto = 50; // pixels
    private int larguraCapturaImagem = 100; // pixels
    private int alturaCapturaImagem = 100; // pixels
    
    // Configurações de validação
    private boolean validarCaracteresEspeciais = true;
    private boolean caseSensitive = false;
    private boolean logDetalhado = true;
    
    public ConfiguracaoVerificacao() {
        // Valores padrão
    }
    
    // Getters e Setters
    public int getTimeoutPadrao() {
        return timeoutPadrao;
    }
    
    public void setTimeoutPadrao(int timeoutPadrao) {
        this.timeoutPadrao = Math.max(1, timeoutPadrao);
    }
    
    public int getIntervaloVerificacaoMs() {
        return intervaloVerificacaoMs;
    }
    
    public void setIntervaloVerificacaoMs(int intervaloVerificacaoMs) {
        this.intervaloVerificacaoMs = Math.max(100, intervaloVerificacaoMs);
    }
    
    public int getToleranciaPosicao() {
        return toleranciaPosicao;
    }
    
    public void setToleranciaPosicao(int toleranciaPosicao) {
        this.toleranciaPosicao = Math.max(0, toleranciaPosicao);
    }
    
    public int getToleranciaCor() {
        return toleranciaCor;
    }
    
    public void setToleranciaCor(int toleranciaCor) {
        this.toleranciaCor = Math.max(0, Math.min(255, toleranciaCor));
    }
    
    public int getLimiarTransparencia() {
        return limiarTransparencia;
    }
    
    public void setLimiarTransparencia(int limiarTransparencia) {
        this.limiarTransparencia = Math.max(0, Math.min(255, limiarTransparencia));
    }
    
    public int getLarguraCapturaTexto() {
        return larguraCapturaTexto;
    }
    
    public void setLarguraCapturaTexto(int larguraCapturaTexto) {
        this.larguraCapturaTexto = Math.max(50, larguraCapturaTexto);
    }
    
    public int getAlturaCapturaTexto() {
        return alturaCapturaTexto;
    }
    
    public void setAlturaCapturaTexto(int alturaCapturaTexto) {
        this.alturaCapturaTexto = Math.max(20, alturaCapturaTexto);
    }
    
    public int getLarguraCapturaImagem() {
        return larguraCapturaImagem;
    }
    
    public void setLarguraCapturaImagem(int larguraCapturaImagem) {
        this.larguraCapturaImagem = Math.max(20, larguraCapturaImagem);
    }
    
    public int getAlturaCapturaImagem() {
        return alturaCapturaImagem;
    }
    
    public void setAlturaCapturaImagem(int alturaCapturaImagem) {
        this.alturaCapturaImagem = Math.max(20, alturaCapturaImagem);
    }
    
    public boolean isValidarCaracteresEspeciais() {
        return validarCaracteresEspeciais;
    }
    
    public void setValidarCaracteresEspeciais(boolean validarCaracteresEspeciais) {
        this.validarCaracteresEspeciais = validarCaracteresEspeciais;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    public boolean isLogDetalhado() {
        return logDetalhado;
    }
    
    public void setLogDetalhado(boolean logDetalhado) {
        this.logDetalhado = logDetalhado;
    }
    
    @Override
    public String toString() {
        return String.format("ConfiguracaoVerificacao{timeout=%ds, intervalo=%dms, toleranciaPos=%dpx, toleranciaCor=%d, caseSensitive=%s}", 
            timeoutPadrao, intervaloVerificacaoMs, toleranciaPosicao, toleranciaCor, caseSensitive);
    }
}