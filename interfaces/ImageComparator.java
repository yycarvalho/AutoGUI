package main.interfaces;

import java.awt.image.BufferedImage;

/**
 * Interface para comparação de imagens com diferentes estratégias
 * Permite injeção de dependência para testes unitários
 */
public interface ImageComparator {
    
    /**
     * Compara duas imagens com tolerância percentual
     * @param expected Imagem esperada
     * @param actual Imagem atual
     * @param tolerancePct Tolerância percentual (0-100)
     * @return true se as imagens são similares dentro da tolerância
     */
    boolean matches(BufferedImage expected, BufferedImage actual, double tolerancePct);
    
    /**
     * Calcula a similaridade entre duas imagens
     * @param expected Imagem esperada
     * @param actual Imagem atual
     * @return Percentual de similaridade (0-100)
     */
    double calculateSimilarity(BufferedImage expected, BufferedImage actual);
    
    /**
     * Compara usando RMS (Root Mean Square)
     * @param expected Imagem esperada
     * @param actual Imagem atual
     * @param threshold Limite de diferença RMS
     * @return true se a diferença RMS está abaixo do limite
     */
    boolean matchesByRMS(BufferedImage expected, BufferedImage actual, double threshold);
    
    /**
     * Compara usando histograma de cores
     * @param expected Imagem esperada
     * @param actual Imagem atual
     * @param tolerancePct Tolerância percentual
     * @return true se os histogramas são similares
     */
    boolean matchesByHistogram(BufferedImage expected, BufferedImage actual, double tolerancePct);
}