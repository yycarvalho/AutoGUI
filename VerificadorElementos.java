package main;

import main.interfaces.ImageComparator;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Classe para verificação e comparação de elementos visuais
 * Implementa diferentes estratégias de comparação com tolerância
 */
public class VerificadorElementos implements ImageComparator {
    
    private static final Logger logger = Logger.getLogger(VerificadorElementos.class.getName());
    
    @Override
    public boolean matches(BufferedImage expected, BufferedImage actual, double tolerancePct) {
        if (expected == null || actual == null) {
            logger.warning("Imagens nulas para comparação");
            return false;
        }
        
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            logger.warning("Dimensões diferentes: expected=" + expected.getWidth() + "x" + expected.getHeight() + 
                          ", actual=" + actual.getWidth() + "x" + actual.getHeight());
            return false;
        }
        
        double similarity = calculateSimilarity(expected, actual);
        boolean matches = similarity >= tolerancePct;
        
        logger.fine(String.format("Comparação: similaridade=%.2f%%, tolerância=%.2f%%, resultado=%s", 
                    similarity, tolerancePct, matches ? "MATCH" : "NO MATCH"));
        
        return matches;
    }
    
    @Override
    public double calculateSimilarity(BufferedImage expected, BufferedImage actual) {
        if (expected == null || actual == null) {
            return 0.0;
        }
        
        int width = Math.min(expected.getWidth(), actual.getWidth());
        int height = Math.min(expected.getHeight(), actual.getHeight());
        
        int totalPixels = width * height;
        int matchingPixels = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int expectedRGB = expected.getRGB(x, y);
                int actualRGB = actual.getRGB(x, y);
                
                if (expectedRGB == actualRGB) {
                    matchingPixels++;
                }
            }
        }
        
        return (double) matchingPixels / totalPixels * 100.0;
    }
    
    @Override
    public boolean matchesByRMS(BufferedImage expected, BufferedImage actual, double threshold) {
        if (expected == null || actual == null) {
            return false;
        }
        
        int width = Math.min(expected.getWidth(), actual.getWidth());
        int height = Math.min(expected.getHeight(), actual.getHeight());
        
        double sumSquaredDiff = 0.0;
        int totalPixels = width * height;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int expectedRGB = expected.getRGB(x, y);
                int actualRGB = actual.getRGB(x, y);
                
                // Calcular diferença por componente RGB
                int expectedR = (expectedRGB >> 16) & 0xFF;
                int expectedG = (expectedRGB >> 8) & 0xFF;
                int expectedB = expectedRGB & 0xFF;
                
                int actualR = (actualRGB >> 16) & 0xFF;
                int actualG = (actualRGB >> 8) & 0xFF;
                int actualB = actualRGB & 0xFF;
                
                double diffR = expectedR - actualR;
                double diffG = expectedG - actualG;
                double diffB = expectedB - actualB;
                
                sumSquaredDiff += diffR * diffR + diffG * diffG + diffB * diffB;
            }
        }
        
        double rms = Math.sqrt(sumSquaredDiff / (totalPixels * 3)); // 3 componentes RGB
        boolean matches = rms <= threshold;
        
        logger.fine(String.format("RMS: %.2f, threshold: %.2f, resultado: %s", 
                    rms, threshold, matches ? "MATCH" : "NO MATCH"));
        
        return matches;
    }
    
    @Override
    public boolean matchesByHistogram(BufferedImage expected, BufferedImage actual, double tolerancePct) {
        if (expected == null || actual == null) {
            return false;
        }
        
        int[] expectedHistogram = calculateHistogram(expected);
        int[] actualHistogram = calculateHistogram(actual);
        
        if (expectedHistogram.length != actualHistogram.length) {
            return false;
        }
        
        int totalPixels = expected.getWidth() * expected.getHeight();
        int matchingBins = 0;
        
        for (int i = 0; i < expectedHistogram.length; i++) {
            double expectedPct = (double) expectedHistogram[i] / totalPixels * 100.0;
            double actualPct = (double) actualHistogram[i] / totalPixels * 100.0;
            
            if (Math.abs(expectedPct - actualPct) <= tolerancePct) {
                matchingBins++;
            }
        }
        
        double similarity = (double) matchingBins / expectedHistogram.length * 100.0;
        boolean matches = similarity >= tolerancePct;
        
        logger.fine(String.format("Histograma: similaridade=%.2f%%, tolerância=%.2f%%, resultado=%s", 
                    similarity, tolerancePct, matches ? "MATCH" : "NO MATCH"));
        
        return matches;
    }
    
    /**
     * Calcula histograma de cores da imagem
     * @param image Imagem para calcular histograma
     * @return Array com contagem de pixels por cor
     */
    private int[] calculateHistogram(BufferedImage image) {
        int[] histogram = new int[256]; // 256 tons de cinza
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Converter para escala de cinza
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                histogram[gray]++;
            }
        }
        
        return histogram;
    }
    
    /**
     * Verifica se um elemento está presente na tela com retry e backoff exponencial
     * @param expected Imagem esperada
     * @param actual Imagem atual
     * @param tolerancePct Tolerância percentual
     * @param maxWaitMs Tempo máximo de espera em ms
     * @param retryPolicy Política de retry (abort, skip, continue)
     * @return Resultado da verificação
     */
    public VerificationResult verifyElementWithRetry(BufferedImage expected, BufferedImage actual, 
                                                   double tolerancePct, long maxWaitMs, String retryPolicy) {
        long startTime = System.currentTimeMillis();
        long currentWait = 500; // Começar com 500ms
        int attempt = 1;
        
        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            if (matches(expected, actual, tolerancePct)) {
                return new VerificationResult(true, attempt, 
                    System.currentTimeMillis() - startTime, "Elemento encontrado");
            }
            
            logger.info(String.format("Tentativa %d falhou, aguardando %dms...", attempt, currentWait));
            
            try {
                Thread.sleep(currentWait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new VerificationResult(false, attempt, 
                    System.currentTimeMillis() - startTime, "Interrompido: " + e.getMessage());
            }
            
            // Backoff exponencial
            currentWait = Math.min(currentWait * 2, 5000); // Máximo 5 segundos
            attempt++;
        }
        
        String message = String.format("Timeout após %d tentativas em %dms", 
            attempt, System.currentTimeMillis() - startTime);
        
        return new VerificationResult(false, attempt, 
            System.currentTimeMillis() - startTime, message);
    }
    
    /**
     * Classe para resultado da verificação
     */
    public static class VerificationResult {
        private final boolean success;
        private final int attempts;
        private final long durationMs;
        private final String message;
        
        public VerificationResult(boolean success, int attempts, long durationMs, String message) {
            this.success = success;
            this.attempts = attempts;
            this.durationMs = durationMs;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public int getAttempts() { return attempts; }
        public long getDurationMs() { return durationMs; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("VerificationResult{success=%s, attempts=%d, duration=%dms, message='%s'}", 
                success, attempts, durationMs, message);
        }
    }
}
