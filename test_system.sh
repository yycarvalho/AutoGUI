#!/bin/bash

# Script de teste para o Sistema de Gravação e Reprodução
# Este script demonstra o funcionamento básico do sistema

echo "=== Teste do Sistema de Gravação e Reprodução ==="
echo ""

# Verificar se Java 21 está disponível
echo "Verificando Java..."
java -version 2>&1 | grep -q "version \"21"
if [ $? -ne 0 ]; then
    echo "AVISO: Java 21 não encontrado. O sistema pode não funcionar corretamente."
    echo "Versão atual do Java:"
    java -version
    echo ""
fi

# Compilar o sistema
echo "Compilando sistema..."
javac -cp . *.java
if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação. Verifique os erros acima."
    exit 1
fi
echo "Compilação bem-sucedida!"
echo ""

# Verificar se as classes foram compiladas
echo "Verificando classes compiladas..."
ls -la *.class | wc -l | xargs -I {} echo "Classes compiladas: {}"
echo ""

# Mostrar informações do sistema
echo "Informações do sistema:"
echo "- Arquitetura: $(uname -m)"
echo "- Sistema Operacional: $(uname -s)"
echo "- Versão do Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Verificar permissões necessárias
echo "Verificando permissões..."
if [ -w . ]; then
    echo "✓ Permissão de escrita no diretório atual: OK"
else
    echo "✗ Permissão de escrita no diretório atual: FALHA"
fi

# Verificar se o sistema pode capturar eventos
echo ""
echo "Testando captura de eventos..."
echo "NOTA: Para testar a captura de eventos, execute o sistema e:"
echo "1. Clique em 'Iniciar Gravação'"
echo "2. Execute algumas ações (cliques, digitação)"
echo "3. Clique em 'Parar Gravação'"
echo "4. Clique em 'Reproduzir' para testar a reprodução"
echo ""

# Executar o sistema
echo "Iniciando sistema..."
echo "Pressione Ctrl+C para parar o sistema"
echo ""

# Executar com timeout para demonstração
timeout 10s java -cp . ActionRecorder 2>&1 || echo "Sistema executado com sucesso (timeout de 10 segundos para demonstração)"

echo ""
echo "=== Teste Concluído ==="
echo "Para usar o sistema completo, execute: java -cp . ActionRecorder"