package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class MapeadorAtividades extends JFrame implements 
    CapturadorEventos.EventoListener, ReprodutorEventos.ReprodutorListener {
    
    private CapturadorEventos capturador;
    private ReprodutorEventos reprodutor;
    
    // Componentes da interface
    private JButton btnIniciarGravacao;
    private JButton btnPararGravacao;
    private JButton btnExportarXML;
    private JButton btnCarregarXML;
    private JButton btnReproduzir;
    private JButton btnPararReproducao;
    
    private JTextArea areaLog;
    private JScrollPane scrollLog;
    private JLabel lblStatus;
    private JLabel lblContadorAcoes;
    private JProgressBar progressBar;
    
    private boolean gravando = false;
    private boolean reproduzindo = false;
    private List<Acao> acoesCarregadas;
    
    public MapeadorAtividades() {
        initComponents();
        initEventos();
    }
    
    private void initComponents() {
        setTitle("MapeadorAtividades - Sistema de Captura e Reprodução");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // ===== PAINEL DE CONTROLES =====
        JPanel painelControles = new JPanel(new GridBagLayout());
        painelControles.setBorder(new TitledBorder("Controles"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Botões de gravação
        btnIniciarGravacao = new JButton("🔴 Iniciar Gravação");
        btnIniciarGravacao.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 0; gbc.gridy = 0;
        painelControles.add(btnIniciarGravacao, gbc);
        
        btnPararGravacao = new JButton("⏹️ Parar Gravação");
        btnPararGravacao.setPreferredSize(new Dimension(150, 30));
        btnPararGravacao.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 0;
        painelControles.add(btnPararGravacao, gbc);
        
        btnExportarXML = new JButton("💾 Exportar XML");
        btnExportarXML.setPreferredSize(new Dimension(150, 30));
        btnExportarXML.setEnabled(false);
        gbc.gridx = 2; gbc.gridy = 0;
        painelControles.add(btnExportarXML, gbc);
        
        // Botões de reprodução
        btnCarregarXML = new JButton("📁 Carregar XML");
        btnCarregarXML.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 0; gbc.gridy = 1;
        painelControles.add(btnCarregarXML, gbc);
        
        btnReproduzir = new JButton("▶️ Reproduzir");
        btnReproduzir.setPreferredSize(new Dimension(150, 30));
        btnReproduzir.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 1;
        painelControles.add(btnReproduzir, gbc);
        
        btnPararReproducao = new JButton("⏸️ Parar Reprodução");
        btnPararReproducao.setPreferredSize(new Dimension(150, 30));
        btnPararReproducao.setEnabled(false);
        gbc.gridx = 2; gbc.gridy = 1;
        painelControles.add(btnPararReproducao, gbc);
        
        add(painelControles, BorderLayout.NORTH);
        
        // ===== PAINEL DE STATUS =====
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelStatus.setBorder(new TitledBorder("Status"));
        
        lblStatus = new JLabel("Pronto para iniciar");
        lblContadorAcoes = new JLabel("Ações capturadas: 0");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setVisible(false);
        
        painelStatus.add(lblStatus);
        painelStatus.add(Box.createHorizontalStrut(20));
        painelStatus.add(lblContadorAcoes);
        painelStatus.add(Box.createHorizontalStrut(20));
        painelStatus.add(progressBar);
        
        add(painelStatus, BorderLayout.CENTER);
        
        // ===== ÁREA DE LOG =====
        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBorder(new TitledBorder("Log de Atividades"));
        
        areaLog = new JTextArea(15, 50);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaLog.setEditable(false);
        scrollLog = new JScrollPane(areaLog);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        painelLog.add(scrollLog, BorderLayout.CENTER);
        add(painelLog, BorderLayout.SOUTH);
        
        // Configurações da janela
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Inicializar componentes
        try {
            capturador = new CapturadorEventos();
            capturador.setEventoListener(this);
            
            reprodutor = new ReprodutorEventos();
            reprodutor.setReprodutorListener(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao inicializar componentes: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initEventos() {
        // ===== EVENTOS DOS BOTÕES =====
        
        btnIniciarGravacao.addActionListener(e -> iniciarGravacao());
        
        btnPararGravacao.addActionListener(e -> pararGravacao());
        
        btnExportarXML.addActionListener(e -> exportarXML());
        
        btnCarregarXML.addActionListener(e -> carregarXML());
        
        btnReproduzir.addActionListener(e -> iniciarReproducao());
        
        btnPararReproducao.addActionListener(e -> pararReproducao());
        
        // Fechar aplicação adequadamente
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                encerrarAplicacao();
            }
        });
    }
    
    // ===== MÉTODOS DE CONTROLE =====
    
    private void iniciarGravacao() {
        try {
            capturador.iniciarCaptura();
            gravando = true;
            
            btnIniciarGravacao.setEnabled(false);
            btnPararGravacao.setEnabled(true);
            btnExportarXML.setEnabled(false);
            btnCarregarXML.setEnabled(false);
            btnReproduzir.setEnabled(false);
            
            lblStatus.setText("🔴 GRAVANDO...");
            lblContadorAcoes.setText("Ações capturadas: 0");
            areaLog.setText("");
            adicionarLog("=== INICIANDO GRAVAÇÃO ===");
            adicionarLog("Capturando eventos de mouse e teclado...");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao iniciar gravação: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void pararGravacao() {
        capturador.pararCaptura();
        gravando = false;
        
        btnIniciarGravacao.setEnabled(true);
        btnPararGravacao.setEnabled(false);
        btnExportarXML.setEnabled(true);
        btnCarregarXML.setEnabled(true);
        
        List<Acao> acoes = capturador.getAcoes();
        lblStatus.setText("⏹️ Gravação finalizada");
        lblContadorAcoes.setText("Ações capturadas: " + acoes.size());
        
        adicionarLog("=== GRAVAÇÃO FINALIZADA ===");
        adicionarLog("Total de ações capturadas: " + acoes.size());
        if (!acoes.isEmpty()) {
            adicionarLog("Primeira ação: " + acoes.get(0).toString());
            adicionarLog("Última ação: " + acoes.get(acoes.size()-1).toString());
        }
    }
    
    private void exportarXML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar mapeamento como XML");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Arquivos XML", "xml"));
        fileChooser.setSelectedFile(new File("mapeamento_" + 
            java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xml"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File arquivo = fileChooser.getSelectedFile();
                String nomeArquivo = arquivo.getAbsolutePath();
                if (!nomeArquivo.endsWith(".xml")) {
                    nomeArquivo += ".xml";
                }
                
                List<Acao> acoes = capturador.getAcoes();
                GerenciadorXML.exportarParaXML(acoes, nomeArquivo);
                
                adicionarLog("=== EXPORTAÇÃO CONCLUÍDA ===");
                adicionarLog("Arquivo salvo: " + nomeArquivo);
                adicionarLog("Total de ações exportadas: " + acoes.size());
                
                JOptionPane.showMessageDialog(this, 
                    "Arquivo XML salvo com sucesso!\n" + nomeArquivo, 
                    "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                adicionarLog("ERRO na exportação: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Erro ao exportar arquivo XML: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void carregarXML() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Carregar mapeamento XML");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Arquivos XML", "xml"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String nomeArquivo = fileChooser.getSelectedFile().getAbsolutePath();
                acoesCarregadas = GerenciadorXML.importarDeXML(nomeArquivo);
                
                btnReproduzir.setEnabled(true);
                
                adicionarLog("=== ARQUIVO CARREGADO ===");
                adicionarLog("Arquivo: " + nomeArquivo);
                adicionarLog("Total de ações carregadas: " + acoesCarregadas.size());
                
                if (!acoesCarregadas.isEmpty()) {
                    adicionarLog("Primeira ação: " + acoesCarregadas.get(0).toString());
                    adicionarLog("Última ação: " + acoesCarregadas.get(acoesCarregadas.size()-1).toString());
                }
                
                lblStatus.setText("📁 Arquivo XML carregado");
                lblContadorAcoes.setText("Ações carregadas: " + acoesCarregadas.size());
                
                JOptionPane.showMessageDialog(this, 
                    "Arquivo XML carregado com sucesso!\n" + 
                    "Ações disponíveis: " + acoesCarregadas.size(), 
                    "Carregamento Concluído", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                adicionarLog("ERRO no carregamento: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar arquivo XML: " + e.getMessage(), 
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void iniciarReproducao() {
        if (acoesCarregadas == null || acoesCarregadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nenhum arquivo XML foi carregado!", 
                "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int resposta = JOptionPane.showConfirmDialog(this,
            "Iniciar reprodução de " + acoesCarregadas.size() + " ações?\n" +
            "ATENÇÃO: O mouse e teclado serão controlados automaticamente!",
            "Confirmar Reprodução", JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
            
        if (resposta == JOptionPane.YES_OPTION) {
            reproduzindo = true;
            
            btnReproduzir.setEnabled(false);
            btnPararReproducao.setEnabled(true);
            btnIniciarGravacao.setEnabled(false);
            btnCarregarXML.setEnabled(false);
            btnExportarXML.setEnabled(false);
            
            lblStatus.setText("▶️ REPRODUZINDO...");
            progressBar.setVisible(true);
            progressBar.setValue(0);
            progressBar.setMaximum(acoesCarregadas.size());
            
            adicionarLog("=== INICIANDO REPRODUÇÃO ===");
            adicionarLog("Total de ações a reproduzir: " + acoesCarregadas.size());
            adicionarLog("ATENÇÃO: Não mova o mouse durante a reprodução!");
            
            // Dar tempo para o usuário se preparar
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(2000); // 2 segundos de preparação
                    reprodutor.reproduzirAcoes(acoesCarregadas);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    private void pararReproducao() {
        reprodutor.pararReproducao();
        reproduzindo = false;
        
        btnReproduzir.setEnabled(true);
        btnPararReproducao.setEnabled(false);
        btnIniciarGravacao.setEnabled(true);
        btnCarregarXML.setEnabled(true);
        btnExportarXML.setEnabled(!capturador.getAcoes().isEmpty());
        
        lblStatus.setText("⏸️ Reprodução interrompida");
        progressBar.setVisible(false);
        
        adicionarLog("=== REPRODUÇÃO INTERROMPIDA ===");
    }
    
    private void encerrarAplicacao() {
        try {
            if (capturador != null) {
                capturador.pararCaptura();
                capturador.limparCaptura();
            }
            if (reprodutor != null) {
                reprodutor.pararReproducao();
            }
        } catch (Exception e) {
            System.err.println("Erro ao encerrar aplicação: " + e.getMessage());
        }
        System.exit(0);
    }
    
    // ===== IMPLEMENTAÇÃO DOS LISTENERS =====
    
    @Override
    public void onNovoEvento(Acao acao) {
        SwingUtilities.invokeLater(() -> {
            List<Acao> acoes = capturador.getAcoes();
            lblContadorAcoes.setText("Ações capturadas: " + acoes.size());
            
            // Mostrar apenas as últimas 3 ações no log para não sobrecarregar
            //if (acoes.size() % 10 == 0 || acoes.size() <= 3) {
                adicionarLog(String.format("[%d] %s", acoes.size(), acao.toString()));
           // }
        });
    }
    
    @Override
    public void onAcaoExecutada(Acao acao, int progresso, int total) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progresso);
            progressBar.setString(String.format("Executando: %d/%d (%.1f%%)", 
                progresso, total, (progresso * 100.0) / total));
                
            if (progresso % 5 == 0 || progresso <= 3) {  // Log a cada 5 ações
                adicionarLog(String.format("[%d/%d] Executando: %s", 
                    progresso, total, acao.toString()));
            }
        });
    }
    
    @Override
    public void onReproducaoCompleta() {
        SwingUtilities.invokeLater(() -> {
            reproduzindo = false;
            
            btnReproduzir.setEnabled(true);
            btnPararReproducao.setEnabled(false);
            btnIniciarGravacao.setEnabled(true);
            btnCarregarXML.setEnabled(true);
            btnExportarXML.setEnabled(!capturador.getAcoes().isEmpty());
            
            lblStatus.setText("✅ Reprodução concluída");
            progressBar.setValue(progressBar.getMaximum());
            progressBar.setString("Concluído!");
            
            adicionarLog("=== REPRODUÇÃO CONCLUÍDA COM SUCESSO ===");
            
            // Ocultar progress bar após 3 segundos
            Timer timer = new Timer(3000, e -> progressBar.setVisible(false));
            timer.setRepeats(false);
            timer.start();
            
            JOptionPane.showMessageDialog(this, 
                "Reprodução concluída com sucesso!", 
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    @Override
    public void onErro(String erro) {
        SwingUtilities.invokeLater(() -> {
            reproduzindo = false;
            
            btnReproduzir.setEnabled(true);
            btnPararReproducao.setEnabled(false);
            btnIniciarGravacao.setEnabled(true);
            btnCarregarXML.setEnabled(true);
            btnExportarXML.setEnabled(!capturador.getAcoes().isEmpty());
            
            lblStatus.setText("❌ Erro na reprodução");
            progressBar.setVisible(false);
            
            adicionarLog("=== ERRO NA REPRODUÇÃO ===");
            adicionarLog("Erro: " + erro);
            
            JOptionPane.showMessageDialog(this, 
                "Erro durante a reprodução:\n" + erro, 
                "Erro", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            areaLog.append(String.format("[%s] %s%n", timestamp, mensagem));
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }
    
    // ===== MÉTODO MAIN =====
    
    public static void main(String[] args) {
        // Configurar Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Verificar dependências
//        try {
//            Class.forName("org.jnativehook.GlobalScreen");
//        } catch (ClassNotFoundException e) {
//            JOptionPane.showMessageDialog(null, 
//                "ERRO: Biblioteca JNativeHook não encontrada!\n\n" +
//                "Para usar este sistema, você precisa:\n" +
//                "1. Baixar o JNativeHook (versão 2.2.2 ou superior)\n" +
//                "2. Adicionar o JAR ao classpath\n\n" +
//                "Download: https://github.com/kwhat/jnativehook/releases", 
//                "Dependência Faltando", JOptionPane.ERROR_MESSAGE);
//            System.exit(1);
//        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                MapeadorAtividades app = new MapeadorAtividades();
                app.setVisible(true);
                
                // Mostrar instruções iniciais
                JOptionPane.showMessageDialog(app,
                    "🎯 MapeadorAtividades - Sistema de Captura e Reprodução\n\n" +
                    "📋 INSTRUÇÕES:\n" +
                    "1. Clique em 'Iniciar Gravação' para capturar ações\n" +
                    "2. Realize as ações desejadas com mouse e teclado\n" +
                    "3. Clique em 'Parar Gravação' quando terminar\n" +
                    "4. Use 'Exportar XML' para salvar o mapeamento\n" +
                    "5. Use 'Carregar XML' + 'Reproduzir' para executar\n\n" +
                    "⚠️ IMPORTANTE:\n" +
                    "• O sistema captura TODOS os eventos globalmente\n" +
                    "• Durante a reprodução, não interfira no mouse/teclado\n" +
                    "• Use ESC para interromper gravações se necessário",
                    "Bem-vindo ao MapeadorAtividades", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Erro ao inicializar aplicação: " + e.getMessage(), 
                    "Erro Fatal", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}