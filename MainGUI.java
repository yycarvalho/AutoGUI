import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Interface gráfica principal do sistema de gravação e reprodução
 */
public class MainGUI extends JFrame {
    
    private ActionRecorder recorder;
    
    // Componentes da interface
    private JButton startRecordButton;
    private JButton stopRecordButton;
    private JButton playButton;
    private JButton exportButton;
    private JButton importButton;
    private JButton clearButton;
    private JButton viewLogsButton;
    
    private JLabel statusLabel;
    private JLabel actionCountLabel;
    private JProgressBar progressBar;
    
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    
    private JCheckBox enableValidationCheckBox;
    private JSlider toleranceSlider;
    private JLabel toleranceLabel;
    
    public MainGUI(ActionRecorder recorder) {
        this.recorder = recorder;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateUI();
    }
    
    private void initializeComponents() {
        // Botões principais
        startRecordButton = new JButton("Iniciar Gravação");
        stopRecordButton = new JButton("Parar Gravação");
        playButton = new JButton("Reproduzir");
        exportButton = new JButton("Exportar XML");
        importButton = new JButton("Importar XML");
        clearButton = new JButton("Limpar Ações");
        viewLogsButton = new JButton("Ver Logs");
        
        // Labels de status
        statusLabel = new JLabel("Sistema pronto");
        actionCountLabel = new JLabel("Ações gravadas: 0");
        
        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Aguardando...");
        
        // Área de logs
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Configurações
        enableValidationCheckBox = new JCheckBox("Habilitar validação visual", true);
        toleranceSlider = new JSlider(50, 100, 95);
        toleranceLabel = new JLabel("Tolerância: 95%");
        
        // Configurar cores dos botões
        startRecordButton.setBackground(new Color(76, 175, 80));
        startRecordButton.setForeground(Color.WHITE);
        stopRecordButton.setBackground(new Color(244, 67, 54));
        stopRecordButton.setForeground(Color.WHITE);
        playButton.setBackground(new Color(33, 150, 243));
        playButton.setForeground(Color.WHITE);
    }
    
    private void setupLayout() {
        setTitle("Sistema de Gravação e Reprodução de Ações");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel superior - Controles principais
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Painel central - Logs
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        // Painel inferior - Status e configurações
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Configurar janela
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Controles Principais"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Primeira linha - Botões de gravação
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(startRecordButton, gbc);
        
        gbc.gridx = 1;
        panel.add(stopRecordButton, gbc);
        
        gbc.gridx = 2;
        panel.add(playButton, gbc);
        
        // Segunda linha - Botões de arquivo
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(exportButton, gbc);
        
        gbc.gridx = 1;
        panel.add(importButton, gbc);
        
        gbc.gridx = 2;
        panel.add(clearButton, gbc);
        
        gbc.gridx = 3;
        panel.add(viewLogsButton, gbc);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Logs do Sistema"));
        panel.add(logScrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Status e Configurações"));
        
        // Painel de status
        JPanel statusSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusSubPanel.add(statusLabel);
        statusSubPanel.add(Box.createHorizontalStrut(20));
        statusSubPanel.add(actionCountLabel);
        statusSubPanel.add(Box.createHorizontalStrut(20));
        statusSubPanel.add(progressBar);
        
        // Painel de configurações
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.add(enableValidationCheckBox);
        configPanel.add(Box.createHorizontalStrut(20));
        configPanel.add(new JLabel("Tolerância:"));
        configPanel.add(toleranceSlider);
        configPanel.add(toleranceLabel);
        
        panel.add(statusSubPanel, BorderLayout.NORTH);
        panel.add(configPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        startRecordButton.addActionListener(e -> {
            recorder.startRecording();
            updateUI();
        });
        
        stopRecordButton.addActionListener(e -> {
            recorder.stopRecording();
            updateUI();
        });
        
        playButton.addActionListener(e -> {
            recorder.playRecording();
            updateUI();
        });
        
        exportButton.addActionListener(e -> exportToXML());
        
        importButton.addActionListener(e -> importFromXML());
        
        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Tem certeza que deseja limpar todas as ações gravadas?", 
                "Confirmar Limpeza", 
                JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            recorder.clearActions();
            updateUI();
        }
        });
        
