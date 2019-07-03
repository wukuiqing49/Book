package com.zia.widget.reader;

import android.graphics.Paint;
import android.util.LruCache;
import android.util.SparseArray;

import com.zia.widget.reader.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Chu on 2017/8/12.
 */

public abstract class StringAdapter implements PageLoaderAdapter {
    private LruCache<Integer, SparseArray<ArrayList<String>>> map;
    private PageProperty mPageProperty;


    @Override
    public int getPageCount(int section, PageProperty property) {
        return getSectionData(section, property).size();
    }

    @Override
    public List<String> getPageLines(int section, int page, PageProperty property) {
        return getSectionData(section, property).get(page);
    }

    private SparseArray<ArrayList<String>> getSectionData(int section, PageProperty property) {
        SparseArray<ArrayList<String>> pages = null;
        if (map == null) {
            map = new LruCache<>(3);
            pages = loadPages(getPageSource(section), property.textPaint, property.visibleHeight, property.visibleWidth, property.intervalSize, property.paragraphSize);
            map.put(section, pages);
            mPageProperty = property;
        } else {
            if (mPageProperty != null && mPageProperty.equals(property)) {
                pages = map.get(section);
            }
            if (pages == null) {
                pages = loadPages(getPageSource(section), property.textPaint, property.visibleHeight, property.visibleWidth, property.intervalSize, property.paragraphSize);
                map.put(section, pages);
                mPageProperty = property;
            }
        }
        return pages;
    }


    protected abstract List<String> getPageSource(int section);

    @Override
    public abstract int getSectionCount();

    @Override
    public abstract String getSectionName(int section);

    public static final String space = String.valueOf((char) 12288) + (char) 12288;

//    public static SparseArray<ArrayList<String>> loadPages(List<String> source, Paint textPaint, int visibleHeight,
//                                                           int visibleWidth, int intervalSize, int paragraphSize) {
//        SparseArray<ArrayList<String>> pageArray = new SparseArray<>();
//        List<String> lines = new ArrayList<>();
//        if (source != null && source.size() > 0) {
//            //剩余高度
//            int rHeight = visibleHeight + intervalSize + paragraphSize;
//            for (String paragraph : source) {
//                boolean hasContent = false;
//                //如果只有换行符，那么就不执行
//                if (StringUtils.isBlank(paragraph)) continue;
//                //重置段落
////                paragraph = StringUtils.halfToFull("  " + paragraph + "\n");
//                //删除最开始的空格
////                paragraph = TextUtil.removeSpaceStart(paragraph);
//                //添加首行缩进
//                paragraph = space + paragraph + "\n";
////                paragraph = StringUtils.trimBeforeReplace(paragraph, "　　");
//                while (paragraph.length() > 0) {
//
//
//                    //测量一行占用的字节数
//                    int count = textPaint.breakText(paragraph, true, visibleWidth, null);
//                    String subStr = paragraph.substring(0, count);
//                    String trim = subStr.trim();
//                    if (trim.length() > 0 && !trim.equals("\n") && !trim.equals("\r\n") && !StringUtils.isBlank(trim)) {
//                        //重置剩余距离
//                        rHeight -= (textPaint.getTextSize() + intervalSize);
//
//                        //达到行数要求,创建Page
//                        if (rHeight < 0) {
//                            //创建Page
//                            pageArray.put(pageArray.size(), new ArrayList<>(lines));
//                            //重置Lines
//                            lines.clear();
//                            rHeight = visibleHeight;
//                            continue;
//                        }
//                        //将一行字节，存储到lines中
//                        lines.add(subStr);
//                        hasContent = true;
//                    }
//
//
//                    //裁剪
//                    paragraph = paragraph.substring(count);
//                }
//
//                if (lines.size() > 0 && hasContent) {
//                    rHeight -= paragraphSize;
//                }
//            }
//
//            if (lines.size() != 0) {
//                pageArray.put(pageArray.size(), new ArrayList<>(lines));
//                //重置Lines
//                lines.clear();
//
//            }
//        }
//        return pageArray;
//    }

