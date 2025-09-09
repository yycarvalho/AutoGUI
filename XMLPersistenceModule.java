import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * Módulo responsável pela persistência e leitura de ações em formato XML
 */
public class XMLPersistenceModule {
    
    private LoggerModule logger;
    private static final String ENCODING = "UTF-8";
    private static final String ROOT_ELEMENT = "ActionSequence";
    private static final String ACTION_ELEMENT = "Action";
    private static final String METADATA_ELEMENT = "Metadata";
    
    public XMLPersistenceModule(LoggerModule logger) {
        this.logger = logger;
    }
    
    /**
     * Exporta uma lista de ações para um arquivo XML
     */
    public void exportActions(List<RecordedAction> actions, String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        // Elemento raiz
        Element rootElement = doc.createElement(ROOT_ELEMENT);
        rootElement.setAttribute("version", "1.0");
        rootElement.setAttribute("totalActions", String.valueOf(actions.size()));
        rootElement.setAttribute("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        doc.appendChild(rootElement);
        
        // Adicionar cada ação
        for (int i = 0; i < actions.size(); i++) {
            RecordedAction action = actions.get(i);
            Element actionElement = createActionElement(doc, action, i);
            rootElement.appendChild(actionElement);
        }
        
        // Escrever arquivo
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
        
        logger.log("Ações exportadas para XML: " + filePath);
    }
    
    /**
     * Importa ações de um arquivo XML
     */
    public List<RecordedAction> importActions(String filePath) throws Exception {
        List<RecordedAction> actions = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        
        NodeList actionNodes = doc.getElementsByTagName(ACTION_ELEMENT);
        
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Node actionNode = actionNodes.item(i);
            if (actionNode.getNodeType() == Node.ELEMENT_NODE) {
                RecordedAction action = parseActionElement((Element) actionNode);
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        
        logger.log("Ações importadas do XML: " + filePath + " (" + actions.size() + " ações)");
        return actions;
    }
    
    /**
     * Cria um elemento XML para uma ação
     */
    private Element createActionElement(Document doc, RecordedAction action, int index) {
        Element actionElement = doc.createElement(ACTION_ELEMENT);
        actionElement.setAttribute("index", String.valueOf(index));
        actionElement.setAttribute("type", action.getActionType());
        actionElement.setAttribute("timestamp", action.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Informações básicas
        addTextElement(doc, actionElement, "Description", action.getDescription());
        addTextElement(doc, actionElement, "WindowTitle", action.getWindowTitle());
        addTextElement(doc, actionElement, "WindowClass", action.getWindowClass());
        addTextElement(doc, actionElement, "WindowId", String.valueOf(action.getWindowId()));
        
        // Dados específicos por tipo de ação
        if (action instanceof MouseClickEvent) {
            createMouseClickElement(doc, actionElement, (MouseClickEvent) action);
        } else if (action instanceof MouseMoveEvent) {
            createMouseMoveElement(doc, actionElement, (MouseMoveEvent) action);
        } else if (action instanceof KeyboardEvent) {
            createKeyboardElement(doc, actionElement, (KeyboardEvent) action);
        } else if (action instanceof DelayEvent) {
            createDelayElement(doc, actionElement, (DelayEvent) action);
        }
        
        // Metadados
        if (!action.getMetadata().isEmpty()) {
            Element metadataElement = doc.createElement(METADATA_ELEMENT);
            for (Map.Entry<String, Object> entry : action.getMetadata().entrySet()) {
                addTextElement(doc, metadataElement, entry.getKey(), entry.getValue().toString());
            }
            actionElement.appendChild(metadataElement);
        }
        
        return actionElement;
    }
    
    private void createMouseClickElement(Document doc, Element parent, MouseClickEvent event) {
        addTextElement(doc, parent, "X", String.valueOf(event.getX()));
        addTextElement(doc, parent, "Y", String.valueOf(event.getY()));
        addTextElement(doc, parent, "Button", String.valueOf(event.getButton()));
        addTextElement(doc, parent, "ClickCount", String.valueOf(event.getClickCount()));
        
        // Cores ao redor
        if (event.getSurroundingColors() != null) {
            Element colorsElement = doc.createElement("SurroundingColors");
            String colorsString = Arrays.toString(event.getSurroundingColors());
            colorsElement.setTextContent(colorsString);
            parent.appendChild(colorsElement);
        }
        
        // Screenshot
        if (event.getScreenshotBase64() != null && !event.getScreenshotBase64().isEmpty()) {
            addTextElement(doc, parent, "Screenshot", event.getScreenshotBase64());
        }
    }
    
    private void createMouseMoveElement(Document doc, Element parent, MouseMoveEvent event) {
        addTextElement(doc, parent, "X", String.valueOf(event.getX()));
        addTextElement(doc, parent, "Y", String.valueOf(event.getY()));
        
        // Cores ao redor
        if (event.getSurroundingColors() != null) {
            Element colorsElement = doc.createElement("SurroundingColors");
            String colorsString = Arrays.toString(event.getSurroundingColors());
            colorsElement.setTextContent(colorsString);
            parent.appendChild(colorsElement);
        }
        
        // Screenshot
        if (event.getScreenshotBase64() != null && !event.getScreenshotBase64().isEmpty()) {
            addTextElement(doc, parent, "Screenshot", event.getScreenshotBase64());
        }
    }
    
    private void createKeyboardElement(Document doc, Element parent, KeyboardEvent event) {
        addTextElement(doc, parent, "KeyCode", String.valueOf(event.getKeyCode()));
        addTextElement(doc, parent, "KeyChar", String.valueOf(event.getKeyChar()));
        addTextElement(doc, parent, "IsKeyPressed", String.valueOf(event.isKeyPressed()));
        addTextElement(doc, parent, "KeyText", event.getKeyText());
        addTextElement(doc, parent, "KeyLocation", String.valueOf(event.getKeyLocation()));
    }
    
    private void createDelayElement(Document doc, Element parent, DelayEvent event) {
        addTextElement(doc, parent, "DelayMillis", String.valueOf(event.getDelayMillis()));
    }
    
    /**
     * Adiciona um elemento de texto ao documento
     */
    private void addTextElement(Document doc, Element parent, String tagName, String textContent) {
        if (textContent != null) {
            Element element = doc.createElement(tagName);
            element.setTextContent(textContent);
            parent.appendChild(element);
        }
    }
    
    /**
     * Analisa um elemento XML e cria o objeto ActionEvent correspondente
     */
    private RecordedAction parseActionElement(Element actionElement) {
        try {
            String actionType = actionElement.getAttribute("type");
            String timestampStr = actionElement.getAttribute("timestamp");
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            RecordedAction action = null;
            
            switch (actionType) {
                case "MOUSE_CLICK":
                    action = parseMouseClickEvent(actionElement);
                    break;
                case "MOUSE_MOVE":
                    action = parseMouseMoveEvent(actionElement);
                    break;
                case "KEYBOARD":
                    action = parseKeyboardEvent(actionElement);
                    break;
                case "DELAY":
                    action = parseDelayEvent(actionElement);
                    break;
                default:
                    logger.log("Tipo de ação desconhecido: " + actionType, LoggerModule.LogLevel.WARNING);
                    return null;
            }
            
            if (action != null) {
                action.setTimestamp(timestamp);
                parseCommonAttributes(actionElement, action);
            }
            
            return action;
            
        } catch (Exception e) {
            logger.log("Erro ao analisar elemento de ação: " + e.getMessage(), LoggerModule.LogLevel.ERROR);
            return null;
        }
    }
    
    private MouseClickEvent parseMouseClickEvent(Element element) {
        int x = Integer.parseInt(getElementText(element, "X"));
        int y = Integer.parseInt(getElementText(element, "Y"));
        int button = Integer.parseInt(getElementText(element, "Button"));
        int clickCount = Integer.parseInt(getElementText(element, "ClickCount"));
        
        MouseClickEvent event = new MouseClickEvent(x, y, button, clickCount);
        
        // Parsear cores ao redor
        String colorsStr = getElementText(element, "SurroundingColors");
        if (colorsStr != null && !colorsStr.isEmpty()) {
            // Remover colchetes e dividir por vírgulas
            colorsStr = colorsStr.substring(1, colorsStr.length() - 1);
            String[] colorStrings = colorsStr.split(", ");
            int[] colors = new int[colorStrings.length];
            for (int i = 0; i < colorStrings.length; i++) {
                colors[i] = Integer.parseInt(colorStrings[i].trim());
            }
            event.setSurroundingColors(colors);
        }
        
        // Parsear screenshot
        String screenshot = getElementText(element, "Screenshot");
        if (screenshot != null && !screenshot.isEmpty()) {
            event.setScreenshotBase64(screenshot);
        }
        
        return event;
    }
    
    private MouseMoveEvent parseMouseMoveEvent(Element element) {
        int x = Integer.parseInt(getElementText(element, "X"));
        int y = Integer.parseInt(getElementText(element, "Y"));
        
        MouseMoveEvent event = new MouseMoveEvent(x, y);
        
        // Parsear cores ao redor
        String colorsStr = getElementText(element, "SurroundingColors");
        if (colorsStr != null && !colorsStr.isEmpty()) {
            colorsStr = colorsStr.substring(1, colorsStr.length() - 1);
            String[] colorStrings = colorsStr.split(", ");
            int[] colors = new int[colorStrings.length];
            for (int i = 0; i < colorStrings.length; i++) {
                colors[i] = Integer.parseInt(colorStrings[i].trim());
            }
            event.setSurroundingColors(colors);
        }
        
        // Parsear screenshot
        String screenshot = getElementText(element, "Screenshot");
        if (screenshot != null && !screenshot.isEmpty()) {
            event.setScreenshotBase64(screenshot);
        }
        
        return event;
    }
    
    private KeyboardEvent parseKeyboardEvent(Element element) {
        int keyCode = Integer.parseInt(getElementText(element, "KeyCode"));
        char keyChar = getElementText(element, "KeyChar").charAt(0);
        boolean isKeyPressed = Boolean.parseBoolean(getElementText(element, "IsKeyPressed"));
        
        KeyboardEvent event = new KeyboardEvent(keyCode, keyChar, isKeyPressed);
        event.setKeyText(getElementText(element, "KeyText"));
        event.setKeyLocation(Long.parseLong(getElementText(element, "KeyLocation")));
        
        return event;
    }
    
    private DelayEvent parseDelayEvent(Element element) {
        long delayMillis = Long.parseLong(getElementText(element, "DelayMillis"));
        return new DelayEvent(delayMillis);
    }
    
    private void parseCommonAttributes(Element element, RecordedAction action) {
        action.setWindowTitle(getElementText(element, "WindowTitle"));
        action.setWindowClass(getElementText(element, "WindowClass"));
        action.setWindowId(Integer.parseInt(getElementText(element, "WindowId")));
        
        // Parsear metadados
        NodeList metadataNodes = element.getElementsByTagName(METADATA_ELEMENT);
        if (metadataNodes.getLength() > 0) {
            Element metadataElement = (Element) metadataNodes.item(0);
            NodeList metadataChildren = metadataElement.getChildNodes();
            
            Map<String, Object> metadata = new HashMap<>();
            for (int i = 0; i < metadataChildren.getLength(); i++) {
                Node child = metadataChildren.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) child;
                    metadata.put(childElement.getTagName(), childElement.getTextContent());
                }
            }
            action.setMetadata(metadata);
        }
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
}