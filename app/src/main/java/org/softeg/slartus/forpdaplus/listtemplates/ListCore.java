package org.softeg.slartus.forpdaplus.listtemplates;

import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.MainActivity;

import java.util.ArrayList;

/*
 * Created by slinkin on 20.02.14.
 */
public class ListCore {
    public static ArrayList<BrickInfo> getMainMenuBricks() {
        ArrayList<BrickInfo> res = new ArrayList<BrickInfo>();
        ArrayList<BrickInfo> allItems = getAllMenuBricks();
        for (String item : MainActivity.getPreferences().getString("selectedMenuItems", "0,1,2,3,4,5,9,10").split(","))
            if(!item.equals("")&!item.equals("null")) res.add(allItems.get(Integer.parseInt(item)));
        return res;
    }
    public static ArrayList<BrickInfo> getAllMenuBricks() {
        ArrayList<BrickInfo> allItems = new ArrayList<>();
        allItems.add(new NewsPagerBrickInfo());//0
        allItems.add(new FavoritesBrickInfo());//1
        allItems.add(new ForumBrickInfo());//2
        allItems.add(new TopicsHistoryBrickInfo());//3
        allItems.add(new NotesBrickInfo());//4
        allItems.add(new AppAndGame());//5
        allItems.add(new AppsBrickInfo());//6
        allItems.add(new AppsGamesCatalogBrickInfo());//7
        allItems.add(new DigestCatalogBrickInfo());//8
        allItems.add(new DevDbCatalogBrickInfo());//9
        allItems.add(new LeadsBrickInfo());//10
        return allItems;
    }

    /**
     * Кирпичи для создания нового поста извне
     */
    public static ArrayList<BrickInfo> getCreatePostBricks() {
        ArrayList<BrickInfo> res = new ArrayList<BrickInfo>();
        res.add(new FavoritesBrickInfo());
        res.add(new TopicsHistoryBrickInfo());
        res.add(new AppsBrickInfo());
        return res;
    }

    public static ArrayList<BrickInfo> createBricks(String[] brickNames) {
        ArrayList<BrickInfo> res = new ArrayList<BrickInfo>();
        for (String brickName : brickNames) {
            if (TextUtils.isEmpty(brickName)) continue;
            BrickInfo brickInfo = getRegisteredBrick(brickName);
            if (brickInfo == null)
                continue;
            res.add(getRegisteredBrick(brickName));
        }
        return res;
    }

    /**
     * Кирпичи для быстрого доступа
     */
    public static ArrayList<BrickInfo> getQuickBricks() {
        ArrayList<BrickInfo> res = new ArrayList<BrickInfo>();
        res.add(new NewsPagerBrickInfo());
        res.add(new FavoritesBrickInfo());
        res.add(new ForumBrickInfo());
        res.add(new TopicsHistoryBrickInfo());
        res.add(new NotesBrickInfo());
        res.add(new AppsBrickInfo());
        res.add(new AppsGamesCatalogBrickInfo());
        res.add(new DigestCatalogBrickInfo());
        res.add(new DevDbCatalogBrickInfo());
        res.add(new LeadsBrickInfo());
        return res;
    }

    /**
     * Кирпичи для быстрого доступа
     */
    public static String[] getBricksNames(ArrayList<BrickInfo> bricks) {
        String[] res = new String[bricks.size()];
        int i = 0;
        for (BrickInfo brickInfo : bricks) {
            res[i++] = brickInfo.getName();
        }

        return res;
    }

    public static ArrayList<String> getTemplateTitles() {
        ArrayList<String> res = new ArrayList<String>();
        for (BrickInfo template : getMainMenuBricks()) {
            res.add(template.getTitle());
        }
        return res;
    }

    private static ArrayList<BrickInfo> m_RegisteredBricks = new ArrayList<>();

    public static ArrayList<BrickInfo> getRegisteredBricks(){
        return m_RegisteredBricks;
    }

    public static void registerBricks() {
        m_RegisteredBricks.add(new NewsBrickInfo());
        m_RegisteredBricks.add(new NewsPagerBrickInfo());
        m_RegisteredBricks.add(new FavoritesBrickInfo());
        m_RegisteredBricks.add(new ForumBrickInfo());
        m_RegisteredBricks.add(new AppsBrickInfo());
        m_RegisteredBricks.add(new ForumTopicsBrickInfo());
        m_RegisteredBricks.add(new DevDbCatalogBrickInfo());
        m_RegisteredBricks.add(new DevDbModelsBrickInfo());
        m_RegisteredBricks.add(new AppsGamesCatalogBrickInfo());
        m_RegisteredBricks.add(new AppsGamesTopicsBrickInfo());
        m_RegisteredBricks.add(new DigestCatalogBrickInfo());
        m_RegisteredBricks.add(new DigestTopicsListBrickInfo());
        m_RegisteredBricks.add(new TopicsHistoryBrickInfo());
        m_RegisteredBricks.add(new NotesBrickInfo());
        m_RegisteredBricks.add(new LeadsBrickInfo());
        m_RegisteredBricks.add(new UserReputationBrickInfo());
        m_RegisteredBricks.add(new TopicAttachmentBrickInfo());
        m_RegisteredBricks.add(new QmsContactsBrickInfo());
        m_RegisteredBricks.add(new TopicWritersBrickInfo());
        m_RegisteredBricks.add(new TopicReadersBrickInfo());
    }

    public static BrickInfo getRegisteredBrick(String name) {
        if (m_RegisteredBricks.size() == 0)
            registerBricks();
        for (BrickInfo template : m_RegisteredBricks) {
            if (name.equals(template.getName()))
                return template;
        }
        return null;
    }
}



