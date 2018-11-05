package com.zia.bookdownloader.lib.engine;

import com.zia.bookdownloader.lib.bean.Book;
import com.zia.bookdownloader.lib.bean.Chapter;
import com.zia.bookdownloader.lib.listener.EventListener;
import com.zia.bookdownloader.lib.util.EpubUtil;
import com.zia.bookdownloader.lib.util.TextUtil;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.*;
import java.util.List;

/**
 * Created By zia on 2018/11/1.
 * 分章节下载的小说网站
 */
public abstract class ChapterSite implements ISite, IRegex {

    private BookFucker bookFucker;

    @Override
    public File download(Book book, Type type, String savePath, EventListener eventListener) {
        mkdirs(savePath);
        bookFucker = new BookFucker();
        List<Chapter> chapters = bookFucker.download(book.getUrl(), getEncodeType(), eventListener, getThreadCount(), this);
        try {
            File file = save(chapters, book, savePath, type);
            eventListener.onEnd("文件保存成功，位置：" + file.getPath(), file);
            return file;
        } catch (Exception e) {
            eventListener.onError("文件保存失败", e);
            return null;
        }
    }

    @Override
    public void shutDown() {
        if (bookFucker != null) {
            bookFucker.shutdown();
        }
    }

    private File save(List<Chapter> chapters, Book book, String savePath, Type type) throws IOException {
        String bookName = book.getBookName() + "-" + getSiteName();
        if (type == Type.EPUB) {
            File file = new File(savePath + File.separator + bookName + ".epub");
            nl.siegmann.epublib.domain.Book epub = new nl.siegmann.epublib.domain.Book();
            String cssName = "book";
            epub.getResources().add(new Resource(EpubUtil.getCss().getBytes(), cssName + ".css"));
            Metadata metadata = epub.getMetadata();
            metadata.addTitle(bookName);
            metadata.addAuthor(new Author(book.getAuthor()));
            for (Chapter chapter : chapters) {
                StringBuilder content = new StringBuilder();
                for (String line : chapter.getContents()) {
                    line = TextUtil.cleanContent(line);
                    if (!line.isEmpty()) {
                        content.append("<p>");
                        content.append("    ");
                        content.append(line);
                        content.append("</p>");
                    }
                }
                epub.addSection(chapter.getChapterName(),
                        new Resource(EpubUtil.getHtml(chapter.getChapterName(), content.toString(), cssName).getBytes()
                                , chapter.getChapterName() + ".html"));
            }
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(epub, new FileOutputStream(file));
            return file;
        } else {
            savePath += File.separator + bookName + ".txt";
            File file = new File(savePath);
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file)));
            for (Chapter chapter : chapters) {
                bufferedWriter.write(chapter.getChapterName());
                bufferedWriter.write("\n\n");
                for (String line : chapter.getContents()) {
                    line = TextUtil.cleanContent(line);
                    //4个空格+正文+换行+空行
                    bufferedWriter.write("    ");
                    bufferedWriter.write(line);
                    bufferedWriter.write("\n\n");
                }
                //章节结束空三行，用来分割下一章节
                bufferedWriter.write("\n\n\n");
            }
            return file;
        }
    }

    private void mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public int getThreadCount() {
        return 150;
    }

    @Override
    public String getEncodeType() {
        return "gbk";
    }
}
