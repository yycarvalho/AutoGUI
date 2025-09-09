package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Sistema de verificação de elementos na tela com validações robustas
 */
public class VerificadorElementos {
    
    private static final Logger logger = Logger.getLogger(VerificadorElementos.class.getName());
    private Robot robot;
    private ConfiguracaoVerificacao configuracao;
    
    public VerificadorElementos() throws AWTException {
        this.robot = new Robot();
        this.configuracao = new ConfiguracaoVerificacao();
    }
    
    public VerificadorElementos(ConfiguracaoVerificacao configuracao) throws AWTException {
        this.robot = new Robot();
        this.configuracao = configuracao != null ? configuracao : new ConfiguracaoVerificacao();
    }
    
    /**
     * Verifica se um elemento está presente na posição especificada
     */
    public boolean verificarElemento(int x, int y, TipoVerificacao tipoVerificacao) {
        return verificarElemento(x, y, tipoVerificacao, null);
    }
    
    /**
     * Verifica se um elemento está presente na posição especificada com valor esperado
     */
    public boolean verificarElemento(int x, int y, TipoVerificacao tipoVerificacao, String valorEsperado) {
        try {
            switch (tipoVerificacao) {
                case POSICAO -> {
                    return verificarPosicao(x, y);
                }
                case COR -> {
                    return verificarCor(x, y, valorEsperado);
                }
                case TEXTO -> {
                    return verificarTexto(x, y, valorEsperado);
                }
                case IMAGEM -> {
                    // valorEsperado pode ser Base64 de referência
                    return verificarImagem(x, y, valorEsperado);
                }
                case ELEMENTO_VISIVEL -> {
                    return verificarElementoVisivel(x, y);
                }
                default -> {
                    logger.warning("Tipo de verificação não suportado: " + tipoVerificacao);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro durante verificação de elemento", e);
            return false;
        }
    }
    
    /**
     * Verifica se um elemento está presente com timeout configurável
     */
    public CompletableFuture<Boolean> verificarElementoComTimeout(int x, int y, 
            TipoVerificacao tipoVerificacao, String valorEsperado, int timeoutSegundos) {
        
        return CompletableFuture.supplyAsync(() -> {
            long inicio = System.currentTimeMillis();
            long timeoutMs = timeoutSegundos * 1000L;
            
            while (System.currentTimeMillis() - inicio < timeoutMs) {
                if (verificarElemento(x, y, tipoVerificacao, valorEsperado)) {
                    logger.info(String.format("Elemento encontrado em (%d,%d) após %dms", 
                        x, y, System.currentTimeMillis() - inicio));
                    return true;
                }
                
                try {
                    Thread.sleep(configuracao.getIntervaloVerificacaoMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Verificação interrompida");
                    return false;
                }
            }
            
            logger.warning(String.format("Timeout: Elemento não encontrado em (%d,%d) após %ds", 
                x, y, timeoutSegundos));
            return false;
        });
    }
    
    // Métodos privados de verificação serão implementados em seguida...
    
    private boolean verificarPosicao(int x, int y) {
        Point posicaoAtual = MouseInfo.getPointerInfo().getLocation();
        int tolerancia = configuracao.getToleranciaPosicao();
        
        boolean dentroTolerancia = Math.abs(posicaoAtual.x - x) <= tolerancia && 
                                  Math.abs(posicaoAtual.y - y) <= tolerancia;
        
        if (dentroTolerancia && configuracao.isLogDetalhado()) {
            logger.fine(String.format("Posição verificada: atual(%d,%d) esperada(%d,%d) tolerância(%d)", 
                posicaoAtual.x, posicaoAtual.y, x, y, tolerancia));
        }
        
        return dentroTolerancia;
    }
    
    private boolean verificarCor(int x, int y, String valorEsperado) {
        if (valorEsperado == null || valorEsperado.trim().isEmpty()) {
            logger.warning("Valor esperado para verificação de cor não informado");
            return false;
        }
        
        try {
            Color corEsperada = parseColor(valorEsperado);
            Color corAtual = robot.getPixelColor(x, y);
            
            boolean coresCorrespondem = coresSimilares(corAtual, corEsperada, configuracao.getToleranciaCor());
            
            if (coresCorrespondem && configuracao.isLogDetalhado()) {
                logger.fine(String.format("Cor verificada: atual(%s) esperada(%s) em (%d,%d)", 
                    corAtual, corEsperada, x, y));
            }
            
            return coresCorrespondem;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao verificar cor: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean verificarTexto(int x, int y, String valorEsperado) {
        if (valorEsperado == null || valorEsperado.trim().isEmpty()) {
            logger.warning("Valor esperado para verificação de texto não informado");
            return false;
        }
        
        try {
            int largura = configuracao.getLarguraCapturaTexto();
            int altura = configuracao.getAlturaCapturaTexto();
            
            Rectangle area = new Rectangle(x - largura/2, y - altura/2, largura, altura);
            BufferedImage captura = robot.createScreenCapture(area);
            
            String textoCapturado = extrairTexto(captura);
            String textoEsperado = configuracao.isCaseSensitive() ? valorEsperado : valorEsperado.toLowerCase();
            String textoCapturadoComparar = configuracao.isCaseSensitive() ? textoCapturado : textoCapturado.toLowerCase();
            
            boolean textoCorresponde = textoCapturadoComparar.contains(textoEsperado);
            
            if (textoCorresponde && configuracao.isLogDetalhado()) {
                logger.fine(String.format("Texto verificado: capturado('%s') esperado('%s') em (%d,%d)", 
                    textoCapturado, valorEsperado, x, y));
            }
            
            return textoCorresponde;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao verificar texto: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean verificarImagem(int x, int y, String valorEsperado) {
        if (valorEsperado == null || valorEsperado.trim().isEmpty()) {
            logger.warning("Valor esperado para verificação de imagem não informado");
            return false;
        }
        
        try {
            int largura = configuracao.getLarguraCapturaImagem();
            int altura = configuracao.getAlturaCapturaImagem();
            
            Rectangle area = new Rectangle(x - largura/2, y - altura/2, largura, altura);
            BufferedImage captura = robot.createScreenCapture(area);
            
            boolean imagemCorresponde = compararImagensBase64(captura, valorEsperado, 10);
            
            if (imagemCorresponde && configuracao.isLogDetalhado()) {
                logger.fine(String.format("Imagem verificada: referência('%s') em (%d,%d)", 
                    valorEsperado, x, y));
            }
            
            return imagemCorresponde;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao verificar imagem: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean verificarImagemAreaTolBase64(int x, int y, int largura, int altura, String referenciaBase64, int toleranciaPercentual) {
        try {
            Rectangle area = new Rectangle(x - largura/2, y - altura/2, largura, altura);
            BufferedImage captura = robot.createScreenCapture(area);
            return compararImagensBase64(captura, referenciaBase64, toleranciaPercentual);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao verificar imagem com área/tolerância: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean verificarElementoVisivel(int x, int y) {
        try {
            Color cor = robot.getPixelColor(x, y);
            boolean visivel = cor.getAlpha() > configuracao.getLimiarTransparencia();
            
            if (visivel && configuracao.isLogDetalhado()) {
                logger.fine(String.format("Elemento visível verificado em (%d,%d) com cor %s", 
                    x, y, cor));
            }
            
            return visivel;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro ao verificar elemento visível: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean coresSimilares(Color cor1, Color cor2, int tolerancia) {
        return Math.abs(cor1.getRed() - cor2.getRed()) <= tolerancia &&
               Math.abs(cor1.getGreen() - cor2.getGreen()) <= tolerancia &&
               Math.abs(cor1.getBlue() - cor2.getBlue()) <= tolerancia;
    }
    
    private String extrairTexto(BufferedImage imagem) {
        StringBuilder texto = new StringBuilder();
        
        Raster raster = imagem.getRaster();
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        
        for (int y = 0; y < altura; y += 2) {
            for (int x = 0; x < largura; x += 2) {
                int[] pixel = new int[3];
                raster.getPixel(x, y, pixel);
                
                if (pixel[0] < 128 && pixel[1] < 128 && pixel[2] < 128) {
                    texto.append("TEXTO_DETECTADO");
                    break;
                }
            }
            if (texto.length() > 0) break;
        }
        
        return texto.toString();
    }
    
    private boolean compararImagensBase64(BufferedImage captura, String referenciaBase64, int toleranciaPercentual) {
        try {
            if (referenciaBase64 == null || referenciaBase64.isEmpty()) return false;
            byte[] bytes = java.util.Base64.getDecoder().decode(referenciaBase64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            BufferedImage referencia = javax.imageio.ImageIO.read(bais);
            if (referencia == null || captura == null) return false;
            int w = Math.min(captura.getWidth(), referencia.getWidth());
            int h = Math.min(captura.getHeight(), referencia.getHeight());
            long totalPixels = (long) w * (long) h;
            long diferentes = 0;
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    int rgb1 = captura.getRGB(xx, yy);
                    int rgb2 = referencia.getRGB(xx, yy);
                    if (!coresSimilares(new Color(rgb1), new Color(rgb2), configuracao.getToleranciaCor())) {
                        diferentes++;
                    }
                }
            }
            double percDiferenca = (diferentes * 100.0) / totalPixels;
            return percDiferenca <= toleranciaPercentual;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Erro comparando imagens Base64: " + e.getMessage(), e);
            return false;
        }
    }
    
    private Color parseColor(String corStr) {
        try {
            if (corStr.startsWith("#")) {
                return Color.decode(corStr);
            } else if (corStr.startsWith("rgb(")) {
                String[] valores = corStr.substring(4, corStr.length() - 1).split(",");
                int r = Integer.parseInt(valores[0].trim());
                int g = Integer.parseInt(valores[1].trim());
                int b = Integer.parseInt(valores[2].trim());
                return new Color(r, g, b);
            } else {
                return (Color) Color.class.getField(corStr.toUpperCase()).get(null);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de cor inválido: " + corStr, e);
        }
    }
    
    // Getters e Setters
    public ConfiguracaoVerificacao getConfiguracao() {
        return configuracao;
    }
    
    public void setConfiguracao(ConfiguracaoVerificacao configuracao) {
        this.configuracao = configuracao;
    }
    
    /**
     * Enum para tipos de verificação
     */
    public enum TipoVerificacao {
        POSICAO("Posição"),
        COR("Cor"),
        TEXTO("Texto"),
        IMAGEM("Imagem"),
        ELEMENTO_VISIVEL("Elemento Visível");
        
        private final String descricao;
        
        TipoVerificacao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        @Override
        public String toString() {
            return descricao;
        }
    }
}
