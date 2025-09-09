import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * Sistema de Gravação e Reprodução de Ações de Mouse e Teclado
 * 
 * Este sistema permite capturar, armazenar e reproduzir ações do usuário
 * com validação visual e tratamento robusto de erros.
 * 
 * @author Sistema de Automação
 * @version 1.0
 */
public class ActionRecorder {
    
    // Configurações do sistema
    private static final int VALIDATION_RADIUS = 10; // Raio para captura de cores ao redor do clique
    private static final int MAX_WAIT_TIME = 60000; // 60 segundos para aguardar elementos
    private static final double COLOR_TOLERANCE = 0.95; // 95% de precisão para validação de cores
    private static final double UNCERTAINTY_FACTOR = 0.02; // 2% de incerteza
    
    // Componentes principais
    private Robot robot;
    private EventCaptureModule eventCapture;
    private VisualAnalysisModule visualAnalysis;
    private XMLPersistenceModule xmlPersistence;
    private PlaybackModule playbackModule;
    private LoggerModule logger;
    private MainGUI gui;
    
    // Estado do sistema
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private List<RecordedAction> recordedActions = new ArrayList<>();
    
    public ActionRecorder() {
        try {
            initializeComponents();
        } catch (Exception e) {
            System.err.println("Erro ao inicializar o sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() throws Exception {
        this.robot = new Robot();
        this.logger = new LoggerModule();
        this.visualAnalysis = new VisualAnalysisModule(robot, logger);
        this.xmlPersistence = new XMLPersistenceModule(logger);
        this.eventCapture = new EventCaptureModule(robot, visualAnalysis, logger);
        this.playbackModule = new PlaybackModule(robot, visualAnalysis, logger);
        this.gui = new MainGUI(this);
        
        logger.log("Sistema inicializado com sucesso");
    }
    
    public void startRecording() {
        if (isRecording) {
            logger.log("Gravação já está em andamento", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        if (isPlaying) {
            logger.log("Não é possível gravar durante a reprodução", LoggerModule.LogLevel.ERROR);
            return;
        }
        
        recordedActions.clear();
        isRecording = true;
        eventCapture.startCapture(recordedActions);
        gui.updateRecordingState(true);
        logger.log("Gravação iniciada");
    }
    
    public void stopRecording() {
        if (!isRecording) {
            logger.log("Nenhuma gravação em andamento", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        isRecording = false;
        eventCapture.stopCapture();
        gui.updateRecordingState(false);
        logger.log("Gravação finalizada. Total de ações: " + recordedActions.size());
    }
    
    public void playRecording() {
        if (isRecording) {
            logger.log("Não é possível reproduzir durante a gravação", LoggerModule.LogLevel.ERROR);
            return;
        }
        
        if (isPlaying) {
            logger.log("Reprodução já está em andamento", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        if (recordedActions.isEmpty()) {
            logger.log("Nenhuma ação gravada para reproduzir", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        isPlaying = true;
        gui.updatePlaybackState(true);
        
        new Thread(() -> {
            try {
                playbackModule.playActions(recordedActions);
            } catch (Exception e) {
                logger.log("Erro durante reprodução: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
            } finally {
                isPlaying = false;
                gui.updatePlaybackState(false);
            }
        }).start();
        
        logger.log("Reprodução iniciada");
    }
    
    public void exportToXML(String filePath) {
        try {
            xmlPersistence.exportActions(recordedActions, filePath);
            logger.log("Ações exportadas para: " + filePath);
        } catch (Exception e) {
            logger.log("Erro ao exportar: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    public void importFromXML(String filePath) {
        try {
            recordedActions = xmlPersistence.importActions(filePath);
            logger.log("Ações importadas de: " + filePath + " (" + recordedActions.size() + " ações)");
        } catch (Exception e) {
            logger.log("Erro ao importar: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
        }
    }
    
    public void clearActions() {
        if (isRecording) {
            logger.log("Não é possível limpar ações durante a gravação", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        if (isPlaying) {
            logger.log("Não é possível limpar ações durante a reprodução", LoggerModule.LogLevel.WARNING);
            return;
        }
        
        recordedActions.clear();
        logger.log("Ações limpas");
    }
    
    public void showGUI() {
        gui.setVisible(true);
    }
    
    public static void main(String[] args) {
        try {
            ActionRecorder recorder = new ActionRecorder();
            recorder.showGUI();
        } catch (Exception e) {
            System.err.println("Falha crítica ao iniciar o sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters para acesso aos módulos
    public boolean isRecording() { return isRecording; }
    public boolean isPlaying() { return isPlaying; }
    public List<RecordedAction> getRecordedActions() { return new ArrayList<>(recordedActions); }
    public LoggerModule getLogger() { return logger; }
}