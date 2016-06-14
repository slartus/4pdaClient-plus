package org.softeg.slartus.forpdaapi.post;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by slinkin on 14.07.2015.
 */
public class EditPostParams implements Serializable {
    private List<String> orderedNames = new ArrayList<>();
    private Map<String, String> params = new HashMap<>();

    public Boolean containsKey(String key) {
        return params.containsKey(key);
    }

    public void put(String key, String value) {
        orderedNames.add(key);
        params.put(key, value);
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
