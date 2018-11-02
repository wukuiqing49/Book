package com.zia.bookdownloader.lib.util;

/**
 * Created by zia on 2018/11/2.
 */
public class EpubUtil {

    public static String getHtml(String title,String content,String cssName){
        return "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"zh-CN\">\n<head>\n\t<title>" +
                title +
                "</title>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"../" +
                cssName +
                ".css\" />\n</head>\n<body>\n<h2><span style=\"border-bottom:1px solid\">" +
                title +
                "</span></h2>\n<div>\n\n" +
                content +
                "\n\n</div>\n</body>\n</html>\n";
    }

    public static String getCss(){
        return "body{\n" +
                " margin:10px;\n" +
                " font-size: 1.0em;word-wrap:break-word;\n" +
                "}\n" +
                "ul,li{list-style-type:none;margin:0;padding:0;}\n" +
                "p{text-indent:2em; line-height:1.5em; margin-top:0; margin-bottom:1.5em;}\n" +
                ".catalog{padding: 1.5em 0;font-size: 0.8em;}\n" +
                "li{border-bottom: 1px solid #D5D5D5;}\n" +
                "h1{font-size:1.6em; font-weight:bold;}\n" +
                "h2 {\n" +
                "    display: block;\n" +
                "    font-size: 1.2em;\n" +
                "    font-weight: bold;\n" +
                "    margin-bottom: 0.83em;\n" +
                "    margin-left: 0;\n" +
                "    margin-right: 0;\n" +
                "    margin-top: 1em;\n" +
                "}\n" +
                ".mbppagebreak {\n" +
                "    display: block;\n" +
                "    margin-bottom: 0;\n" +
                "    margin-left: 0;\n" +
                "    margin-right: 0;\n" +
                "    margin-top: 0 }\n" +
                "a {\n" +
                "    color: inherit;\n" +
                "    text-decoration: none;\n" +
                "    cursor: default\n" +
                "    }\n" +
                "a[href] {\n" +
                "    color: blue;\n" +
                "    text-decoration: none;\n" +
                "    cursor: pointer\n" +
                "    }\n" +
                "\n" +
                ".italic {\n" +
                "    font-style: italic\n" +
                "    }\n";
    }
}
