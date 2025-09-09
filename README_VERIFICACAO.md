# Sistema de Verificação de Elementos - MapeadorAtividades

## Visão Geral

Este sistema estende o MapeadorAtividades com funcionalidades robustas de verificação de elementos na tela, permitindo validar se elementos estão presentes antes de executar ações automatizadas.

## Funcionalidades Principais

### 1. Verificação de Elementos
- **Verificação de Posição**: Valida se o mouse está na posição esperada
- **Verificação de Cor**: Compara cores em posições específicas
- **Verificação de Texto**: Detecta texto na tela usando OCR básico
- **Verificação de Imagem**: Compara imagens na tela
- **Verificação de Elemento Visível**: Verifica se há elementos não transparentes

### 2. Sistema de Timeout
- Timeout configurável (padrão: 60 segundos)
- Verificação periódica com intervalo ajustável
- Parada automática da automação se elemento não for encontrado

### 3. Configurações Avançadas
- Tolerância de posição (pixels)
- Tolerância de cor (0-255)
- Limiar de transparência
- Dimensões de captura para texto/imagem
- Validação de caracteres especiais
- Case sensitive para texto

## Como Usar

### 1. Ativação da Verificação
1. Clique em "🔍 Ativar Verificação" para habilitar o sistema
2. Configure as opções clicando em "⚙️ Configurar Verificação"
3. Inicie a gravação normalmente

### 2. Configuração de Verificação
- **Timeout Padrão**: Tempo máximo para aguardar elemento (1-300s)
- **Intervalo de Verificação**: Frequência das verificações (100-5000ms)
- **Tolerância de Posição**: Margem de erro em pixels (0-50px)
- **Tolerância de Cor**: Diferença aceitável de cor (0-255)
- **Limiar de Transparência**: Valor mínimo de opacidade (0-255)

### 3. Tipos de Verificação Automática
- **Cliques de Mouse**: Verifica se há elemento visível na posição
- **Digitação de Texto**: Verifica se o texto foi digitado corretamente
- **Movimentos de Mouse**: Verifica posição final

## Estrutura do XML

O sistema salva as configurações de verificação no XML:

```xml
<acao id="1" tipo="MOUSE_CLICK" detalhes="ESQUERDO_1" x="100" y="200" 
      timestamp="2024-01-01T10:00:00.000" delay="1000"
      verificarElemento="true" 
      tipoVerificacao="ELEMENTO_VISIVEL" 
      valorEsperado="" 
      timeoutVerificacao="60" />
```

## Exemplo de Uso Programático

```java
// Criar verificador com configuração personalizada
ConfiguracaoVerificacao config = new ConfiguracaoVerificacao();
config.setTimeoutPadrao(30);
config.setToleranciaPosicao(10);

VerificadorElementos verificador = new VerificadorElementos(config);

// Verificação simples
boolean ok = verificador.verificarElemento(100, 200, 
    VerificadorElementos.TipoVerificacao.POSICAO);

// Verificação com timeout
CompletableFuture<Boolean> resultado = verificador.verificarElementoComTimeout(
    100, 200, 
    VerificadorElementos.TipoVerificacao.COR, 
    "#FF0000", 
    10
);
```

## Validações de Caracteres

O sistema inclui validação robusta para:
- Caracteres acentuados (á, é, í, ó, ú, ç, etc.)
- Símbolos especiais (!, @, #, $, %, etc.)
- Teclas de função (F1-F12)
- Teclas de navegação (Home, End, Page Up/Down)
- Teclas do numpad

## Logs e Debugging

- Logs detalhados de todas as verificações
- Informações de timeout e erros
- Status de verificação em tempo real
- Configuração de nível de log

## Limitações

1. **OCR Básico**: O reconhecimento de texto é simplificado
2. **Comparação de Imagens**: Implementação básica
3. **Performance**: Verificações frequentes podem impactar performance
4. **Compatibilidade**: Funciona apenas em sistemas com Java AWT

## Troubleshooting

### Elemento não encontrado
- Verifique se a tolerância de posição está adequada
- Confirme se o elemento está visível na tela
- Ajuste o timeout se necessário

### Verificação de cor falhando
- Verifique se a cor está no formato correto (#RRGGBB ou rgb(r,g,b))
- Ajuste a tolerância de cor
- Confirme se a posição está correta

### Performance lenta
- Aumente o intervalo de verificação
- Reduza a área de captura
- Desative logs detalhados se não necessário

## Arquivos Principais

- `VerificadorElementos.java`: Classe principal de verificação
- `ConfiguracaoVerificacao.java`: Configurações do sistema
- `ConfiguracaoVerificacaoDialog.java`: Interface de configuração
- `Acao.java`: Extensão com propriedades de verificação
- `GerenciadorXML.java`: Salvamento/carregamento de configurações
- `ReprodutorEventos.java`: Integração com reprodução
- `MapeadorAtividades.java`: Interface principal atualizada