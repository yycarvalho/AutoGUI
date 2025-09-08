package main;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class GerenciadorXML {
    
    public static void exportarParaXML(List<Acao> acoes, String nomeArquivo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        
        Element root = document.createElement("mapa");
        document.appendChild(root);
        
        for (Acao acao : acoes) {
            Element acaoElement = document.createElement("acao");
            
            acaoElement.setAttribute("id", String.valueOf(acao.getId()));
            acaoElement.setAttribute("tipo", acao.getTipo().name());
            acaoElement.setAttribute("detalhes", acao.getDetalhes());
            acaoElement.setAttribute("x", String.valueOf(acao.getX()));
            acaoElement.setAttribute("y", String.valueOf(acao.getY()));
            acaoElement.setAttribute("timestamp", acao.getTimestampFormatted());
            acaoElement.setAttribute("delay", String.valueOf(acao.getDelay()));
            
            root.appendChild(acaoElement);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(nomeArquivo));
        transformer.transform(source, result);
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