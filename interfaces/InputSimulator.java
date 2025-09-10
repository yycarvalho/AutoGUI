package main.interfaces;

import java.awt.Point;

/**
 * Interface para simulação de entrada (mouse e teclado)
 * Permite injeção de dependência para testes unitários
 */
public interface InputSimulator {
    
    /**
     * Move o mouse para uma posição específica
     * @param point Posição de destino
     */
    void moveMouse(Point point);
    
    /**
     * Clica em uma posição específica
     * @param point Posição do clique
     * @param button Botão do mouse (1=esquerdo, 2=meio, 3=direito)
     * @param clickCount Número de cliques
     */
    void click(Point point, int button, int clickCount);
    
    /**
     * Pressiona uma tecla
     * @param keyCode Código da tecla
     */
    void keyPress(int keyCode);
    
    /**
     * Libera uma tecla
     * @param keyCode Código da tecla
     */
    void keyRelease(int keyCode);
    
    /**
     * Executa scroll do mouse
     * @param point Posição do mouse
     * @param direction Direção do scroll (-1 para cima, 1 para baixo)
     * @param clicks Número de cliques de scroll
     */
    void mouseWheel(Point point, int direction, int clicks);
    
    /**
     * Aplica um delay/pausa
     * @param milliseconds Milissegundos de pausa
     */
    void delay(int milliseconds);
    
    /**
     * Verifica se o simulador está disponível
     * @return true se disponível
     */
    boolean isAvailable();
}