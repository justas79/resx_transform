package com.ysc.utils;

import org.jsoup.nodes.Document;

public interface ParsingService {

    Document unescapeHtmlInValues(String fileName);
}
