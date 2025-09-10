package main.test;

import main.VerificadorElementos;
import java.awt.image.BufferedImage;
import java.awt.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para VerificadorElementos
 */
public class VerificadorElementosTest {
    
    private VerificadorElementos verificador;
    
    @BeforeEach
    void setUp() {
        verificador = new VerificadorElementos();
    }
    
    @Test
    void testMatchesIdenticalImages() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);
        
        assertTrue(verificador.matches(img1, img2, 95.0));
    }
    
    @Test
    void testMatchesDifferentImages() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.BLUE);
        
        assertFalse(verificador.matches(img1, img2, 95.0));
    }
    
    @Test
    void testMatchesWithTolerance() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);
        
        // Alterar alguns pixels
        img2.setRGB(5, 5, Color.BLUE.getRGB());
        img2.setRGB(6, 6, Color.BLUE.getRGB());
        
        // Com 90% de tolerância, deve passar (98% de similaridade)
        assertTrue(verificador.matches(img1, img2, 90.0));
        
        // Com 99% de tolerância, deve falhar
        assertFalse(verificador.matches(img1, img2, 99.0));
    }
    
    @Test
    void testCalculateSimilarity() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);
        
        double similarity = verificador.calculateSimilarity(img1, img2);
        assertEquals(100.0, similarity, 0.1);
    }
    
    @Test
    void testMatchesByRMS() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);
        
        assertTrue(verificador.matchesByRMS(img1, img2, 10.0));
        
        // Imagem completamente diferente
        BufferedImage img3 = createTestImage(10, 10, Color.BLUE);
        assertFalse(verificador.matchesByRMS(img1, img3, 10.0));
    }
    
    @Test
    void testMatchesByHistogram() {
        BufferedImage img1 = createTestImage(10, 10, Color.RED);
        BufferedImage img2 = createTestImage(10, 10, Color.RED);
        
        assertTrue(verificador.matchesByHistogram(img1, img2, 5.0));
        
        // Imagem com cores diferentes
        BufferedImage img3 = createTestImage(10, 10, Color.BLUE);
        assertFalse(verificador.matchesByHistogram(img1, img3, 5.0));
    }
    
    @Test
    void testVerifyElementWithRetry() {
        BufferedImage expected = createTestImage(10, 10, Color.RED);
        BufferedImage actual = createTestImage(10, 10, Color.RED);
        
        var result = verificador.verifyElementWithRetry(expected, actual, 95.0, 1000, "abort");
        
        assertTrue(result.isSuccess());
        assertEquals(1, result.getAttempts());
    }
    
    @Test
    void testVerifyElementWithRetryTimeout() {
        BufferedImage expected = createTestImage(10, 10, Color.RED);
        BufferedImage actual = createTestImage(10, 10, Color.BLUE);
        
        var result = verificador.verifyElementWithRetry(expected, actual, 99.0, 100, "abort");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getAttempts() > 1);
    }
    
    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }
}