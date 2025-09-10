package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog para configuração de parâmetros de verificação e reprodução
 */
public class ConfiguracaoVerificacaoDialog extends JDialog {
    
    private JSlider sliderTolerancia;
    private JSlider sliderIncerteza;
    private JComboBox<String> comboRetryPolicy;
    private JSpinner spinnerMaxWait;
    private JCheckBox checkCaptureVisual;
    private JTextField fieldExportDir;
    
    private boolean configuracaoAceita = false;
    
    // Valores padrão
    private double toleranciaPadrao = 95.0;
    private double incertezaPadrao = 1.5;
    private String retryPolicyPadrao = "abort";
    private int maxWaitPadrao = 60000;
    private boolean captureVisualPadrao = true;
    private String exportDirPadrao = "./exports";
    
    public ConfiguracaoVerificacaoDialog(JFrame parent) {
        super(parent, "Configurações de Verificação", true);
        initComponents();
        initEventos();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tolerância de validação
        gbc.gridx = 0; gbc.gridy = 0;
        painelPrincipal.add(new JLabel("Tolerância de Validação (%):"), gbc);
        
        sliderTolerancia = new JSlider(50, 100, (int)toleranciaPadrao);
        sliderTolerancia.setMajorTickSpacing(10);
        sliderTolerancia.setMinorTickSpacing(5);
        sliderTolerancia.setPaintTicks(true);
        sliderTolerancia.setPaintLabels(true);
        gbc.gridx = 1; gbc.gridy = 0;
        painelPrincipal.add(sliderTolerancia, gbc);
        
        JLabel lblToleranciaValor = new JLabel(String.valueOf(toleranciaPadrao) + "%");
        sliderTolerancia.addChangeListener(e -> 
            lblToleranciaValor.setText(sliderTolerancia.getValue() + "%"));
        gbc.gridx = 2; gbc.gridy = 0;
        painelPrincipal.add(lblToleranciaValor, gbc);
        
        // Incerteza (ruído controlado)
        gbc.gridx = 0; gbc.gridy = 1;
        painelPrincipal.add(new JLabel("Incerteza (ruído) (%):"), gbc);
        
        sliderIncerteza = new JSlider(0, 50, (int)(incertezaPadrao * 10));
        sliderIncerteza.setMajorTickSpacing(10);
        sliderIncerteza.setMinorTickSpacing(5);
        sliderIncerteza.setPaintTicks(true);
        sliderIncerteza.setPaintLabels(true);
        gbc.gridx = 1; gbc.gridy = 1;
        painelPrincipal.add(sliderIncerteza, gbc);
        
        JLabel lblIncertezaValor = new JLabel(String.valueOf(incertezaPadrao) + "%");
        sliderIncerteza.addChangeListener(e -> 
            lblIncertezaValor.setText(String.format("%.1f%%", sliderIncerteza.getValue() / 10.0)));
        gbc.gridx = 2; gbc.gridy = 1;
        painelPrincipal.add(lblIncertezaValor, gbc);
        
        // Política de retry
        gbc.gridx = 0; gbc.gridy = 2;
        painelPrincipal.add(new JLabel("Política de Retry:"), gbc);
        
        comboRetryPolicy = new JComboBox<>(new String[]{"abort", "skip", "continue"});
        comboRetryPolicy.setSelectedItem(retryPolicyPadrao);
        gbc.gridx = 1; gbc.gridy = 2;
        painelPrincipal.add(comboRetryPolicy, gbc);
        
        // Tempo máximo de espera
        gbc.gridx = 0; gbc.gridy = 3;
        painelPrincipal.add(new JLabel("Tempo Máximo de Espera (ms):"), gbc);
        
        spinnerMaxWait = new JSpinner(new SpinnerNumberModel(maxWaitPadrao, 1000, 300000, 1000));
        gbc.gridx = 1; gbc.gridy = 3;
        painelPrincipal.add(spinnerMaxWait, gbc);
        
        // Captura de dados visuais
        gbc.gridx = 0; gbc.gridy = 4;
        painelPrincipal.add(new JLabel("Capturar Dados Visuais:"), gbc);
        
        checkCaptureVisual = new JCheckBox("Habilitar captura de screenshots e pixel samples");
        checkCaptureVisual.setSelected(captureVisualPadrao);
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.gridwidth = 2;
        painelPrincipal.add(checkCaptureVisual, gbc);
        
        // Diretório de exportação
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 1;
        painelPrincipal.add(new JLabel("Diretório de Exportação:"), gbc);
        
        fieldExportDir = new JTextField(exportDirPadrao, 20);
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.gridwidth = 2;
        painelPrincipal.add(fieldExportDir, gbc);
        
        add(painelPrincipal, BorderLayout.CENTER);
        
        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        
        JButton btnOK = new JButton("OK");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnPadrao = new JButton("Restaurar Padrões");
        
        painelBotoes.add(btnOK);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnPadrao);
        
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Eventos dos botões
        btnOK.addActionListener(e -> {
            configuracaoAceita = true;
            dispose();
        });
        
