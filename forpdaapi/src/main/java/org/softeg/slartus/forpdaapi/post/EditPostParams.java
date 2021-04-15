package org.softeg.slartus.forpdaapi.post;


import org.softeg.slartus.forpdacommon.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.NameValuePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by slinkin on 14.07.2015.
 */
public class EditPostParams implements Serializable {
    private final List<String> orderedNames = new ArrayList<>();
    private final Map<String, String> params = new HashMap<>();

    public Boolean containsKey(String key) {
        return params.containsKey(key);
    }

    public void put(String key, String value) {
        orderedNames.add(key);
        params.put(key, value);
    }

    public void delete(String key) {
        if (params.containsKey(key)) {
            params.remove(params.get(key));
            orderedNames.remove(key);
        }
    }

    public String get(String key) {
        return params.get(key);
    }

    public List<NameValuePair> getListParams() {
        List<NameValuePair> res = new ArrayList<>();
        for (String key : orderedNames) {
            res.add(new BasicNameValuePair(key, params.get(key)));
        }
        return res;
    }
}
