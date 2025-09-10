# AutoGUI - Sistema de Captura e Reprodução de Ações

Sistema robusto para captura, validação e reprodução de ações de mouse e teclado com arquitetura modular e recursos avançados de verificação visual.

## 🏗️ Arquitetura

O sistema foi modularizado em 5 camadas principais:

1. **Input Capture** - Captura de eventos de mouse e teclado
2. **Context Analyzer** - Análise visual e identificação de elementos
3. **Persistence** - Gerenciamento de XML com validação XSD
4. **Playback** - Reprodução com validação e retry
5. **UI/Controller** - Interface gráfica e configurações

## ✨ Funcionalidades

### Captura Avançada
- ✅ Captura de coordenadas absolutas e relativas à janela
- ✅ Metadados da janela ativa (título, PID)
- ✅ Pixel samples (21x21) para validação precisa
- ✅ Screenshots recortados (100x100) para contexto visual
- ✅ Suporte a Base64 inline no XML
- ✅ Captura de eventos keydown/keyup com modificadores

### Validação e Tolerância
- ✅ Comparação de imagens com tolerância percentual configurável
- ✅ Múltiplas estratégias: RMS, histograma, similaridade por pixel
- ✅ Sistema de retry com backoff exponencial
- ✅ Políticas configuráveis: abort, skip, continue
- ✅ Timeouts configuráveis por ação

### Ruído Controlado
- ✅ Incerteza configurável (0-5%) para coordenadas
- ✅ Deslocamentos aleatórios pequenos para simular tolerância humana
- ✅ Aplicado apenas após validação visual bem-sucedida

### Robustez
- ✅ Logging detalhado com rotação de arquivos
- ✅ Injeção de dependência para testabilidade
- ✅ Validação XSD para XML
- ✅ Tratamento de erros e corner cases
- ✅ Suporte a multi-monitor e DPI

## 🚀 Como Compilar

### Pré-requisitos
- Java 21 ou superior
- Biblioteca JNativeHook (jnativehook-2.2.2.jar)

### Compilação
```bash
# Compilar todas as classes
javac -cp "lib/jnativehook-2.2.2.jar" -d bin src/main/*.java interfaces/*.java impl/*.java util/*.java

# Criar JAR executável
jar cfe autogui.jar main.MapeadorAtividades -C bin .
```

### Execução
```bash
# Executar aplicação
java -cp "lib/jnativehook-2.2.2.jar:autogui.jar" main.MapeadorAtividades

# Ou com JAR
java -cp "lib/jnativehook-2.2.2.jar" -jar autogui.jar
```

## 📋 Uso

### Interface Principal
1. **Iniciar Gravação** - Captura eventos de mouse e teclado
2. **Parar Gravação** - Finaliza captura e calcula delays
3. **Exportar XML** - Salva sessão no formato XML com validação
4. **Carregar XML** - Importa sessão salva anteriormente
5. **Reproduzir** - Executa ações com validação e retry

### Configurações Avançadas
- **Tolerância de Validação**: 50-100% (padrão: 95%)
- **Incerteza (Ruído)**: 0-5% (padrão: 1.5%)
- **Política de Retry**: abort/skip/continue (padrão: abort)
- **Tempo Máximo de Espera**: 1-300 segundos (padrão: 60s)
- **Captura de Dados Visuais**: Habilitar/desabilitar screenshots

## 📄 Formato XML

### Estrutura da Sessão
```xml
<Session id="20250909T201500Z" app="AutoGUI" version="1.0">
  <Meta>
    <Author>yycarvalho</Author>
    <UncertaintyPct>1.5</UncertaintyPct>
    <ValidationTolerancePct>95</ValidationTolerancePct>
    <RetryPolicy>abort</RetryPolicy>
  </Meta>
  <Actions>
    <Action id="1" type="MOUSE_CLICK" timestamp="2025-09-09T20:15:02.000Z">
      <Window title="Nome da Janela" pid="1234"/>
      <Coords absoluteX="100" absoluteY="200" relativeX="50" relativeY="25"/>
      <PixelSample width="21" height="21">BASE64PNG_DATA</PixelSample>
      <Screenshot path="screens/1.png" base64="BASE64PNG_DATA"/>
      <Validation tolerancePct="95" maxWaitMs="60000"/>
      <Notes>ESQUERDO_1</Notes>
    </Action>
  </Actions>
</Session>
```

