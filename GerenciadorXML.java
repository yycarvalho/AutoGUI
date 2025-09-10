package main;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.awt.Rectangle;

class GerenciadorXML {
    
    private static final Logger logger = Logger.getLogger(GerenciadorXML.class.getName());
    private static final String XSD_SCHEMA = "autogui-schema.xsd";
    
    /**
     * Classe para representar uma sessão completa
     */
    public static class Session {
        private String id;
        private String app = "AutoGUI";
        private String version = "1.0";
        private String author = "yycarvalho";
        private double uncertaintyPct = 1.5;
        private double validationTolerancePct = 95.0;
        private String retryPolicy = "abort";
        private List<Acao> actions = new ArrayList<>();
        
        public Session() {
            this.id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        }
        
        // Getters e Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getApp() { return app; }
        public void setApp(String app) { this.app = app; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public double getUncertaintyPct() { return uncertaintyPct; }
        public void setUncertaintyPct(double uncertaintyPct) { this.uncertaintyPct = uncertaintyPct; }
        public double getValidationTolerancePct() { return validationTolerancePct; }
        public void setValidationTolerancePct(double validationTolerancePct) { this.validationTolerancePct = validationTolerancePct; }
        public String getRetryPolicy() { return retryPolicy; }
        public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
        public List<Acao> getActions() { return actions; }
        public void setActions(List<Acao> actions) { this.actions = actions; }
    }
    
    public static void exportarParaXML(List<Acao> acoes, String nomeArquivo) throws Exception {
        Session session = new Session();
        session.setActions(acoes);
        exportSession(session, Paths.get(nomeArquivo));
    }
    
