package com.ysc.utils;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class BaseParsingService {

    public boolean isWellFormedHtml(String text) {
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