### Validação XSD
O sistema inclui validação XSD automática. O schema está em `autogui-schema.xsd`.

## 🧪 Testes

### Testes Unitários
```bash
# Compilar testes (requer JUnit 5)
javac -cp "lib/jnativehook-2.2.2.jar:lib/junit-platform-console-standalone-1.9.2.jar" -d bin test/*.java

# Executar testes
java -jar lib/junit-platform-console-standalone-1.9.2.jar --class-path bin --scan-classpath
```

### Testes de Integração
1. Gravar uma sessão simples (ex: abrir bloco de notas, digitar texto)
2. Exportar para XML
3. Importar XML
4. Reproduzir ações
5. Verificar logs de validação

## 📊 Logs e Telemetria

### Estrutura de Logs
- **Logs Gerais**: `logs/autogui_*.log` (rotação automática)
- **Logs de Sessão**: `logs/session_YYYYMMDD_HHMMSS.log`
- **Níveis**: DEBUG, INFO, WARN, ERROR

### Informações Capturadas
- ID da sessão e operação (gravação/reprodução)
- Ação atual e resultado da validação
- Tentativas de retry e backoff
- Erros e exceções com contexto
- Timestamps precisos

## 🔧 Configuração Avançada

### Variáveis de Ambiente
```bash
export AUTOGUI_LOG_LEVEL=INFO
export AUTOGUI_LOG_DIR=./logs
export AUTOGUI_EXPORT_DIR=./exports
```

### Propriedades do Sistema
```bash
java -Dautogui.tolerance=95.0 -Dautogui.uncertainty=1.5 -jar autogui.jar
```

## 🐛 Troubleshooting

### Problemas Comuns

1. **Erro de permissão JNativeHook**
   - Executar como administrador (Windows)
   - Verificar permissões de acessibilidade (macOS/Linux)

2. **Validação falhando constantemente**
   - Ajustar tolerância de validação
   - Verificar se a janela está ativa
   - Considerar usar política "skip" ou "continue"

3. **Performance lenta**
   - Desabilitar captura de dados visuais
   - Reduzir tamanho dos pixel samples
   - Ajustar intervalos de retry

### Logs de Debug
```bash
# Ativar logs detalhados
java -Djava.util.logging.config.file=logging.properties -jar autogui.jar
```

## 🔄 Changelog

### v2.0.0 (Atual)
- ✅ Arquitetura modular com 5 camadas
- ✅ Validação visual com múltiplas estratégias
- ✅ Sistema de retry com backoff exponencial
- ✅ Ruído controlado para coordenadas
- ✅ XML Schema com validação XSD
- ✅ Logging robusto com rotação
- ✅ Injeção de dependência para testes
- ✅ Interface de configuração avançada

### v1.0.0 (Original)
- Captura básica de mouse e teclado
- Exportação/importação XML simples
- Reprodução sequencial

## 🤝 Contribuição

1. Fork o repositório
2. Crie uma branch para sua feature
3. Implemente testes unitários
4. Siga as convenções de código
5. Submeta um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

## 👨‍💻 Autor

**yycarvalho** - Desenvolvedor principal

## 🔮 Próximos Passos

- [ ] Algoritmos de visão computacional mais avançados (ORB, SIFT)
- [ ] Suporte a reconhecimento de texto (OCR)
- [ ] Interface web para execução remota
- [ ] Integração com CI/CD
- [ ] Suporte a múltiplas linguagens
- [ ] Plugin system para extensões

---

**⚠️ Aviso de Segurança**: Este sistema controla mouse e teclado globalmente. Use com responsabilidade e apenas em ambientes controlados. Não execute em sistemas de produção sem supervisão adequada.