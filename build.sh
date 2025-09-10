#!/bin/bash

# Script de compilação e execução do AutoGUI
# Uso: ./build.sh [compile|run|test|clean]

set -e

# Configurações
JAVA_VERSION="21"
JNATIVEHOOK_JAR="lib/jnativehook-2.2.2.jar"
MAIN_CLASS="main.MapeadorAtividades"
JAR_NAME="autogui.jar"
BUILD_DIR="bin"
SRC_DIR="src/main"
INTERFACES_DIR="interfaces"
IMPL_DIR="impl"
UTIL_DIR="util"
TEST_DIR="test"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir mensagens coloridas
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se Java 21 está disponível
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java não encontrado. Instale Java 21 ou superior."
        exit 1
    fi
    
    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VER" -lt 21 ]; then
        print_error "Java 21 ou superior é necessário. Versão atual: $JAVA_VER"
        exit 1
    fi
    
    print_success "Java $JAVA_VER encontrado"
}

# Verificar dependências
check_dependencies() {
    if [ ! -f "$JNATIVEHOOK_JAR" ]; then
        print_error "JNativeHook JAR não encontrado: $JNATIVEHOOK_JAR"
        print_info "Baixe de: https://github.com/kwhat/jnativehook/releases"
        exit 1
    fi
    
    print_success "Dependências encontradas"
}

# Criar diretórios necessários
create_directories() {
    mkdir -p "$BUILD_DIR"
    mkdir -p "logs"
    mkdir -p "exports"
    print_info "Diretórios criados"
}

# Compilar código
compile() {
    print_info "Compilando AutoGUI..."
    
    # Limpar build anterior
    rm -rf "$BUILD_DIR"/*
    
    # Compilar classes principais
    javac -cp "$JNATIVEHOOK_JAR" -d "$BUILD_DIR" \
        "$SRC_DIR"/*.java \
        "$INTERFACES_DIR"/*.java \
        "$IMPL_DIR"/*.java \
        "$UTIL_DIR"/*.java
    
    if [ $? -eq 0 ]; then
        print_success "Compilação concluída"
    else
        print_error "Falha na compilação"
        exit 1
    fi
}

# Criar JAR
create_jar() {
    print_info "Criando JAR executável..."
    
    # Criar manifest
    cat > MANIFEST.MF << EOF
Manifest-Version: 1.0
Main-Class: $MAIN_CLASS
Class-Path: $JNATIVEHOOK_JAR
EOF
    
    # Criar JAR
    jar cfe "$JAR_NAME" "$MAIN_CLASS" -C "$BUILD_DIR" . -m MANIFEST.MF
    
    if [ $? -eq 0 ]; then
        print_success "JAR criado: $JAR_NAME"
        rm -f MANIFEST.MF
    else
        print_error "Falha ao criar JAR"
        exit 1
    fi
}

# Executar aplicação
run() {
    print_info "Executando AutoGUI..."
    
    if [ ! -f "$JAR_NAME" ]; then
        print_warning "JAR não encontrado, compilando primeiro..."
        compile
        create_jar
    fi
    
    java -cp "$JNATIVEHOOK_JAR" -jar "$JAR_NAME"
}

# Executar testes
run_tests() {
    print_info "Executando testes..."
    
    # Verificar se JUnit está disponível
    if [ ! -f "lib/junit-platform-console-standalone-1.9.2.jar" ]; then
        print_warning "JUnit não encontrado, baixando..."
        mkdir -p lib
        curl -L -o lib/junit-platform-console-standalone-1.9.2.jar \
            "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.2/junit-platform-console-standalone-1.9.2.jar"
    fi
    
    # Compilar testes
    javac -cp "$JNATIVEHOOK_JAR:lib/junit-platform-console-standalone-1.9.2.jar" \
        -d "$BUILD_DIR" "$TEST_DIR"/*.java
    
    # Executar testes
    java -jar lib/junit-platform-console-standalone-1.9.2.jar \
        --class-path "$BUILD_DIR" \
        --scan-classpath
}

# Limpar arquivos de build
clean() {
    print_info "Limpando arquivos de build..."
    rm -rf "$BUILD_DIR"
    rm -f "$JAR_NAME"
    rm -f MANIFEST.MF
    print_success "Limpeza concluída"
}

# Mostrar ajuda
show_help() {
    echo "AutoGUI Build Script"
    echo ""
    echo "Uso: $0 [comando]"
    echo ""
    echo "Comandos:"
    echo "  compile  - Compilar o código"
    echo "  run      - Executar a aplicação"
    echo "  test     - Executar testes unitários"
    echo "  clean    - Limpar arquivos de build"
    echo "  help     - Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 compile && $0 run"
    echo "  $0 test"
    echo "  $0 clean"
}

# Função principal
main() {
    case "${1:-help}" in
        "compile")
            check_java
            check_dependencies
            create_directories
            compile
            create_jar
            ;;
        "run")
            check_java
            check_dependencies
            run
            ;;
        "test")
            check_java
            check_dependencies
            create_directories
            run_tests
            ;;
        "clean")
            clean
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# Executar função principal
main "$@"