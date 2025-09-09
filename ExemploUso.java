package main;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Exemplo de uso do sistema de verificação de elementos
 */
public class ExemploUso {
    
    public static void main(String[] args) {
        try {
            // Criar verificador com configuração personalizada
            ConfiguracaoVerificacao config = new ConfiguracaoVerificacao();
            config.setTimeoutPadrao(30); // 30 segundos de timeout
            config.setToleranciaPosicao(10); // 10 pixels de tolerância
            config.setLogDetalhado(true);
            
            VerificadorElementos verificador = new VerificadorElementos(config);
            
            // Exemplo 1: Verificação simples de posição
            System.out.println("=== Exemplo 1: Verificação de Posição ===");
            boolean posicaoOK = verificador.verificarElemento(100, 200, VerificadorElementos.TipoVerificacao.POSICAO);
            System.out.println("Posição verificada: " + posicaoOK);
            
            // Exemplo 2: Verificação de cor
            System.out.println("\n=== Exemplo 2: Verificação de Cor ===");
            boolean corOK = verificador.verificarElemento(100, 200, VerificadorElementos.TipoVerificacao.COR, "#FF0000");
            System.out.println("Cor verificada: " + corOK);
            
            // Exemplo 3: Verificação com timeout
            System.out.println("\n=== Exemplo 3: Verificação com Timeout ===");
            CompletableFuture<Boolean> verificacaoFutura = verificador.verificarElementoComTimeout(
                100, 200, 
                VerificadorElementos.TipoVerificacao.ELEMENTO_VISIVEL, 
                null, 
                10 // 10 segundos
            );
            
            verificacaoFutura.thenAccept(resultado -> {
                System.out.println("Verificação com timeout concluída: " + resultado);
            });
            
            // Exemplo 4: Uso com ReprodutorEventos
            System.out.println("\n=== Exemplo 4: Uso com ReprodutorEventos ===");
            ReprodutorEventos reprodutor = new ReprodutorEventos(verificador);
            
            // Simular uma ação com verificação
            Acao acao = new Acao(1, Acao.TipoAcao.MOUSE_CLICK, "ESQUERDO_1", 100, 200);
            acao.setVerificarElemento(true);
            acao.setTipoVerificacao(VerificadorElementos.TipoVerificacao.ELEMENTO_VISIVEL);
            acao.setTimeoutVerificacao(15);
            
            System.out.println("Ação configurada: " + acao.toString());
            
            // Aguardar um pouco para ver os resultados
            Thread.sleep(2000);
            
        } catch (Exception e) {
            System.err.println("Erro no exemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}