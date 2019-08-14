package ru.slartus.http.prefs;

import java.util.Map;

public interface IPreferences {
    boolean contains(String key);
    Map<String, ?> getAll();
    int getInt(String key, int defValue);
    String getString(String key, String defValue);
    boolean getBoolean(String key, boolean defValue); // added

    void putInt(String key, int value);
    void putBoolean(String key, boolean value); // added
    void putString(String key, String value);

    // easiness of use
    void put(String key, String value);
    void put(String key, int value);
    void put(String key, boolean value);
}
