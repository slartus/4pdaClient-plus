package org.softeg.slartus.forpdaplus.emotic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.classes.BbImage;
import org.softeg.slartus.forpdaplus.classes.common.ExtBitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:27
 * To change this template use File | Settings | File Templates.
 */
public class Smiles extends ArrayList<Smile> {

    protected Smiles() {
        fill();

    }

    public static Smiles createSmiles() {
        return new Smiles();
//        String cssFilePath = MyApp.getInstance().getThemeCssFileName();
//        if (cssFilePath.startsWith("/android_asset/"))
//            return new Smiles();
//        return new SmilesPack(cssFilePath);
    }

    public void setWeights() {
        SharedPreferences preferences = App.getInstance().getPreferences();
        String weights = preferences.getString("smiles.weights", "");
        Matcher m = Pattern.compile("(.*?):(\\d+);").matcher(weights);
        while (m.find()) {
            Smile smile = findByFileName(m.group(1));
            if (smile == null) continue;
            smile.Weight = Integer.parseInt(m.group(2));
        }
    }

    private void saveWeights() {
        normalizeWights();
        App.getInstance().getPreferences().edit()
                .putString("smiles.weights", getWeightString())
                .apply();

    }

    private String getWeightString() {
        StringBuilder sb = new StringBuilder();
        for (Smile smile : this) {
            sb.append(smile.FileName).append(":").append(smile.Weight).append(";");
        }
        return sb.toString();
    }

    private void normalizeWights() {
        int minWeight = Integer.MAX_VALUE;
        for (Smile smile : this) {
            minWeight = Math.min(minWeight, smile.Weight);
        }
        if (minWeight > 0)
            for (Smile smile : this) {
                smile.Weight -= 1;
            }
    }

    public void sortByWeights() {
        Collections.sort(this, new Comparator<Smile>() {
            @Override
            public int compare(Smile lhs, Smile rhs) {
                if (lhs.Weight == rhs.Weight)
                    return 0;
                return lhs.Weight > rhs.Weight ? -1 : 1;
            }
        });
    }

    public void addWeight(Smile smile) {
        smile.Weight += 1;
        saveWeights();
    }

    public Smile findByFileName(String fileName) {
        for (Smile smile : this) {
            if (smile.FileName.equals(fileName))
                return smile;
        }
        return null;
    }

    public String getDirPath() {
        return "forum/style_emoticons/default/";
    }

    public String getCssPath() {
        return "file:///android_asset/forum/style_emoticons";
    }

    public BbImage[] getFilesList() {
        setWeights();
        sortByWeights();

        BbImage[] res = new BbImage[size()];
        String path = getDirPath();
        for (int i = 0; i < size(); i++) {
            res[i] = new BbImage(path, this.get(i).FileName, this.get(i).HtmlText);
        }
        return res;
    }

    public static String getPattern(String value, Hashtable<String, String> emoticsDict, String path) {


        SmilesComparator bvc = new SmilesComparator(emoticsDict);
        TreeMap<String, String> sorted_map = new TreeMap<String, String>(bvc);
        sorted_map.putAll(emoticsDict);
        StringBuilder sb=new StringBuilder();
        for (Map.Entry<String, String> entry : sorted_map.entrySet()) {
            String emo = entry.getKey();
            if (!emo.startsWith(":") || !emo.endsWith(":")) {
                value = value.replaceAll("(^|\\s+)" + Pattern.quote(emo) + "($|\\s+)", String.format("$1<img src=\"%s%s\"/>$2", path, entry.getValue()));
            } else
                value = value.replaceAll("(^|.)" + Pattern.quote(emo) + "($|.)", String.format("$1<img src=\"%s%s\"/>$2", path, entry.getValue()));

        }
        return value;

    }

