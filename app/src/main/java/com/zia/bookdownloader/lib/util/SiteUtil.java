package com.zia.bookdownloader.lib.util;

import com.zia.bookdownloader.lib.engine.ISite;
import com.zia.bookdownloader.lib.site.*;
import com.zia.bookdownloader.lib.site.anim.Binhuo;
import com.zia.bookdownloader.lib.site.anim.Daocaoren;
import com.zia.bookdownloader.lib.site.h.Jidian;
import com.zia.bookdownloader.lib.site.h.Shouji;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 */
public class SiteUtil {
    public static List<ISite> getAllSites() {
        List<ISite> sites = new ArrayList<>();
        //normal
        sites.add(new Biquge());
        sites.add(new Dingdian());
        sites.add(new Xbiquge());
        sites.add(new Kanshenzuo());
        sites.add(new Bishenge());
        sites.add(new Mainhuatang());

        //anim
        sites.add(new Binhuo());
        sites.add(new Daocaoren());

        //h
        sites.add(new Jidian());
        sites.add(new Shouji());
//        sites.add(new Zhai());
        return sites;
    }
}
