#!/bin/bash

echo "=== Testando Sistema de Verificação de Elementos ==="
echo

# Compilar o sistema
echo "1. Compilando o sistema..."
javac -cp "lib/jnativehook-2.2.2.jar" *.java
if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
else
    echo "❌ Erro na compilação!"
    exit 1
fi

echo

# Executar exemplo de uso
echo "2. Executando exemplo de uso..."
java -cp ".:lib/jnativehook-2.2.2.jar" ExemploUso
if [ $? -eq 0 ]; then
    echo "✅ Exemplo executado com sucesso!"
else
    echo "❌ Erro na execução do exemplo!"
fi

echo

# Executar interface principal
echo "3. Iniciando interface principal..."
echo "   (Pressione Ctrl+C para sair)"
java -cp ".:lib/jnativehook-2.2.2.jar" MapeadorAtividades