    public static Hashtable<String, String> getSmilesDict() {
        Hashtable<String, String> res = new Hashtable<String, String>();
        res.put(":happy:", "happy.gif");
        res.put(";)", "wink.gif");
        res.put(":P", "tongue.gif");
        res.put(":-D", "biggrin.gif");
        res.put(":lol:", "laugh.gif");
        res.put(":rolleyes:", "rolleyes.gif");
        res.put(":)", "smile_good.gif");
        res.put(":beee:", "beee.gif");
        res.put(":rofl:", "rofl.gif");
        res.put(":sveta:", "sveta.gif");
        res.put(":thank_you:", "thank_you.gif");
        res.put("}-)", "devil.gif");
        res.put(":girl_cray:", "girl_cray.gif");
        res.put(":D", "biggrin.gif");
        res.put("o.O", "blink.gif");
        res.put(":blush:", "blush.gif");
        res.put(":yes2:", "yes.gif");
        res.put(":mellow:", "mellow.gif");
        res.put(":huh:", "huh.gif");
        res.put(":o", "ohmy.gif");
        res.put("B)", "cool.gif");
        res.put("-_-", "sleep.gif");
        res.put("&lt;_&lt;", "dry.gif");
        res.put(":wub:", "wub.gif");
        res.put(":angry:", "angry.gif");
        res.put(":(", "sad.gif");
        res.put(":unsure:", "unsure.gif");
        res.put(":wacko:", "wacko.gif");
        res.put(":blink:", "blink.gif");
        res.put(":ph34r:", "ph34r.gif");
        res.put(":banned:", "banned.gif");
        res.put(":antifeminism:", "antifeminism.gif");
        res.put(":beta:", "beta.gif");
        res.put(":boy_girl:", "boy_girl.gif");
        res.put(":butcher:", "butcher.gif");
        res.put(":bubble:", "bubble.gif");
        res.put(":censored:", "censored.gif");
        res.put(":clap:", "clap.gif");
        res.put(":close_tema:", "close_tema.gif");
        res.put(":clapping:", "clapping.gif");
        res.put(":coldly:", "coldly.gif");
        res.put(":comando:", "comando.gif");
        res.put(":congratulate:", "congratulate.gif");
        res.put(":dance:", "dance.gif");
        res.put(":daisy:", "daisy.gif");
        res.put(":dancer:", "dancer.gif");
        res.put(":derisive:", "derisive.gif");
        res.put(":dinamo:", "dinamo.gif");
        res.put(":dirol:", "dirol.gif");
        res.put(":diver:", "diver.gif");
        res.put(":drag:", "drag.gif");
        res.put(":download:", "download.gif");
        res.put(":drinks:", "drinks.gif");
        res.put(":first_move:", "first_move.gif");
        res.put(":feminist:", "feminist.gif");
        res.put(":flood:", "flood.gif");
        res.put(":fool:", "fool.gif");
        res.put(":friends:", "friends.gif");
        res.put(":foto:", "foto.gif");
        res.put(":girl_blum:", "girl_blum.gif");
        res.put(":girl_crazy:", "girl_crazy.gif");
        res.put(":girl_curtsey:", "girl_curtsey.gif");
        res.put(":girl_dance:", "girl_dance.gif");
        res.put(":girl_flirt:", "girl_flirt.gif");
        res.put(":girl_hospital:", "girl_hospital.gif");
        res.put(":girl_hysterics:", "girl_hysterics.gif");
        res.put(":girl_in_love:", "girl_in_love.gif");
        res.put(":girl_kiss:", "girl_kiss.gif");
        res.put(":girl_pinkglassesf:", "girl_pinkglassesf.gif");
        res.put(":girl_parting:", "girl_parting.gif");
        res.put(":girl_prepare_fish:", "girl_prepare_fish.gif");
        res.put(":good:", "good.gif");
        res.put(":girl_spruce_up:", "girl_spruce_up.gif");
        res.put(":girl_tear:", "girl_tear.gif");
        res.put(":girl_tender:", "girl_tender.gif");
        res.put(":girl_teddy:", "girl_teddy.gif");
        res.put(":girl_to_babruysk:", "girl_to_babruysk.gif");
        res.put(":girl_to_take_umbrage:", "girl_to_take_umbrage.gif");
        res.put(":girl_triniti:", "girl_triniti.gif");
        res.put(":girl_tongue:", "girl_tongue.gif");
        res.put(":girl_wacko:", "girl_wacko.gif");
        res.put(":girl_werewolf:", "girl_werewolf.gif");
        res.put(":girl_witch:", "girl_witch.gif");
        res.put(":grabli:", "grabli.gif");
        res.put(":good_luck:", "good_luck.gif");
        res.put(":guess:", "guess.gif");
        res.put(":hang:", "hang.gif");
        res.put(":heart:", "heart.gif");
        res.put(":help:", "help.gif");
        res.put(":helpsmilie:", "helpsmilie.gif");
        res.put(":hemp:", "hemp.gif");
        res.put(":heppy_dancing:", "heppy_dancing.gif");
        res.put(":hysterics:", "hysterics.gif");
        res.put(":indeec:", "indeec.gif");
        res.put(":i-m_so_happy:", "i-m_so_happy.gif");
        res.put(":kindness:", "kindness.gif");
        res.put(":king:", "king.gif");
        res.put(":laugh_wild:", "laugh_wild.gif");
        res.put(":4PDA:", "love_4PDA.gif");
        res.put(":nea:", "nea.gif");
        res.put(":moil:", "moil.gif");
        res.put(":no:", "no.gif");
        res.put(":nono:", "nono.gif");
        res.put(":offtopic:", "offtopic.gif");
        res.put(":ok:", "ok.gif");
        res.put(":papuas:", "papuas.gif");
        res.put(":party:", "party.gif");
        res.put(":pioneer_smoke:", "pioneer_smoke.gif");
        res.put(":pipiska:", "pipiska.gif");
        res.put(":protest:", "protest.gif");
        res.put(":popcorm:", "popcorm.gif");
        res.put(":rabbi:", "rabbi.gif");
        res.put(":resent:", "resent.gif");
        res.put(":roll:", "roll.gif");
        res.put(":rtfm:", "rtfm.gif");
        res.put(":russian_garmoshka:", "russian_garmoshka.gif");
        res.put(":russian:", "russian.gif");
        res.put(":russian_ru:", "russian_ru.gif");
        res.put(":scratch_one-s_head:", "scratch_one-s_head.gif");
        res.put(":scare:", "scare.gif");
        res.put(":search:", "search.gif");
        res.put(":secret:", "secret.gif");
        res.put(":skull:", "skull.gif");
        res.put(":shok:", "shok.gif");
        res.put(":sorry:", "sorry.gif");
        res.put(":smoke:", "smoke.gif");
        res.put(":spiteful:", "spiteful.gif");
        res.put(":stop_flood:", "stop_flood.gif");
        res.put(":suicide:", "suicide.gif");
        res.put(":stop_holywar:", "stop_holywar.gif");
        res.put(":superman:", "superman.gif");
        res.put(":superstition:", "superstition.gif");
        res.put(":tablet_za:", "tablet_protiv.gif");
        res.put(":tablet_protiv:", "tablet_za.gif");
        res.put(":this:", "this.gif");
        res.put(":tomato:", "tomato.gif");
        res.put(":to_clue:", "to_clue.gif");
        res.put(":tommy:", "tommy.gif");
        res.put(":tongue3:", "tongue3.gif");
        res.put(":umnik:", "umnik.gif");
        res.put(":victory:", "victory.gif");
        res.put(":vinsent:", "vinsent.gif");
        res.put(":wallbash:", "wallbash.gif");
        res.put(":whistle:", "whistle.gif");
        res.put(":wink_kind:", "wink_kind.gif");
        res.put(":yahoo:", "yahoo.gif");
        res.put(":yes:", "yes.gif");
        res.put(":&#91;", "confusion.gif");
        res.put("&#93;-:{", "girl_devil.gif");
        res.put(":*", "kiss.gif");
        res.put("@}-'-,-", "give_rose.gif");
        res.put(":'(", "cry.gif");
        res.put(":-{", "mad.gif");
        res.put("=^.^=", "kitten.gif");
        res.put("(-=", "girl_hide.gif");
        res.put("(-;", "girl_wink.gif");
        res.put(")-:{", "girl_angry.gif");
        res.put("*-:", "girl_chmok.gif");
        res.put(")-:", "girl_sad.gif");
        res.put(":girl_mad:", "girl_mad.gif");
        res.put("(-:", "girl_smile.gif");
        res.put(":acute:", "acute.gif");
        res.put(":aggressive:", "aggressive.gif");
        res.put(":air_kiss:", "air_kiss.gif");
        res.put("o_O", "blink.gif");
        res.put(":-&#91;", "confusion.gif");
        res.put(":'-(", "cry.gif");
        res.put(":lol_girl:", "girl_haha.gif");
        res.put(")-':", "girl_cray.gif");
        res.put("(;", "girl_wink.gif");
        res.put(":-*", "kiss.gif");
        res.put(":laugh:", "laugh.gif");
        res.put(":ohmy:", "ohmy.gif");
        res.put(":-(", "sad.gif");
        res.put("8-)", "rolleyes.gif");
        res.put(":-)", "smile.gif");
        res.put(":smile:", "smile.gif");
        res.put(":-P", "tongue.gif");
        res.put(";-)", "wink.gif");
        return res;
    }


    private void fill() {
        Hashtable<String, String> dict = getSmilesDict();

        for (Map.Entry<String, String> entry : dict.entrySet()) {
            add(new Smile(entry.getKey()
                    .replace("&#93;", "]")
                    .replace("&#91;", "[")
                    .replace("&lt;", "<")
                    , entry.getValue()));
        }
    }

    public static Bitmap getBitmap(Context context, String filePath) throws IOException {
        return ExtBitmap.getBitmapFromAsset(context, filePath);
    }


}
