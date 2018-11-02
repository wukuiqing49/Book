package com.zia.bookdownloader.lib.engine;

import com.zia.bookdownloader.lib.bean.Catalog;

import java.util.List;

public interface IRegex {
    public List<Catalog> parseCatalog(String catalogHtml, String url);

    public List<String> parseContent(String chapterHtml);
}
