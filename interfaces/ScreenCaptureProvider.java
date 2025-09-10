package main.interfaces;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Interface para captura de tela e análise de contexto visual
 * Permite injeção de dependência para testes unitários
 */
public interface ScreenCaptureProvider {
    
    /**
     * Captura uma região específica da tela
     * @param bounds Região a ser capturada
     * @return Imagem capturada
     */
    BufferedImage capture(Rectangle bounds);
    
    /**
     * Captura uma região ao redor de um ponto central
     * @param centerX Coordenada X do centro
     * @param centerY Coordenada Y do centro
     * @param width Largura da região
     * @param height Altura da região
     * @return Imagem capturada
     */
    BufferedImage captureAround(int centerX, int centerY, int width, int height);
    
    /**
     * Obtém os limites da janela ativa
     * @return Retângulo com os limites da janela ativa
     */
    Rectangle getActiveWindowBounds();
    
    /**
     * Obtém o título da janela ativa
     * @return Título da janela ativa
     */
    String getActiveWindowTitle();
    
    /**
     * Obtém o PID da janela ativa (se disponível)
     * @return PID da janela ativa ou -1 se não disponível
     */
    long getActiveWindowPid();
    
    /**
     * Verifica se uma janela está visível e ativa
     * @param windowTitle Título da janela a verificar
     * @return true se a janela está visível e ativa
     */
    boolean isWindowActive(String windowTitle);
}