        viewLogsButton.addActionListener(e -> showLogsDialog());
        
        toleranceSlider.addChangeListener(e -> {
            int value = toleranceSlider.getValue();
            toleranceLabel.setText("Tolerância: " + value + "%");
        });
        
        // Timer para atualizar logs em tempo real
        Timer logTimer = new Timer(1000, e -> updateLogArea());
        logTimer.start();
    }
    
    private void exportToXML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Ações para XML");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos XML", "xml"));
        fileChooser.setSelectedFile(new File("actions_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xml"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            recorder.exportToXML(filePath);
            addLog("Ações exportadas para: " + filePath);
        }
    }
    
    private void importFromXML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importar Ações de XML");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos XML", "xml"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            recorder.importFromXML(filePath);
            updateUI();
            addLog("Ações importadas de: " + filePath);
        }
    }
    
    private void showLogsDialog() {
        JDialog logDialog = new JDialog(this, "Logs do Sistema", true);
        logDialog.setSize(600, 400);
        logDialog.setLocationRelativeTo(this);
        
        JTextArea logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Preencher com logs
        for (LoggerModule.LogEntry entry : recorder.getLogger().getLogEntries()) {
            logTextArea.append(String.format("[%s] [%s] %s\n", 
                entry.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                entry.getLevel(),
                entry.getMessage()));
        }
        
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        logDialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> logDialog.dispose());
        buttonPanel.add(closeButton);
        
        JButton exportLogButton = new JButton("Exportar Logs");
        exportLogButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exportar Logs");
            fileChooser.setSelectedFile(new File("logs_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt"));
            
            if (fileChooser.showSaveDialog(logDialog) == JFileChooser.APPROVE_OPTION) {
                recorder.getLogger().exportLogs(fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(logDialog, "Logs exportados com sucesso!");
            }
        });
        buttonPanel.add(exportLogButton);
        
        logDialog.add(buttonPanel, BorderLayout.SOUTH);
        logDialog.setVisible(true);
    }
    
    public void updateRecordingState(boolean isRecording) {
        startRecordButton.setEnabled(!isRecording);
        stopRecordButton.setEnabled(isRecording);
        playButton.setEnabled(!isRecording && !recorder.isPlaying());
        
        if (isRecording) {
            statusLabel.setText("Gravando...");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText("Sistema pronto");
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    public void updatePlaybackState(boolean isPlaying) {
        playButton.setEnabled(!isPlaying);
        startRecordButton.setEnabled(!isPlaying);
        
        if (isPlaying) {
            statusLabel.setText("Reproduzindo...");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("Sistema pronto");
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    public void updateUI() {
        java.util.List<RecordedAction> actions = recorder.getRecordedActions();
        actionCountLabel.setText("Ações gravadas: " + actions.size());
        
        updateRecordingState(recorder.isRecording());
        updatePlaybackState(recorder.isPlaying());
    }
    
    private void updateLogArea() {
        // Atualizar área de logs com as últimas entradas
        LoggerModule.LogStatistics stats = recorder.getLogger().getStatistics();
        if (stats.getTotalCount() > 0) {
            // Mostrar apenas as últimas 50 entradas para performance
            List<LoggerModule.LogEntry> recentEntries = recorder.getLogger().getLogEntries(LoggerModule.LogLevel.DEBUG)
                .stream()
                .skip(Math.max(0, stats.getTotalCount() - 50))
                .collect(java.util.stream.Collectors.toList());
            
            StringBuilder logText = new StringBuilder();
            for (LoggerModule.LogEntry entry : recentEntries) {
                logText.append(String.format("[%s] [%s] %s\n", 
                    entry.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                    entry.getLevel(),
                    entry.getMessage()));
            }
            
            logArea.setText(logText.toString());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    
    private void addLog(String message) {
        recorder.getLogger().log(message);
    }
}