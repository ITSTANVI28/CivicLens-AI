package com.example.civiclensai.utils;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SessionManagerTest {

    private SessionManager sessionManager;
    private MockSharedPreferences mockPreferences;

    @Before
    public void setUp() {
        mockPreferences = new MockSharedPreferences();
        sessionManager = new SessionManager(mockPreferences);
    }

    @Test
    public void testDefaultSessionValues() {
        assertFalse(sessionManager.isLoggedIn());
        assertEquals("usr_1001", sessionManager.getUid());
        assertEquals("Alex Citizen", sessionManager.getUserName());
        assertEquals("alex.citizen@example.com", sessionManager.getUserEmail());
        assertEquals(150, sessionManager.getKarmaPoints());
    }

    @Test
    public void testCreateLoginSessionAndLogout() {
        sessionManager.createLoginSession("usr_505", "John Doe", "john@example.com");

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("usr_505", sessionManager.getUid());
        assertEquals("John Doe", sessionManager.getUserName());
        assertEquals("john@example.com", sessionManager.getUserEmail());
        assertEquals(150, sessionManager.getKarmaPoints());

        sessionManager.addKarmaPoints(50);
        assertEquals(200, sessionManager.getKarmaPoints());

        sessionManager.logoutUser();
        assertFalse(sessionManager.isLoggedIn());
    }

    // Custom lightweight in-memory Mock SharedPreferences
    private static class MockSharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override public Map<String, ?> getAll() { return values; }
        @Override public String getString(String key, String defValue) { return values.containsKey(key) ? (String) values.get(key) : defValue; }
        @Override public java.util.Set<String> getStringSet(String key, java.util.Set<String> defValues) { return defValues; }
        @Override public int getInt(String key, int defValue) { return values.containsKey(key) ? (Integer) values.get(key) : defValue; }
        @Override public long getLong(String key, long defValue) { return values.containsKey(key) ? (Long) values.get(key) : defValue; }
        @Override public float getFloat(String key, float defValue) { return values.containsKey(key) ? (Float) values.get(key) : defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { return values.containsKey(key) ? (Boolean) values.get(key) : defValue; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public Editor edit() { return new MockEditor(values); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
    }

    private static class MockEditor implements SharedPreferences.Editor {
        private final Map<String, Object> values;

        MockEditor(Map<String, Object> values) {
            this.values = values;
        }

        @Override public SharedPreferences.Editor putString(String key, String value) { values.put(key, value); return this; }
        @Override public SharedPreferences.Editor putStringSet(String key, java.util.Set<String> values) { return this; }
        @Override public SharedPreferences.Editor putInt(String key, int value) { values.put(key, value); return this; }
        @Override public SharedPreferences.Editor putLong(String key, long value) { values.put(key, value); return this; }
        @Override public SharedPreferences.Editor putFloat(String key, float value) { values.put(key, value); return this; }
        @Override public SharedPreferences.Editor putBoolean(String key, boolean value) { values.put(key, value); return this; }
        @Override public SharedPreferences.Editor remove(String key) { values.remove(key); return this; }
        @Override public SharedPreferences.Editor clear() { values.clear(); return this; }
        @Override public boolean commit() { return true; }
        @Override public void apply() {}
    }
}