    public static void exportSession(Session session, Path outputPath) throws Exception {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath.toFile()))) {
            XMLStreamWriter w = xof.createXMLStreamWriter(new OutputStreamWriter(bos, StandardCharsets.UTF_8));
            
            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("Session");
            w.writeAttribute("id", session.getId());
            w.writeAttribute("app", session.getApp());
            w.writeAttribute("version", session.getVersion());
            
            // Meta informações
            w.writeStartElement("Meta");
            w.writeStartElement("Author");
            w.writeCharacters(session.getAuthor());
            w.writeEndElement();
            w.writeStartElement("UncertaintyPct");
            w.writeCharacters(String.valueOf(session.getUncertaintyPct()));
            w.writeEndElement();
            w.writeStartElement("ValidationTolerancePct");
            w.writeCharacters(String.valueOf(session.getValidationTolerancePct()));
            w.writeEndElement();
            w.writeStartElement("RetryPolicy");
            w.writeCharacters(session.getRetryPolicy());
            w.writeEndElement();
            w.writeEndElement(); // Meta
            
            // Ações
            w.writeStartElement("Actions");
            for (Acao acao : session.getActions()) {
                writeAction(w, acao);
            }
            w.writeEndElement(); // Actions
            
            w.writeEndElement(); // Session
            w.writeEndDocument();
            w.flush();
            w.close();
        }
    }
    
    private static void writeAction(XMLStreamWriter w, Acao acao) throws Exception {
        w.writeStartElement("Action");
        w.writeAttribute("id", String.valueOf(acao.getId()));
        w.writeAttribute("type", acao.getTipo().name());
        w.writeAttribute("timestamp", acao.getTimestampFormatted());
        
        // Janela
        w.writeStartElement("Window");
        w.writeAttribute("title", acao.getWindowTitle());
        w.writeAttribute("pid", String.valueOf(acao.getWindowPid()));
        w.writeEndElement();
        
        // Coordenadas
        w.writeStartElement("Coords");
        w.writeAttribute("absoluteX", String.valueOf(acao.getX()));
        w.writeAttribute("absoluteY", String.valueOf(acao.getY()));
        w.writeAttribute("relativeX", String.valueOf(acao.getRelativeX()));
        w.writeAttribute("relativeY", String.valueOf(acao.getRelativeY()));
        w.writeEndElement();
        
        // Pixel Sample
        if (acao.getPixelSampleBase64() != null && !acao.getPixelSampleBase64().isEmpty()) {
            w.writeStartElement("PixelSample");
            w.writeAttribute("width", String.valueOf(PIXEL_SAMPLE_SIZE));
            w.writeAttribute("height", String.valueOf(PIXEL_SAMPLE_SIZE));
            w.writeCharacters(acao.getPixelSampleBase64());
            w.writeEndElement();
        }
        
        // Screenshot
        if (acao.getScreenshotBase64() != null && !acao.getScreenshotBase64().isEmpty()) {
            w.writeStartElement("Screenshot");
            w.writeAttribute("path", "screens/" + acao.getId() + ".png");
            w.writeAttribute("base64", acao.getScreenshotBase64());
            w.writeEndElement();
        }
        
        // Validação
        w.writeStartElement("Validation");
        w.writeAttribute("tolerancePct", String.valueOf(acao.getValidationTolerancePct()));
        w.writeAttribute("maxWaitMs", String.valueOf(acao.getMaxWaitMs()));
        w.writeEndElement();
        
        // Notas
        w.writeStartElement("Notes");
        w.writeCharacters(acao.getDetalhes());
        w.writeEndElement();
        
        w.writeEndElement(); // Action
    }
    
    public static List<Acao> importarDeXML(String nomeArquivo) throws Exception {
        Session session = importSession(Paths.get(nomeArquivo));
        return session.getActions();
    }
    
    public static Session importSession(Path xmlPath) throws Exception {
        // Validar XML contra XSD se disponível
        validateXmlAgainstXsd(xmlPath, Paths.get(XSD_SCHEMA));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlPath.toFile());
        
        Session session = new Session();
        List<Acao> acoes = new ArrayList<>();
        
        // Ler metadados da sessão
        NodeList sessionNodes = document.getElementsByTagName("Session");
        if (sessionNodes.getLength() > 0) {
            Element sessionElement = (Element) sessionNodes.item(0);
            session.setId(sessionElement.getAttribute("id"));
            session.setApp(sessionElement.getAttribute("app"));
            session.setVersion(sessionElement.getAttribute("version"));
        }
        
        // Ler meta informações
        NodeList metaNodes = document.getElementsByTagName("Meta");
        if (metaNodes.getLength() > 0) {
            Element metaElement = (Element) metaNodes.item(0);
            
            NodeList authorNodes = metaElement.getElementsByTagName("Author");
            if (authorNodes.getLength() > 0) {
                session.setAuthor(authorNodes.item(0).getTextContent());
            }
            
            NodeList uncertaintyNodes = metaElement.getElementsByTagName("UncertaintyPct");
            if (uncertaintyNodes.getLength() > 0) {
                session.setUncertaintyPct(Double.parseDouble(uncertaintyNodes.item(0).getTextContent()));
            }
            
            NodeList toleranceNodes = metaElement.getElementsByTagName("ValidationTolerancePct");
            if (toleranceNodes.getLength() > 0) {
                session.setValidationTolerancePct(Double.parseDouble(toleranceNodes.item(0).getTextContent()));
            }
            
            NodeList retryNodes = metaElement.getElementsByTagName("RetryPolicy");
            if (retryNodes.getLength() > 0) {
                session.setRetryPolicy(retryNodes.item(0).getTextContent());
            }
        }
        
        // Ler ações
        NodeList actionNodes = document.getElementsByTagName("Action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element actionElement = (Element) actionNodes.item(i);
            Acao acao = parseAction(actionElement);
            acoes.add(acao);
        }
        
        session.setActions(acoes);
        return session;
    }
    
    private static Acao parseAction(Element actionElement) throws Exception {
        int id = Integer.parseInt(actionElement.getAttribute("id"));
        Acao.TipoAcao tipo = Acao.TipoAcao.valueOf(actionElement.getAttribute("type"));
        String timestampStr = actionElement.getAttribute("timestamp");
        
        // Coordenadas
        NodeList coordsNodes = actionElement.getElementsByTagName("Coords");
        int x = 0, y = 0, relativeX = 0, relativeY = 0;
        if (coordsNodes.getLength() > 0) {
            Element coordsElement = (Element) coordsNodes.item(0);
            x = Integer.parseInt(coordsElement.getAttribute("absoluteX"));
            y = Integer.parseInt(coordsElement.getAttribute("absoluteY"));
            relativeX = Integer.parseInt(coordsElement.getAttribute("relativeX"));
            relativeY = Integer.parseInt(coordsElement.getAttribute("relativeY"));
        }
        
        Acao acao = new Acao(id, tipo, "", x, y, relativeX, relativeY);
        
        // Timestamp
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        acao.setTimestamp(timestamp);
        
        // Janela
        NodeList windowNodes = actionElement.getElementsByTagName("Window");
        if (windowNodes.getLength() > 0) {
            Element windowElement = (Element) windowNodes.item(0);
            acao.setWindowTitle(windowElement.getAttribute("title"));
            acao.setWindowPid(Long.parseLong(windowElement.getAttribute("pid")));
        }
        
        // Pixel Sample
        NodeList pixelNodes = actionElement.getElementsByTagName("PixelSample");
        if (pixelNodes.getLength() > 0) {
            acao.setPixelSampleBase64(pixelNodes.item(0).getTextContent());
        }
        
        // Screenshot
        NodeList screenshotNodes = actionElement.getElementsByTagName("Screenshot");
        if (screenshotNodes.getLength() > 0) {
            Element screenshotElement = (Element) screenshotNodes.item(0);
            acao.setScreenshotPath(screenshotElement.getAttribute("path"));
            acao.setScreenshotBase64(screenshotElement.getAttribute("base64"));
        }
        
        // Validação
        NodeList validationNodes = actionElement.getElementsByTagName("Validation");
        if (validationNodes.getLength() > 0) {
            Element validationElement = (Element) validationNodes.item(0);
            acao.setValidationTolerancePct(Double.parseDouble(validationElement.getAttribute("tolerancePct")));
            acao.setMaxWaitMs(Long.parseLong(validationElement.getAttribute("maxWaitMs")));
        }
        
        // Notas/Detalhes
        NodeList notesNodes = actionElement.getElementsByTagName("Notes");
        if (notesNodes.getLength() > 0) {
            acao.setDetalhes(notesNodes.item(0).getTextContent());
        }
        
        return acao;
    }
    
    public static void validateXmlAgainstXsd(Path xmlPath, Path xsdPath) throws Exception {
        if (!xsdPath.toFile().exists()) {
            logger.warning("XSD schema não encontrado: " + xsdPath + " - pulando validação");
            return;
        }
        
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(xsdPath.toFile());
        Validator validator = schema.newValidator();
        validator.validate(new javax.xml.transform.stream.StreamSource(xmlPath.toFile()));
        logger.info("XML validado com sucesso contra XSD");
    }
    
    private static final int PIXEL_SAMPLE_SIZE = 21;
}