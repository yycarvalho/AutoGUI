package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Diálogo para configuração das opções de verificação de elementos
 */
public class ConfiguracaoVerificacaoDialog extends JDialog {
    
    private ConfiguracaoVerificacao configuracao;
    private boolean configuracaoAceita = false;
    
    // Componentes da interface
    private JSpinner spinnerTimeout;
    private JSpinner spinnerIntervalo;
    private JSpinner spinnerToleranciaPosicao;
    private JSpinner spinnerToleranciaCor;
    private JSpinner spinnerLimiarTransparencia;
    private JSpinner spinnerLarguraTexto;
    private JSpinner spinnerAlturaTexto;
    private JSpinner spinnerLarguraImagem;
    private JSpinner spinnerAlturaImagem;
    private JCheckBox checkCaseSensitive;
    private JCheckBox checkCaracteresEspeciais;
    private JCheckBox checkLogDetalhado;
    
    public ConfiguracaoVerificacaoDialog(Frame parent, ConfiguracaoVerificacao configuracao) {
        super(parent, "Configurações de Verificação", true);
        this.configuracao = configuracao != null ? configuracao : new ConfiguracaoVerificacao();
        
        initComponents();
        carregarConfiguracao();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal com scroll
        JScrollPane scrollPane = new JScrollPane();
        JPanel painelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // ===== CONFIGURAÇÕES DE TIMEOUT =====
        JPanel painelTimeout = criarPainelTimeout();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        painelPrincipal.add(painelTimeout, gbc);
        
        // ===== CONFIGURAÇÕES DE TOLERÂNCIA =====
        JPanel painelTolerancia = criarPainelTolerancia();
        gbc.gridy = 1;
        painelPrincipal.add(painelTolerancia, gbc);
        
        // ===== CONFIGURAÇÕES DE CAPTURA =====
        JPanel painelCaptura = criarPainelCaptura();
        gbc.gridy = 2;
        painelPrincipal.add(painelCaptura, gbc);
        
        // ===== CONFIGURAÇÕES DE VALIDAÇÃO =====
        JPanel painelValidacao = criarPainelValidacao();
        gbc.gridy = 3;
        painelPrincipal.add(painelValidacao, gbc);
        
        scrollPane.setViewportView(painelPrincipal);
        add(scrollPane, BorderLayout.CENTER);
        
        // ===== BOTÕES =====
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnPadrao = new JButton("Restaurar Padrões");
        btnPadrao.addActionListener(e -> restaurarPadroes());
        painelBotoes.add(btnPadrao);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            configuracaoAceita = false;
            dispose();
        });
        painelBotoes.add(btnCancelar);
        
        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(e -> {
            salvarConfiguracao();
            configuracaoAceita = true;
            dispose();
        });
        painelBotoes.add(btnOK);
        
        add(painelBotoes, BorderLayout.SOUTH);
    }
    
    private JPanel criarPainelTimeout() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new TitledBorder("Configurações de Timeout"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Timeout padrão
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Timeout padrão (segundos):"), gbc);
        
        spinnerTimeout = new JSpinner(new SpinnerNumberModel(60, 1, 300, 1));
        spinnerTimeout.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerTimeout, gbc);
        
        // Intervalo de verificação
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Intervalo de verificação (ms):"), gbc);
        
        spinnerIntervalo = new JSpinner(new SpinnerNumberModel(500, 100, 5000, 100));
        spinnerIntervalo.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerIntervalo, gbc);
        
        return painel;
    }
    
    private JPanel criarPainelTolerancia() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new TitledBorder("Configurações de Tolerância"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tolerância de posição
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Tolerância de posição (pixels):"), gbc);
        
        spinnerToleranciaPosicao = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1));
        spinnerToleranciaPosicao.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerToleranciaPosicao, gbc);
        
        // Tolerância de cor
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Tolerância de cor (0-255):"), gbc);
        
        spinnerToleranciaCor = new JSpinner(new SpinnerNumberModel(10, 0, 255, 1));
        spinnerToleranciaCor.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerToleranciaCor, gbc);
        
        // Limiar de transparência
        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(new JLabel("Limiar de transparência (0-255):"), gbc);
        
        spinnerLimiarTransparencia = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
        spinnerLimiarTransparencia.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerLimiarTransparencia, gbc);
        
        return painel;
    }
    
    private JPanel criarPainelCaptura() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new TitledBorder("Configurações de Captura"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Largura captura texto
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Largura captura texto (px):"), gbc);
        
        spinnerLarguraTexto = new JSpinner(new SpinnerNumberModel(200, 50, 1000, 10));
        spinnerLarguraTexto.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerLarguraTexto, gbc);
        
        // Altura captura texto
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Altura captura texto (px):"), gbc);
        
        spinnerAlturaTexto = new JSpinner(new SpinnerNumberModel(50, 20, 500, 5));
        spinnerAlturaTexto.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerAlturaTexto, gbc);
        
        // Largura captura imagem
        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(new JLabel("Largura captura imagem (px):"), gbc);
        
        spinnerLarguraImagem = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));
        spinnerLarguraImagem.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerLarguraImagem, gbc);
        
        // Altura captura imagem
        gbc.gridx = 0; gbc.gridy = 3;
        painel.add(new JLabel("Altura captura imagem (px):"), gbc);
        
        spinnerAlturaImagem = new JSpinner(new SpinnerNumberModel(100, 20, 500, 10));
        spinnerAlturaImagem.setPreferredSize(new Dimension(80, 25));
        gbc.gridx = 1;
        painel.add(spinnerAlturaImagem, gbc);
        
        return painel;
    }
    
    private JPanel criarPainelValidacao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new TitledBorder("Configurações de Validação"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Case sensitive
        checkCaseSensitive = new JCheckBox("Case Sensitive (maiúsculas/minúsculas)");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        painel.add(checkCaseSensitive, gbc);
        
        // Caracteres especiais
        checkCaracteresEspeciais = new JCheckBox("Validar caracteres especiais");
        gbc.gridy = 1;
        painel.add(checkCaracteresEspeciais, gbc);
        
        // Log detalhado
        checkLogDetalhado = new JCheckBox("Log detalhado");
        gbc.gridy = 2;
        painel.add(checkLogDetalhado, gbc);
        
        return painel;
    }
    
    private void carregarConfiguracao() {
        spinnerTimeout.setValue(configuracao.getTimeoutPadrao());
        spinnerIntervalo.setValue(configuracao.getIntervaloVerificacaoMs());
        spinnerToleranciaPosicao.setValue(configuracao.getToleranciaPosicao());
        spinnerToleranciaCor.setValue(configuracao.getToleranciaCor());
        spinnerLimiarTransparencia.setValue(configuracao.getLimiarTransparencia());
        spinnerLarguraTexto.setValue(configuracao.getLarguraCapturaTexto());
        spinnerAlturaTexto.setValue(configuracao.getAlturaCapturaTexto());
        spinnerLarguraImagem.setValue(configuracao.getLarguraCapturaImagem());
        spinnerAlturaImagem.setValue(configuracao.getAlturaCapturaImagem());
        checkCaseSensitive.setSelected(configuracao.isCaseSensitive());
        checkCaracteresEspeciais.setSelected(configuracao.isValidarCaracteresEspeciais());
        checkLogDetalhado.setSelected(configuracao.isLogDetalhado());
    }
    
    private void salvarConfiguracao() {
        configuracao.setTimeoutPadrao((Integer) spinnerTimeout.getValue());
        configuracao.setIntervaloVerificacaoMs((Integer) spinnerIntervalo.getValue());
        configuracao.setToleranciaPosicao((Integer) spinnerToleranciaPosicao.getValue());
        configuracao.setToleranciaCor((Integer) spinnerToleranciaCor.getValue());
        configuracao.setLimiarTransparencia((Integer) spinnerLimiarTransparencia.getValue());
        configuracao.setLarguraCapturaTexto((Integer) spinnerLarguraTexto.getValue());
        configuracao.setAlturaCapturaTexto((Integer) spinnerAlturaTexto.getValue());
        configuracao.setLarguraCapturaImagem((Integer) spinnerLarguraImagem.getValue());
        configuracao.setAlturaCapturaImagem((Integer) spinnerAlturaImagem.getValue());
        configuracao.setCaseSensitive(checkCaseSensitive.isSelected());
        configuracao.setValidarCaracteresEspeciais(checkCaracteresEspeciais.isSelected());
        configuracao.setLogDetalhado(checkLogDetalhado.isSelected());
    }
    
    private void restaurarPadroes() {
        ConfiguracaoVerificacao padroes = new ConfiguracaoVerificacao();
        carregarConfiguracao();
    }
    
    public boolean isConfiguracaoAceita() {
        return configuracaoAceita;
    }
    
    public ConfiguracaoVerificacao getConfiguracao() {
        return configuracao;
    }
    
    public static ConfiguracaoVerificacao mostrarDialogo(Frame parent, ConfiguracaoVerificacao configuracao) {
        ConfiguracaoVerificacaoDialog dialog = new ConfiguracaoVerificacaoDialog(parent, configuracao);
        dialog.setVisible(true);
        
        if (dialog.isConfiguracaoAceita()) {
            return dialog.getConfiguracao();
        }
        
        return null;
    }
}