        btnCancelar.addActionListener(e -> {
            configuracaoAceita = false;
            dispose();
        });
        
        btnPadrao.addActionListener(e -> restaurarPadroes());
    }
    
    private void initEventos() {
        // Adicionar tooltips explicativos
        sliderTolerancia.setToolTipText("Tolerância percentual para comparação de imagens (50-100%)");
        sliderIncerteza.setToolTipText("Porcentagem de ruído aleatório aplicado às coordenadas (0-5%)");
        comboRetryPolicy.setToolTipText("Política quando validação falha: abort=parar, skip=pular, continue=continuar");
        spinnerMaxWait.setToolTipText("Tempo máximo para aguardar elemento aparecer (1-300 segundos)");
        checkCaptureVisual.setToolTipText("Capturar screenshots e pixel samples durante gravação");
        fieldExportDir.setToolTipText("Diretório onde salvar arquivos exportados");
    }
    
    private void restaurarPadroes() {
        sliderTolerancia.setValue((int)toleranciaPadrao);
        sliderIncerteza.setValue((int)(incertezaPadrao * 10));
        comboRetryPolicy.setSelectedItem(retryPolicyPadrao);
        spinnerMaxWait.setValue(maxWaitPadrao);
        checkCaptureVisual.setSelected(captureVisualPadrao);
        fieldExportDir.setText(exportDirPadrao);
    }
    
    // Getters para os valores configurados
    public double getTolerancia() {
        return sliderTolerancia.getValue();
    }
    
    public double getIncerteza() {
        return sliderIncerteza.getValue() / 10.0;
    }
    
    public String getRetryPolicy() {
        return (String) comboRetryPolicy.getSelectedItem();
    }
    
    public int getMaxWait() {
        return (Integer) spinnerMaxWait.getValue();
    }
    
    public boolean isCaptureVisual() {
        return checkCaptureVisual.isSelected();
    }
    
    public String getExportDir() {
        return fieldExportDir.getText();
    }
    
    public boolean isConfiguracaoAceita() {
        return configuracaoAceita;
    }
    
    /**
     * Aplica as configurações a uma lista de ações
     */
    public void aplicarConfiguracoes(List<Acao> acoes) {
        for (Acao acao : acoes) {
            acao.setValidationTolerancePct(getTolerancia());
            acao.setUncertaintyPct(getIncerteza());
            acao.setRetryPolicy(getRetryPolicy());
            acao.setMaxWaitMs(getMaxWait());
        }
    }
    
    /**
     * Exibe o dialog e retorna true se as configurações foram aceitas
     */
    public static boolean mostrarDialog(JFrame parent) {
        ConfiguracaoVerificacaoDialog dialog = new ConfiguracaoVerificacaoDialog(parent);
        dialog.setVisible(true);
        return dialog.isConfiguracaoAceita();
    }
}
