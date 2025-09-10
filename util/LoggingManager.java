package main.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Gerenciador de logging com rotação de arquivos e níveis configuráveis
 */
public class LoggingManager {
    
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "autogui";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 5;
    
    private static Logger logger;
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;
    
    static {
        setupLogging();
    }
    
    private static void setupLogging() {
        try {
            // Criar diretório de logs se não existir
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // Configurar logger principal
            logger = Logger.getLogger("AutoGUI");
            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(false);
            
            // Handler para arquivo com rotação
            String logFileName = LOG_DIR + "/" + LOG_FILE_PREFIX + "_%g.log";
            fileHandler = new FileHandler(logFileName, MAX_FILE_SIZE, MAX_FILES, true);
            fileHandler.setFormatter(new DetailedFormatter());
            fileHandler.setLevel(Level.ALL);
            
            // Handler para console
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.WARNING);
            
            // Adicionar handlers
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            
            // Logger para sessões específicas
            setupSessionLogging();
            
        } catch (IOException e) {
            System.err.println("Erro ao configurar logging: " + e.getMessage());
        }
    }
    
    private static void setupSessionLogging() {
        // Logger específico para sessões de gravação/reprodução
        Logger sessionLogger = Logger.getLogger("AutoGUI.Session");
        sessionLogger.setLevel(Level.INFO);
        sessionLogger.setUseParentHandlers(false);
        
        try {
            String sessionLogFile = LOG_DIR + "/session_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
            FileHandler sessionHandler = new FileHandler(sessionLogFile, false);
            sessionHandler.setFormatter(new SessionFormatter());
            sessionLogger.addHandler(sessionHandler);
        } catch (IOException e) {
            System.err.println("Erro ao configurar session logging: " + e.getMessage());
        }
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static Logger getSessionLogger() {
        return Logger.getLogger("AutoGUI.Session");
    }
    
    public static void setLogLevel(Level level) {
        logger.setLevel(level);
        consoleHandler.setLevel(level);
    }
    
    public static void logSessionStart(String sessionId, String operation) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.info(String.format("SESSION_START|%s|%s|%s", 
            sessionId, operation, LocalDateTime.now()));
    }
    
    public static void logSessionEnd(String sessionId, String operation, boolean success) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.info(String.format("SESSION_END|%s|%s|%s|%s", 
            sessionId, operation, success ? "SUCCESS" : "FAILED", LocalDateTime.now()));
    }
    
    public static void logAction(String sessionId, String operation, Acao acao, String result) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.info(String.format("ACTION|%s|%s|%d|%s|%s|%s", 
            sessionId, operation, acao.getId(), acao.getTipo(), result, LocalDateTime.now()));
    }
    
    public static void logValidation(String sessionId, int actionId, boolean success, String details) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.info(String.format("VALIDATION|%s|%d|%s|%s|%s", 
            sessionId, actionId, success ? "PASS" : "FAIL", details, LocalDateTime.now()));
    }
    
    public static void logRetry(String sessionId, int actionId, int attempt, String reason) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.warning(String.format("RETRY|%s|%d|%d|%s|%s", 
            sessionId, actionId, attempt, reason, LocalDateTime.now()));
    }
    
    public static void logError(String sessionId, String operation, String error, Exception e) {
        Logger sessionLogger = getSessionLogger();
        sessionLogger.severe(String.format("ERROR|%s|%s|%s|%s|%s", 
            sessionId, operation, error, e.getMessage(), LocalDateTime.now()));
    }
    
    /**
     * Formatter detalhado para logs de arquivo
     */
    private static class DetailedFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("[%s] %s: %s%n",
                record.getLevel(),
                record.getLoggerName(),
                record.getMessage());
        }
    }
    
    /**
     * Formatter específico para logs de sessão
     */
    private static class SessionFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("%s|%s|%s%n",
                record.getLevel(),
                record.getMessage(),
                LocalDateTime.now());
        }
    }
}