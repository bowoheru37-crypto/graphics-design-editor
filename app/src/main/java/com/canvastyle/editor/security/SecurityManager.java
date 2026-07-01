package com.canvastyle.editor.security;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Objects;

public class SecurityManager {
    private static final String TAG = SecurityManager.class.getSimpleName();
    private static final String MASTER_KEY_ALIAS = "_androidx_security_master_key_";
    private final Context context;
    private EncryptedSharedPreferences encryptedPrefs;
    
    public SecurityManager(@NonNull Context context) {
        this.context = Objects.requireNonNull(context);
        initializeEncryption();
    }
    
    private void initializeEncryption() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secret_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            Log.d(TAG, "Encryption initialized");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Encryption init failed", e);
        }
    }
    
    public void storeSecure(@NonNull String key, @NonNull String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        try {
            if (encryptedPrefs != null) {
                encryptedPrefs.edit().putString(key, value).apply();
                Log.d(TAG, "Stored: " + key);
            }
        } catch (Exception e) {
            Log.e(TAG, "Store failed", e);
        }
    }
    
    public String retrieveSecure(@NonNull String key) {
        Objects.requireNonNull(key);
        try {
            if (encryptedPrefs != null) {
                return encryptedPrefs.getString(key, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Retrieve failed", e);
        }
        return null;
    }
    
    @NonNull
    public String generateHash(@NonNull String input) {
        Objects.requireNonNull(input);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Hash failed", e);
            return "";
        }
    }
    
    public boolean validateInput(@NonNull String input) {
        Objects.requireNonNull(input);
        if (input.contains(";") || input.contains("'") || input.contains("\"")) {
            Log.w(TAG, "Suspicious input");
            return false;
        }
        if (input.contains("..") || input.contains("./")) {
            Log.w(TAG, "Path traversal detected");
            return false;
        }
        return true;
    }
    
    @NonNull
    public String sanitizeInput(@NonNull String input) {
        Objects.requireNonNull(input);
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
