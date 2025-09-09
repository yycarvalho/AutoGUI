import java.awt.*;
import java.util.concurrent.CompletableFuture;

/**
 * Teste simples do sistema de verificação
 */
public class TesteSimples {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Testando Sistema de Verificação de Elementos ===");
            
            // Criar verificador
            VerificadorElementos verificador = new VerificadorElementos();
            
            // Teste 1: Verificação de posição
            System.out.println("\n1. Testando verificação de posição...");
            boolean posicaoOK = verificador.verificarElemento(100, 200, VerificadorElementos.TipoVerificacao.POSICAO);
            System.out.println("   Resultado: " + posicaoOK);
            
            // Teste 2: Verificação de cor
            System.out.println("\n2. Testando verificação de cor...");
            boolean corOK = verificador.verificarElemento(100, 200, VerificadorElementos.TipoVerificacao.COR, "#FF0000");
            System.out.println("   Resultado: " + corOK);
            
            // Teste 3: Verificação com timeout
            System.out.println("\n3. Testando verificação com timeout...");
            CompletableFuture<Boolean> resultado = verificador.verificarElementoComTimeout(
                100, 200, 
                VerificadorElementos.TipoVerificacao.ELEMENTO_VISIVEL, 
                null, 
                5 // 5 segundos
            );
            
            resultado.thenAccept(r -> {
                System.out.println("   Resultado com timeout: " + r);
            });
            
            // Aguardar resultado
            Thread.sleep(6000);
            
            System.out.println("\n✅ Teste concluído com sucesso!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}