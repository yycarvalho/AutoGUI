package main;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class GerenciadorXML {
    
    public static void exportarParaXML(List<Acao> acoes, String nomeArquivo) throws Exception {
        // Escrita em streaming para suportar arquivos grandes sem esgotar memória
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(nomeArquivo)))) {
            XMLStreamWriter w = xof.createXMLStreamWriter(new OutputStreamWriter(bos, StandardCharsets.UTF_8));
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("mapa");
            w.writeAttribute("versao", "2.0");
            for (Acao acao : acoes) {
                w.writeStartElement("acao");
                w.writeAttribute("id", String.valueOf(acao.getId()));
                w.writeAttribute("tipo", acao.getTipo().name());
                w.writeAttribute("detalhes", acao.getDetalhes());
                w.writeAttribute("x", String.valueOf(acao.getX()));
                w.writeAttribute("y", String.valueOf(acao.getY()));
                w.writeAttribute("timestamp", acao.getTimestampFormatted());
                w.writeAttribute("delay", String.valueOf(acao.getDelay()));
                
                // Atributos de verificação (v1.0 compat)
                w.writeAttribute("verificarElemento", String.valueOf(acao.isVerificarElemento()));
                if (acao.getTipoVerificacao() != null) {
                    w.writeAttribute("tipoVerificacao", acao.getTipoVerificacao().name());
                }
                if (acao.getValorEsperado() != null) {
                    w.writeAttribute("valorEsperado", acao.getValorEsperado());
                }
                w.writeAttribute("timeoutVerificacao", String.valueOf(acao.getTimeoutVerificacao()));
                
                // Novos campos v2.0
                if (acao.getScreenshotBase64() != null && !acao.getScreenshotBase64().isEmpty()) {
                    w.writeStartElement("verificacaoVisual");
                    w.writeAttribute("width", String.valueOf(acao.getVerificacaoWidth()));
                    w.writeAttribute("height", String.valueOf(acao.getVerificacaoHeight()));
                    w.writeAttribute("tolerancia", String.valueOf(acao.getToleranciaComparacao()));
                    w.writeCData(acao.getScreenshotBase64());
                    w.writeEndElement();
                }
                
                w.writeEndElement();
            }
            w.writeEndElement(); // mapa
            w.writeEndDocument();
            w.flush();
            w.close();
        }
    }
    
    public static List<Acao> importarDeXML(String nomeArquivo) throws Exception {
        List<Acao> acoes = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(nomeArquivo));
        
        NodeList nodeList = document.getElementsByTagName("acao");
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                
                int id = Integer.parseInt(element.getAttribute("id"));
                Acao.TipoAcao tipo = Acao.TipoAcao.valueOf(element.getAttribute("tipo"));
                String detalhes = element.getAttribute("detalhes");
                int x = Integer.parseInt(element.getAttribute("x"));
                int y = Integer.parseInt(element.getAttribute("y"));
                long delay = Long.parseLong(element.getAttribute("delay"));
                
                Acao acao = new Acao(id, tipo, detalhes, x, y);
                acao.setDelay(delay);
                
                // Carregar configurações de verificação
                boolean verificarElemento = Boolean.parseBoolean(element.getAttribute("verificarElemento"));
                acao.setVerificarElemento(verificarElemento);
                
                if (verificarElemento) {
                    String tipoVerificacaoStr = element.getAttribute("tipoVerificacao");
                    if (tipoVerificacaoStr != null && !tipoVerificacaoStr.isEmpty()) {
                        try {
                            acao.setTipoVerificacao(VerificadorElementos.TipoVerificacao.valueOf(tipoVerificacaoStr));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Tipo de verificação inválido: " + tipoVerificacaoStr);
                        }
                    }
                    
                    String valorEsperado = element.getAttribute("valorEsperado");
                    if (valorEsperado != null && !valorEsperado.isEmpty()) {
                        acao.setValorEsperado(valorEsperado);
                    }
                    
                    String timeoutAttr = element.getAttribute("timeoutVerificacao");
                    if (timeoutAttr != null && !timeoutAttr.isEmpty()) {
                        int timeoutVerificacao = Integer.parseInt(timeoutAttr);
                        acao.setTimeoutVerificacao(timeoutVerificacao);
                    }
                    
                    // v2.0 - Nó de verificação visual opcional
                    NodeList visuais = element.getElementsByTagName("verificacaoVisual");
                    if (visuais != null && visuais.getLength() > 0) {
                        Element visual = (Element) visuais.item(0);
                        String wAttr = visual.getAttribute("width");
                        String hAttr = visual.getAttribute("height");
                        String tolAttr = visual.getAttribute("tolerancia");
                        if (wAttr != null && !wAttr.isEmpty()) acao.setVerificacaoWidth(Integer.parseInt(wAttr));
                        if (hAttr != null && !hAttr.isEmpty()) acao.setVerificacaoHeight(Integer.parseInt(hAttr));
                        if (tolAttr != null && !tolAttr.isEmpty()) acao.setToleranciaComparacao(Integer.parseInt(tolAttr));
                        
                        String base64 = visual.getTextContent();
                        if (base64 != null && !base64.trim().isEmpty()) {
                            acao.setScreenshotBase64(base64.trim());
                        }
                    }
                }
                
                String timestampStr = element.getAttribute("timestamp");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
                acao.setTimestamp(timestamp);
                
                acoes.add(acao);
            }
        }
        
        return acoes;
    }
}