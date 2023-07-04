package com.ysc.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

public class Main {


    public static void main(String[] args) {

        if (isZeroParameters(args)) return;
        d("=== start RESX transformation ===");
        String defaultInputFile = "ProjectServiceResources.en.resx";

        JsoupParsingService service = new JsoupParsingService();
        if (args.length > 0) {
            //ESCAPING
            if (isEscapeParameters(args)) {

                // ========================
                // ====== ESCAPING
                // ========================
                escapeParameters(args, service);

                return;
            } else if (isUnescapeParameters(args)) {

                // ========================
                // ===== UNESCAPING
                // ========================
                unescapeParameters(args, service);


            } else if (isAnalyseParameters(args)) {
                analyseParameters(args, defaultInputFile, service);
            }
            else if (isXlfParameters(args)) {
                xlfAnalyseParameters(args, service);
            } else if (args[0].equals("key")) {
                String key = args[1];
                String[] keys = args[1].split(",");

                Arrays.stream(keys).forEach(k -> {

                    service.analyseKey("ProjectServiceResources.en.resx", k);

                });
            }
        }
    }

    private static void xlfAnalyseParameters(String[] args, JsoupParsingService service) {
        String report = service.analyseMessage(args[1]);
        d(report);
    }

    private static boolean isXlfParameters(String[] args) {
        return args[0].equals("xlf");
    }

    private static void analyseParameters(String[] args, String defaultInputFile, JsoupParsingService service) {
        String inputForAnalyse = args.length < 2 || args[1] == null ? defaultInputFile : args[1];
        d(service.analyseShort(inputForAnalyse));
    }

    private static boolean isAnalyseParameters(String[] args) {
        return args[0].equals("analyse");
    }

    private static boolean isUnescapeParameters(String[] args) {
        return "unescape".equals(args[0]) || "u".equals(args[0]);
    }

    private static boolean isEscapeParameters(String[] args) {
        return "escape".equals(args[0]) || "e".equals(args[0]);
    }

    private static void unescapeParameters(String[] args, JsoupParsingService service) {
        String inputFile = "transformed_ProjectServiceResources.en.resx";

        //FILE GIVEN
        if (args.length > 1 && !"".equals(args[1].trim())) {
            inputFile = args[1].trim();
        }

        org.jsoup.nodes.Document document = service.unescapeHtmlInValues(inputFile);

        //Create new with "escaped_" prefix
        String newFile = renameFile("unescaped", inputFile);
        service.createOutputFile(document, newFile);
    }

    private static void escapeParameters(String[] args, JsoupParsingService service) {
        String inputFile = "transformed_ProjectServiceResources.en.resx";

        //FILE GIVEN
        if (args.length > 1 && !"".equals(args[1].trim())) {
            inputFile = args[1].trim();
        }

        org.jsoup.nodes.Document document = service.escapeContent(inputFile);

        //Create new with "escaped_" prefix
        String newFile = renameFile("escaped", inputFile);
        service.createOutputFile(document, newFile);
    }

    private static boolean isZeroParameters(String[] args) {
        if (args.length == 0 || args[1].endsWith("-h") || args[1].endsWith("-help")) {
            d("Use the following flags: \n\n escape + file - for file escaping. Ex.: java -jar ... escape filePah\n" +
                    " xlf - for xlf processing (in progress..)\n" +
                    " analyse - for summary. (ex.: java -jar .. analyse)");
            return true;
        }
        return false;
    }

    private static String renameFile(String prefix, String inputFile) {
        Path path = Paths.get(inputFile);
        Path directory = path.getParent();
        Path fileName = path.getFileName();

        String newFileName = prefix + "_" + fileName;
        if (directory == null) {
            return newFileName;
        }
        return directory.resolve(newFileName).toAbsolutePath().toString();

    }

    public static List<String> showFiles(String dir) {


        String[] array = {"txt"};
        Collection<File> files = FileUtils.listFiles(new File(dir), array, true);

        return files.stream()
                .map(f -> f.getAbsolutePath())
                .collect(Collectors.toList());


//        List<File> result = new ArrayList<>();
//        for (File file : files) {
//            if (file.isDirectory()) {
//                //System.out.println("Directory: " + file.getAbsolutePath());
//                showFiles(file.listFiles()); // Calls same method again.
//            } else {
//                //System.out.println("File: " + file.getAbsolutePath());
//                if (file.getAbsolutePath().endsWith(".txt")) {
//                    result.add(file);
//                }
//            }
//        }
//        return result;
    }

    private static Document updateFileUsingDocumentBuilder(String inputFileName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputFileName);
        document.createProcessingInstruction(StreamResult.PI_ENABLE_OUTPUT_ESCAPING, "");
        NodeList nodeListData = document.getElementsByTagName("value");
        for (int i = 0; i < nodeListData.getLength(); i++) {
            Node node = nodeListData.item(i);
            String text = node.getTextContent();
            if (isWellFormedHtml(text)) {
                deescapeHtml(document, node, text);
            }
        }
        return document;
    }

    private static void generateOutputFileUsingDOM(String fileName, Document document) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            OutputStream outputStream = new FileOutputStream(fileName);
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);
        } catch (IOException | TransformerException e) {
            System.err.println("Error writing output file: " + e.getMessage());
            return;
        }

    }

    private static void deescapeHtml(Document document, Node node, String text) {
        String textContent = StringEscapeUtils.unescapeHtml4(text);
        d("compared: " + text.compareTo(textContent));

        node.setTextContent("bla" + textContent);
    }

    private static void appendCdata(Document doc, Node node1) {
        CDATASection cdataSection = doc.createCDATASection(node1.getTextContent());
        node1.setTextContent("");
        node1.appendChild(cdataSection);
    }

    private static String docToString(org.w3c.dom.Document doc) {
        return Jsoup.parse(docToStringWithoutDocType(doc), "", Parser.xmlParser()).toString();
    }

    private static String docToStringWithoutDocType(org.w3c.dom.Document doc) {
        NodeList children = doc.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                sb.append(elementToString((Element) child));
            }
        }
        return sb.toString();
    }

    private static String elementToString(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(element.getNodeName());
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            sb.append(" ").append(attribute.getNodeName()).append("=\"").append(attribute.getNodeValue()).append("\"");
        }
        sb.append(">");
        sb.append(element.getTextContent());
        sb.append("</").append(element.getNodeName()).append(">");
        return sb.toString();
    }

    private static void d(String s) {
        System.out.println(s);
    }


    public static boolean isWellFormedHtml(String text) {
        org.jsoup.nodes.Document document = Jsoup.parseBodyFragment(text);
        Elements elements = document.body().children();
        if (elements.size() == 0) {
            return false;
        } else if (elements.size() == 1 && elements.first().nodeName().equals("#text")) {
            return false;
        }
        return true;
    }


}
