# Exemplo de Uso do Sistema de Gravação e Reprodução

## Executando o Sistema

### Método 1: Script Automático
```bash
./compile_and_run.sh
```

### Método 2: Compilação e Execução Manual
```bash
# Compilar
javac -cp . *.java

# Executar
java -cp . ActionRecorder
```

## Fluxo de Trabalho Típico

### 1. Iniciando o Sistema
- Execute o comando acima
- A interface gráfica será aberta automaticamente
- O sistema estará pronto para gravação

### 2. Gravando Ações
1. **Clique em "Iniciar Gravação"**
   - O botão ficará desabilitado
   - Status mudará para "Gravando..."
   - Contador de ações será zerado

2. **Execute suas ações normalmente**
   - Cliques do mouse
   - Movimentos do mouse
   - Digitação no teclado
   - Navegação entre janelas

3. **Clique em "Parar Gravação"**
   - O sistema parará de capturar eventos
   - Status voltará para "Sistema pronto"
   - Contador mostrará total de ações gravadas

### 3. Reproduzindo Ações
1. **Posicione o cursor** na área onde deseja reproduzir
2. **Clique em "Reproduzir"**
   - O sistema validará o contexto visual
   - Executará as ações na sequência gravada
   - Status mostrará "Reproduzindo..."

### 4. Gerenciando Arquivos
- **Exportar XML**: Salva as ações em arquivo XML portável
- **Importar XML**: Carrega ações de arquivo XML existente
- **Limpar Ações**: Remove todas as ações da memória

## Configurações Avançadas

### Validação Visual
- **Habilitar validação visual**: Ativa verificação de contexto antes de cada ação
- **Tolerância**: Ajusta a precisão da validação (50% a 100%)
  - 95% = Validação rigorosa (padrão)
  - 80% = Validação moderada
  - 50% = Validação permissiva

### Logs e Monitoramento
- **Ver Logs**: Visualiza logs detalhados do sistema
- **Exportar Logs**: Salva logs em arquivo de texto
- **Níveis de Log**: DEBUG, INFO, WARNING, ERROR

## Exemplos Práticos

### Exemplo 1: Automação de Login
1. Inicie a gravação
2. Clique no campo de usuário
3. Digite o nome de usuário
4. Clique no campo de senha
5. Digite a senha
6. Clique no botão "Entrar"
7. Pare a gravação
8. Exporte para XML
9. Use para reproduzir o login automaticamente

### Exemplo 2: Preenchimento de Formulário
1. Inicie a gravação
2. Navegue pelos campos do formulário
3. Preencha cada campo com dados
4. Clique em botões de navegação
5. Pare a gravação
6. Reproduza para preencher formulários similares

### Exemplo 3: Sequência de Navegação
1. Inicie a gravação
2. Navegue por menus e submenus
3. Clique em links e botões
4. Digite em campos de busca
5. Pare a gravação
6. Use para automatizar navegação repetitiva

## Dicas e Boas Práticas

### Para Melhor Precisão
- Aguarde elementos carregarem completamente antes de gravar
- Use validação visual para ambientes dinâmicos
- Ajuste a tolerância conforme necessário
- Teste a reprodução em ambiente similar

### Para Performance
- Evite gravar ações desnecessárias
- Use delays apropriados entre ações
- Limpe ações antigas regularmente
- Monitore logs para identificar problemas

### Para Confiabilidade
- Sempre teste a reprodução antes de usar em produção
- Mantenha backups dos arquivos XML importantes
- Use validação visual em ambientes críticos
- Monitore logs para detectar falhas

## Solução de Problemas

### Problema: "Contexto visual inválido"
**Solução**: 
- Ajuste a tolerância para um valor menor
- Verifique se a tela não mudou desde a gravação
- Aguarde elementos carregarem completamente

### Problema: "Timeout na validação"
**Solução**:
- Verifique se o elemento está visível
- Aumente o tempo de espera se necessário
- Desabilite validação visual temporariamente

### Problema: "Erro ao capturar eventos"
**Solução**:
- Execute com privilégios elevados
- Verifique permissões do sistema
- Reinicie a aplicação

### Problema: "Ações não reproduzem corretamente"
**Solução**:
- Verifique se o ambiente é similar ao da gravação
- Ajuste a tolerância de validação
- Verifique logs para detalhes do erro

## Formatos de Arquivo

### XML de Exportação
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ActionSequence version="1.0" totalActions="5" exportDate="2024-01-15T10:30:00">
  <Action index="0" type="MOUSE_CLICK" timestamp="2024-01-15T10:30:01">
    <Description>Clique do mouse em (100, 200) - Botão: 1, Contagem: 1</Description>
    <X>100</X>
    <Y>200</Y>
    <Button>1</Button>
    <ClickCount>1</ClickCount>
    <SurroundingColors>[123456, 789012, ...]</SurroundingColors>
    <Screenshot>iVBORw0KGgoAAAANSUhEUgAA...</Screenshot>
  </Action>
  <!-- Mais ações... -->
</ActionSequence>
```

### Logs do Sistema
```
[2024-01-15 10:30:01.123] [INFO] Sistema inicializado com sucesso
[2024-01-15 10:30:02.456] [INFO] Gravação iniciada
[2024-01-15 10:30:03.789] [INFO] Mouse click capturado: Clique do mouse em (100, 200)
[2024-01-15 10:30:04.012] [INFO] Tecla capturada: Tecla a pressionada
[2024-01-15 10:30:05.345] [INFO] Gravação finalizada. Total de ações: 3
```

## Integração com Outros Sistemas

O sistema pode ser integrado com outros aplicativos através de:
- Arquivos XML para troca de dados
- Logs para monitoramento
- Interface programática (modificando o código)
- Scripts de automação externos

## Limitações Conhecidas

- Requer ambiente desktop com Java AWT
- Alguns aplicativos podem bloquear captura de eventos
- Performance pode ser afetada em sistemas lentos
- Validação visual pode falhar em ambientes muito dinâmicos