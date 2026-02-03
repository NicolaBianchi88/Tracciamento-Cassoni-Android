package com.example.tracciamentocassoni;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.Calendar;

public class SessionManager {
    private static final String PREFS = "secure_session";
    private static final String KEY_LOGIN_TIME = "login_time_ms";

    // 24 ore in millisecondi
    private static final long TTL_MS = 24L * 60L * 60L * 1000L;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Errore init EncryptedSharedPreferences", e);
        }
    }

    /** Chiama questo quando il login è OK */
    public void markLoginNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            prefs.edit()
                    .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                    .apply();
        }
    }

    /** true se il login è stato fatto e non sono passate 24 ore
    public boolean isLoginValid() {
        long t = prefs.getLong(KEY_LOGIN_TIME, 0L);
        if (t == 0L) return false;
        return (System.currentTimeMillis() - t) < TTL_MS;
    }
     */

    /** Fa scadere il login alla mezzanotte*/

    public boolean isLoginValid() {
        long loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L);
        if (loginTime == 0L) return false;

        long now = System.currentTimeMillis();

        // Calcola la "mezzanotte" (inizio del giorno successivo) rispetto ad ORA
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long nextMidnight = cal.getTimeInMillis();

        // Valido solo se:
        // 1) il login è avvenuto oggi (dopo la mezzanotte di oggi)
        // 2) e non abbiamo ancora superato la mezzanotte di domani
        Calendar startToday = Calendar.getInstance();
        startToday.setTimeInMillis(now);
        startToday.set(Calendar.HOUR_OF_DAY, 0);
        startToday.set(Calendar.MINUTE, 0);
        startToday.set(Calendar.SECOND, 0);
        startToday.set(Calendar.MILLISECOND, 0);

        long todayMidnight = startToday.getTimeInMillis();

        return loginTime >= todayMidnight && now < nextMidnight;
    }
}