    public static SparseArray<ArrayList<String>> loadPages(List<String> source, Paint textPaint, int visibleHeight,
                                                           int visibleWidth, int intervalSize, int paragraphSize) {
        SparseArray<ArrayList<String>> result = new SparseArray<>();
        if (source != null && source.size() > 0) {

            //生成适合手机宽度的行列
            List<String> measuredLines = new LinkedList<>();

            visibleHeight = visibleHeight - PageLoader.contentMarginTop - PageLoader.contentMarginBottom;
            for (String line : source) {
                line = space + line;
                int pos = 0;
                final int lineLength = line.length();
                while (pos < lineLength) {
                    int count = textPaint.breakText(line, true, visibleWidth, null);
                    String s = line.substring(0, count);
                    line = line.substring(count);
                    pos += count;
                    if (s.length() == 0 || StringUtils.isBlank(s) || s.equals("\n") || s.equals("\n\r")) {
                        continue;
                    }
                    measuredLines.add(s);
                }
                //添加一个换行，在后面用于增加行距
                measuredLines.add("\n");
            }

            int pagePosition = 0;
            //一行高度 = 字体大小 + 行间距
            int lineHeight = (int) (textPaint.getTextSize() + intervalSize);
            int h = visibleHeight;
            List<String> pageLines = new ArrayList<>();
            boolean measureTitle = true;
            boolean firstLine = true;
            for (String measuredLine : measuredLines) {
                if (measureTitle) {
                    measureTitle = false;
                    //空出一行绘制标题
                    h = (int) (h - textPaint.getTextSize() * PageProperty.titleRatio - intervalSize);
                }
                if (h < lineHeight) {
                    //添加Page
                    result.put(pagePosition, new ArrayList<>(pageLines));
                    pagePosition++;
                    //重置属性
                    h = visibleHeight;
                    firstLine = true;
                    pageLines.clear();
                }
                //如果段落结束，多空出一个段落间距
                if (measuredLine.equals("\n") || StringUtils.isBlank(measuredLine)) {
                    //不能让最后一行被空格占用
                    if (h - paragraphSize < lineHeight) {
                        continue;
                    } else {
                        h -= paragraphSize;
                    }
                    //不能让第一行被空格占用
                    if (firstLine) {
                        continue;
                    }
                } else {
                    firstLine = false;
                    h = h - lineHeight;
                }
                pageLines.add(measuredLine);
            }
            if (!pageLines.isEmpty()){
                //把最后一页也加入集合
                result.put(pagePosition, new ArrayList<>(pageLines));
            }
//            //剩余高度
//            int rHeight = visibleHeight + intervalSize + paragraphSize;
//            for (String paragraph : source) {
//                boolean hasContent = false;
//                //如果只有换行符，那么就不执行
//                if (StringUtils.isBlank(paragraph)) continue;
//                //重置段落
////                paragraph = StringUtils.halfToFull("  " + paragraph + "\n");
//                //删除最开始的空格
////                paragraph = TextUtil.removeSpaceStart(paragraph);
//                //添加首行缩进
//                paragraph = space + paragraph + "\n";
////                paragraph = StringUtils.trimBeforeReplace(paragraph, "　　");
//                while (paragraph.length() > 0) {
//
//
//                    //测量一行占用的字节数
//                    int count = textPaint.breakText(paragraph, true, visibleWidth, null);
//                    String subStr = paragraph.substring(0, count);
//                    String trim = subStr.trim();
//                    if (trim.length() > 0 && !trim.equals("\n") && !trim.equals("\r\n") && !StringUtils.isBlank(trim)) {
//                        //重置剩余距离
//                        rHeight -= (textPaint.getTextSize() + intervalSize);
//
//                        //达到行数要求,创建Page
//                        if (rHeight < 0) {
//                            //创建Page
//                            pageArray.put(pageArray.size(), new ArrayList<>(lines));
//                            //重置Lines
//                            lines.clear();
//                            rHeight = visibleHeight;
//                            continue;
//                        }
//                        //将一行字节，存储到lines中
//                        lines.add(subStr);
//                        hasContent = true;
//                    }
//
//
//                    //裁剪
//                    paragraph = paragraph.substring(count);
//                }
//
//                if (lines.size() > 0 && hasContent) {
//                    rHeight -= paragraphSize;
//                }
//            }
//
//            if (lines.size() != 0) {
//                pageArray.put(pageArray.size(), new ArrayList<>(lines));
//                //重置Lines
//                lines.clear();
//
//            }
        }
        return result;
    }
}
