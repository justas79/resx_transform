package com.ysc.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class JsoupParsingService extends BaseParsingService implements ParsingService {
    public String parseFile(String inputFile) {
        return readXmlFile(inputFile);
    }

    public Document unescapeHtmlInValues(String fileName) {

        AtomicInteger unescapedItems = new AtomicInteger();
        String fileContent = parseFile(fileName);
        Document jsoupDocument = Jsoup.parse(fileContent, "", Parser.xmlParser());
        Elements dataElements = jsoupDocument.select("data");
        dataElements.forEach(d -> {
            Elements valueElement = d.select("value");
            String originalUnescapedText = ((Element) valueElement.get(0)).text();
            if (isWellFormedHtml(originalUnescapedText)) {
                valueElement.html(originalUnescapedText);
                unescapedItems.getAndIncrement();
            }
        });

        System.out.println("Unescaped " + unescapedItems + " items.");
        return jsoupDocument;
    }


    public String analyseTxtFile(String fileName) {
        File f = new File(fileName);
        String report = "";
        String fileContent = parseFile(fileName);
        int i = countWords(fileContent);
        report = report + report + ": word count: " + f.getName();
        return report;
    }


    public String analyseMessage(String fileName) {
        AtomicInteger totalWordsCount = new AtomicInteger(0);
        AtomicInteger countOfTotalValueElements = new AtomicInteger(0);
        String report = "";

        String fileContent = parseFile(fileName);
        Document jsoupDocument = Jsoup.parse(fileContent, "", Parser.xmlParser());
        Elements sourceElements = jsoupDocument.select("source");
        sourceElements.forEach(d -> {
            String sourceText = d.text();

            int i = countWords(sourceText);

            totalWordsCount.addAndGet(i);
            countOfTotalValueElements.getAndIncrement();
        });
        report = report + "Total keys : " + report;
        report = report + "\nTotal words : " + report;

        return report;
    }


    public String analyseShort(String fileName) {
        AtomicInteger countOfHtmlKeys = new AtomicInteger();
        AtomicInteger totalWordsCount = new AtomicInteger();
        AtomicInteger countOfTotalValueElements = new AtomicInteger();


        List<String> listOfKeys = new ArrayList<>();
        String fileContent = parseFile(fileName);
        Document jsoupDocument = Jsoup.parse(fileContent, "", Parser.xmlParser());
        Elements dataElements = jsoupDocument.select("data");
        dataElements.forEach(d -> {
            Elements valueElement = d.select("value");

            String originalEscappedText = ((Element) valueElement.get(0)).html();

            String originalUnescapedText = ((Element) valueElement.get(0)).text();

            String cleanText = Jsoup.clean(originalUnescapedText, Whitelist.none());

            int wordsCount = countWords(cleanText);

            totalWordsCount.addAndGet(wordsCount);

            countOfTotalValueElements.getAndIncrement();
            if (isWellFormedHtml(originalUnescapedText)) {
                countOfHtmlKeys.getAndIncrement();
                String name = d.attr("name");
                listOfKeys.add(name);
                valueElement.html(originalUnescapedText);
            }
        });
        String analysisContent = "Total translatable keys: " + countOfTotalValueElements + "\nCount of html containing elements: " + countOfHtmlKeys;

        analysisContent = analysisContent + "\nword count in this file: " + analysisContent;


        return analysisContent;
    }


    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String[] arr = text.split("\\s+");
        return arr.length;
    }


    private static String readXmlFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
            } catch (Throwable throwable) {
                try {
                    reader.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return content.toString();
    }


    public void createOutputFile(Document jsoupDocument, String outputFile) {
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.syntax(Document.OutputSettings.Syntax.xml);
        outputSettings.prettyPrint(true);
        outputSettings.charset("UTF-8");
        outputSettings.escapeMode(Entities.EscapeMode.xhtml);
        outputSettings.indentAmount(4);
        outputSettings.outline(true);
        jsoupDocument.outputSettings(outputSettings);

        String fileContent = jsoupDocument.outerHtml();

        try {
            FileWriter writer = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(fileContent);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Document escapeContent(String fileInput) {
        AtomicInteger escaped = new AtomicInteger();
        String fileContent = parseFile(fileInput);
        Document jsoupDocument = Jsoup.parse(fileContent, "", Parser.xmlParser());
        Elements dataElements = jsoupDocument.select("data");
        dataElements.forEach(d -> {
            Elements valueElement = d.select("value");

            String text = ((Element) valueElement.get(0)).html();

            if (isWellFormedHtml(text)) {
                String escpaedText = StringEscapeUtils.escapeHtml4(text);

                valueElement.html(escpaedText);

                escaped.getAndIncrement();
            }
        });

        System.out.println("total escaped: " + escaped);
        return jsoupDocument;
    }


    public void analyseKey(String fileName, String key) {
        AtomicInteger countOfHtmlKeys = new AtomicInteger();
        AtomicInteger totalWordsCount = new AtomicInteger();
        AtomicInteger countOfTotalValueElements = new AtomicInteger();

        List<String> listOfKeys = new ArrayList<>();
        String fileContent = parseFile(fileName);
        Document jsoupDocument = Jsoup.parse(fileContent, "", Parser.xmlParser());
        Elements dataElements = jsoupDocument.select("data");
        dataElements
                .forEach(d -> {
                    String keyName = d.attr("name");


                    if (keyName.startsWith(key)) {
                        Element valueElement = (Element) d.select("value").get(0);

                        String text = valueElement.text();

                        int wordCountForKey = countWords(text);

                        totalWordsCount.addAndGet(wordCountForKey);
                    }
                });

        System.out.println("for key: " + key + ", words count: " + totalWordsCount);
    }
}

