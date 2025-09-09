#!/bin/bash

# Script para compilar e executar o Sistema de Gravação e Reprodução de Ações
# Java 21 é necessário

echo "=== Sistema de Gravação e Reprodução de Ações ==="
echo "Compilando projeto..."

# Compilar todas as classes Java
javac -cp . *.java

if [ $? -eq 0 ]; then
    echo "Compilação bem-sucedida!"
    echo ""
    echo "Iniciando aplicação..."
    echo "Nota: O sistema requer permissões para capturar eventos de mouse e teclado."
    echo "Em alguns sistemas, pode ser necessário executar com privilégios elevados."
    echo ""
    
    # Executar a aplicação
    java -cp . ActionRecorder
else
    echo "Erro na compilação. Verifique se o Java 21 está instalado e configurado."
    exit 1
fi