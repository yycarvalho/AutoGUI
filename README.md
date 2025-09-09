# Sistema de Gravação e Reprodução de Ações

Um sistema robusto e moderno desenvolvido em Java 21 para gravação e reprodução de ações de mouse e teclado com validação visual e tratamento robusto de erros.

## Características Principais

- **Gravação em Tempo Real**: Captura ações de mouse e teclado com contexto visual completo
- **Validação Visual**: Verifica cores e screenshots antes de reproduzir ações
- **Persistência XML**: Exporta e importa sequências de ações em formato XML portável
- **Interface Gráfica Intuitiva**: Controles simples para gravação, reprodução e gerenciamento
- **Sistema de Logs Avançado**: Registro detalhado de todas as operações
- **Tratamento de Erros Robusto**: Recuperação automática e validação de contexto
- **Arquitetura Modular**: Código organizado em módulos especializados

## Requisitos do Sistema

- Java 21 ou superior
- Sistema operacional com suporte a Java AWT (Windows, Linux, macOS)
- Permissões para captura de eventos de mouse e teclado
- Mínimo 100MB de espaço em disco para logs e arquivos temporários

## Instalação e Execução

### Método 1: Script Automático (Recomendado)

```bash
./compile_and_run.sh
```

### Método 2: Compilação Manual

```bash
# Compilar todas as classes
javac -cp . *.java

# Executar a aplicação
java -cp . ActionRecorder
```

## Como Usar

### 1. Iniciando o Sistema

Execute o comando acima e a interface gráfica será aberta automaticamente.

### 2. Gravando Ações

1. Clique em "Iniciar Gravação"
2. Execute as ações desejadas (cliques, movimentos do mouse, digitação)
3. Clique em "Parar Gravação" quando terminar
4. O sistema capturará automaticamente:
   - Coordenadas absolutas e relativas
   - Cores dos pixels ao redor do clique
   - Screenshots parciais da região
   - Informações da janela ativa
   - Timestamp exato de cada ação

### 3. Reproduzindo Ações

1. Certifique-se de que as ações foram gravadas
2. Posicione o cursor na área apropriada
3. Clique em "Reproduzir"
4. O sistema validará o contexto visual antes de cada ação
5. Aguarde a conclusão da reprodução

### 4. Gerenciando Arquivos

- **Exportar XML**: Salva as ações gravadas em formato XML
- **Importar XML**: Carrega ações de um arquivo XML
- **Limpar Ações**: Remove todas as ações gravadas da memória

### 5. Configurações

- **Habilitar validação visual**: Ativa/desativa verificação de contexto
- **Tolerância**: Ajusta a precisão da validação (50% a 100%)

## Estrutura do Projeto

```
├── ActionRecorder.java          # Classe principal e orquestrador
├── ActionEvent.java             # Classes de dados para eventos
├── EventCaptureModule.java      # Captura de eventos de mouse/teclado
├── VisualAnalysisModule.java    # Análise visual e validação
├── XMLPersistenceModule.java    # Persistência em formato XML
├── PlaybackModule.java          # Reprodução de ações
├── LoggerModule.java            # Sistema de logging
├── MainGUI.java                 # Interface gráfica
├── compile_and_run.sh           # Script de compilação e execução
└── README.md                    # Este arquivo
```

## Arquitetura do Sistema

### Módulos Principais

1. **EventCaptureModule**: Captura eventos de mouse e teclado em tempo real
2. **VisualAnalysisModule**: Analisa cores e captura screenshots para validação
3. **XMLPersistenceModule**: Gerencia importação/exportação de dados
4. **PlaybackModule**: Executa ações gravadas com validação
5. **LoggerModule**: Sistema de logging com múltiplos níveis
6. **MainGUI**: Interface gráfica para controle do sistema

### Fluxo de Dados

```
Captura → Análise Visual → Armazenamento → Validação → Reprodução
    ↓           ↓              ↓            ↓           ↓
  Eventos → Screenshots → XML/Base64 → Verificação → Execução
```

## Recursos Avançados

### Validação Visual

- Captura de cores em raio de 10 pixels ao redor do clique
- Screenshots de 50x50 pixels para validação
- Tolerância configurável (padrão: 95%)
- Aguardar até 60 segundos por elemento carregar

### Tratamento de Erros

- Validação de contexto antes de cada ação
- Retry automático em caso de falha
- Logs detalhados de todas as operações
- Recuperação graceful de erros

### Portabilidade

- Formato XML com imagens em Base64
- Sem dependências externas além do Java
- Compatível com qualquer ambiente desktop Java

## Logs e Monitoramento

O sistema gera logs detalhados em:
- Console (tempo real)
- Arquivo `action_recorder.log`
- Interface gráfica (últimas 50 entradas)

Níveis de log disponíveis:
- DEBUG: Informações detalhadas
- INFO: Operações normais
- WARNING: Avisos não críticos
- ERROR: Erros que requerem atenção

## Solução de Problemas

### Problemas Comuns

1. **"Erro ao capturar eventos"**
   - Verifique se o Java tem permissões adequadas
   - Execute com privilégios elevados se necessário

2. **"Contexto visual inválido"**
   - Ajuste a tolerância na interface
   - Verifique se a tela não mudou desde a gravação

3. **"Timeout na validação"**
   - Aumente o tempo de espera
   - Verifique se o elemento está visível

### Logs de Debug

Para ativar logs detalhados, modifique o nível no código:
```java
logger.setMinLogLevel(LoggerModule.LogLevel.DEBUG);
```

## Limitações e Considerações

- Requer ambiente desktop com Java AWT
- Performance pode ser afetada em sistemas lentos
- Validação visual pode falhar em ambientes dinâmicos
- Alguns aplicativos podem bloquear captura de eventos

## Desenvolvimento e Extensibilidade

O sistema foi projetado com arquitetura modular para facilitar extensões:

- Adicionar novos tipos de eventos
- Implementar validações customizadas
- Integrar com outros sistemas
- Personalizar interface gráfica

## Licença

Este projeto é fornecido como exemplo educacional e de demonstração.

## Suporte

Para questões técnicas ou melhorias, consulte os logs do sistema e a documentação do código.