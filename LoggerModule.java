import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Módulo de logging para o sistema de gravação e reprodução
 */
public class LoggerModule {
    
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    private static final String LOG_FILE = "action_recorder.log";
    private static final int MAX_LOG_ENTRIES = 10000;
    
    private ConcurrentLinkedQueue<LogEntry> logEntries;
    private PrintWriter logWriter;
    private boolean enableFileLogging;
    private boolean enableConsoleLogging;
    private LogLevel minLogLevel;
    
    public LoggerModule() {
        this.logEntries = new ConcurrentLinkedQueue<>();
        this.enableFileLogging = true;
        this.enableConsoleLogging = true;
        this.minLogLevel = LogLevel.INFO;
        
        try {
            this.logWriter = new PrintWriter(new FileWriter(LOG_FILE, true));
        } catch (IOException e) {
            System.err.println("Erro ao inicializar arquivo de log: " + e.getMessage());
            this.enableFileLogging = false;
        }
    }
    
    /**
     * Registra uma mensagem de log
     */
    public void log(String message) {
        log(message, LogLevel.INFO);
    }
    
    /**
     * Registra uma mensagem de log com nível específico
     */
    public void log(String message, LogLevel level) {
        if (level.ordinal() < minLogLevel.ordinal()) {
            return; // Ignorar logs abaixo do nível mínimo
        }
        
        LogEntry entry = new LogEntry(LocalDateTime.now(), level, message);
        logEntries.offer(entry);
        
        // Manter apenas as últimas entradas
        while (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.poll();
        }
        
        // Log para console
        if (enableConsoleLogging) {
            String consoleMessage = formatLogMessage(entry);
            if (level == LogLevel.ERROR) {
                System.err.println(consoleMessage);
            } else {
                System.out.println(consoleMessage);
            }
        }
        
        // Log para arquivo
        if (enableFileLogging && logWriter != null) {
            try {
                logWriter.println(formatLogMessage(entry));
                logWriter.flush();
            } catch (Exception e) {
                System.err.println("Erro ao escrever no arquivo de log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Formata uma mensagem de log
     */
    private String formatLogMessage(LogEntry entry) {
        String timestamp = entry.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        return String.format("[%s] [%s] %s", timestamp, entry.getLevel(), entry.getMessage());
    }
    
    /**
     * Obtém todas as entradas de log
     */
    public ConcurrentLinkedQueue<LogEntry> getLogEntries() {
        return new ConcurrentLinkedQueue<>(logEntries);
    }
    
    /**
     * Obtém entradas de log filtradas por nível
     */
    public ConcurrentLinkedQueue<LogEntry> getLogEntries(LogLevel minLevel) {
        ConcurrentLinkedQueue<LogEntry> filtered = new ConcurrentLinkedQueue<>();
        for (LogEntry entry : logEntries) {
            if (entry.getLevel().ordinal() >= minLevel.ordinal()) {
                filtered.offer(entry);
            }
        }
        return filtered;
    }
    
    /**
     * Limpa todas as entradas de log
     */
    public void clearLogs() {
        logEntries.clear();
        log("Logs limpos", LogLevel.INFO);
    }
    
    /**
     * Exporta logs para arquivo
     */
    public void exportLogs(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (LogEntry entry : logEntries) {
                writer.println(formatLogMessage(entry));
            }
            log("Logs exportados para: " + filePath);
        } catch (IOException e) {
            log("Erro ao exportar logs: " + e.getMessage(), LogLevel.ERROR);
        }
    }
    
    /**
     * Configura o nível mínimo de log
     */
    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level;
        log("Nível mínimo de log alterado para: " + level);
    }
    
    /**
     * Habilita/desabilita logging para console
     */
    public void setConsoleLogging(boolean enable) {
        this.enableConsoleLogging = enable;
        log("Logging para console " + (enable ? "habilitado" : "desabilitado"));
    }
    
    /**
     * Habilita/desabilita logging para arquivo
     */
    public void setFileLogging(boolean enable) {
        this.enableFileLogging = enable;
        log("Logging para arquivo " + (enable ? "habilitado" : "desabilitado"));
    }
    
    /**
     * Obtém estatísticas de log
     */
    public LogStatistics getStatistics() {
        int debugCount = 0, infoCount = 0, warningCount = 0, errorCount = 0;
        
        for (LogEntry entry : logEntries) {
            switch (entry.getLevel()) {
                case DEBUG: debugCount++; break;
                case INFO: infoCount++; break;
                case WARNING: warningCount++; break;
                case ERROR: errorCount++; break;
            }
        }
        
        return new LogStatistics(debugCount, infoCount, warningCount, errorCount);
    }
    
    /**
     * Fecha o logger e libera recursos
     */
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
    
    /**
     * Classe para representar uma entrada de log
     */
    public static class LogEntry {
        private LocalDateTime timestamp;
        private LogLevel level;
        private String message;
        
        public LogEntry(LocalDateTime timestamp, LogLevel level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public LogLevel getLevel() { return level; }
        public String getMessage() { return message; }
    }
    
    /**
     * Classe para estatísticas de log
     */
    public static class LogStatistics {
        private int debugCount, infoCount, warningCount, errorCount;
        
        public LogStatistics(int debugCount, int infoCount, int warningCount, int errorCount) {
            this.debugCount = debugCount;
            this.infoCount = infoCount;
            this.warningCount = warningCount;
            this.errorCount = errorCount;
        }
        
        public int getDebugCount() { return debugCount; }
        public int getInfoCount() { return infoCount; }
        public int getWarningCount() { return warningCount; }
        public int getErrorCount() { return errorCount; }
        public int getTotalCount() { return debugCount + infoCount + warningCount + errorCount; }
        
        @Override
        public String toString() {
            return String.format("Logs - Debug: %d, Info: %d, Warning: %d, Error: %d, Total: %d",
                               debugCount, infoCount, warningCount, errorCount, getTotalCount());
        }
    }
}