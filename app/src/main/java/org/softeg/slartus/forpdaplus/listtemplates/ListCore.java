package org.softeg.slartus.forpdaplus.listtemplates;

import android.text.TextUtils;

import org.softeg.slartus.forpdaplus.MainActivity;

import java.util.ArrayList;

public class ListCore {

    public static final String DEFAULT_MENU_ITEMS = "0,1,2,3,4,7,8";

    public static ArrayList<BrickInfo> getMainMenuBricks() {

        ArrayList<BrickInfo> allItems = getAllMenuBricks();

        return new ArrayList<>(allItems);
    }

    public static boolean checkIndex(String[] items, int size) {
        boolean state = false;
        for (String item : items) {
            if (Integer.parseInt(item) > size) {
                state = true;
                break;
            }
        }
        return state;
    }

    public static ArrayList<BrickInfo> getAllMenuBricks() {
        ArrayList<BrickInfo> allItems = new ArrayList<>();
        allItems.add(new NewsPagerBrickInfo());//0
        allItems.add(new FavoritesBrickInfo());//1
        allItems.add(new ForumBrickInfo());//2
        allItems.add(new TopicsHistoryBrickInfo());//3
        allItems.add(new NotesBrickInfo());//4
        allItems.add(new DevDbCatalogBrickInfo());// 5
        allItems.add(new LeadsBrickInfo());// 6
        return allItems;
    }

    public static ArrayList<BrickInfo> getOthersBricks(){
        ArrayList<BrickInfo> res = new ArrayList<>();
        res.add(new PreferencesBrickInfo());
        res.add(new MarkAllReadBrickInfo());
        res.add(new FaqBrickInfo());
        res.add(new ForumRulesBrick());
        return res;
    }

    public static ArrayList<BrickInfo> createBricks(String[] brickNames) {
        ArrayList<BrickInfo> res = new ArrayList<>();
        for (String brickName : brickNames) {
            if (TextUtils.isEmpty(brickName)) continue;
            BrickInfo brickInfo = getRegisteredBrick(brickName);
            if (brickInfo == null)
                continue;
            res.add(getRegisteredBrick(brickName));
        }
        return res;
    }

    private static final ArrayList<BrickInfo> m_RegisteredBricks = new ArrayList<>();

    public static void registerBricks() {
        m_RegisteredBricks.add(new NewsBrickInfo());
        m_RegisteredBricks.add(new NewsPagerBrickInfo());
        m_RegisteredBricks.add(new FavoritesBrickInfo());
        m_RegisteredBricks.add(new ForumBrickInfo());
        m_RegisteredBricks.add(new ForumTopicsBrickInfo());
        m_RegisteredBricks.add(new DevDbCatalogBrickInfo());
        m_RegisteredBricks.add(new DevDbModelsBrickInfo());
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



