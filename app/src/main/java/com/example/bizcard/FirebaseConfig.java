package com.example.bizcard;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * פרק 11, סעיף "מכינים את הפרויקט ל-Firebase" — החיבור לענן בלי google-services.json.
 *
 * בפרויקט "רגיל" מורידים מהקונסולה קובץ בשם google-services.json ומניחים אותו בתיקיית app.
 * התוסף com.google.gms.google-services קורא ממנו את הפרטים ומייצר את החיבור לבד.
 * הבעיה: הקובץ הזה שייך לחשבון Firebase מסוים, ובלעדיו הפרויקט אפילו לא מתקמפל.
 *
 * לכן בפרויקט הזה **אין תוסף ואין קובץ json**. במקומם ממלאים כאן שלושה קבועים,
 * ובונים את אותו חיבור בדיוק בקוד, דרך FirebaseOptions.Builder.
 * כל עוד הקבועים ריקים — האפליקציה עובדת רגיל, המסכים של SharedPreferences והקבצים
 * פועלים במלואם, ומסכי הענן מפנים למסך הסבר במקום לקרוס.
 *
 * === מאיפה לוקחים כל ערך ===
 * נכנסים ל-https://console.firebase.google.com, בוחרים את הפרויקט שלכם,
 * ולוחצים על גלגל השיניים ⚙ ליד Project Overview ← Project settings ← לשונית General.
 * גוללים למטה אל "Your apps" ובוחרים את אפליקציית האנדרואיד שרשמתם.
 *
 *   API_KEY        — בכרטיס האפליקציה, השדה "Web API Key" (או "Api key" בקובץ ה-json,
 *                    תחת api_key → current_key). מחרוזת שמתחילה ב-AIza...
 *   APPLICATION_ID — באותו כרטיס, השדה "App ID". נראה כך: 1:123456789012:android:abc123def456
 *   DATABASE_URL   — בתפריט הצד: Build ← Realtime Database. הכתובת מופיעה בראש הטבלה,
 *                    בצורה https://<שם-הפרויקט>-default-rtdb.europe-west1.firebasedatabase.app
 *
 * שימו לב: שם החבילה שרשמתם בקונסולה חייב להיות בדיוק com.ort.tivon.ch11 —
 * זה ה-applicationId שמופיע ב-app/build.gradle.kts.
 */
public final class FirebaseConfig {

    // ↓↓↓ שלושת הערכים שהתלמיד ממלא. השאירו את הגרשיים, הדביקו בין הגרשיים. ↓↓↓

    /** "Web API Key" ממסך Project settings ← General. מתחיל ב-AIza */
    public static final String API_KEY = "AIzaSyC5vyypfgkyxQc58Q_J1gS4rOhv_Yza8Is";

    /** "App ID" של אפליקציית האנדרואיד. בצורה 1:000000000000:android:0000000000000000 */
    public static final String APPLICATION_ID = "1:609335051955:android:7cd8af194cdfc3b6b7d2c6";

    /** כתובת ה-Realtime Database ממסך Build ← Realtime Database */
    public static final String DATABASE_URL = "https://biscard-554b7-default-rtdb.firebaseio.com/";

    // ↑↑↑ עד כאן. כל השאר עובד לבד. ↑↑↑

    /**
     * דלי האחסון של Firebase Storage.
     * ברוב המקרים אפשר להשאיר ריק — הוא נגזר אוטומטית משם הפרויקט שבתוך DATABASE_URL.
     * אם במסך Build ← Storage מופיעה אצלכם כתובת אחרת (למשל שמסתיימת ב-appspot.com),
     * העתיקו אותה לכאן בלי הקידומת gs:// — לדוגמה "my-project.appspot.com".
     */
    public static final String STORAGE_BUCKET = "";

    private FirebaseConfig() {
    }

    /** האם התלמיד כבר מילא את שלושת הערכים? */
    public static boolean isConfigured() {
        return !API_KEY.trim().isEmpty()
                && !APPLICATION_ID.trim().isEmpty()
                && !DATABASE_URL.trim().isEmpty();
    }

    /**
     * מאתחל את FirebaseApp ידנית, בלי google-services.json.
     * מחזיר true אם החיבור מוכן לשימוש, ו-false אם עוד לא מילאו את הקבועים.
     * אפשר לקרוא לזה כמה פעמים שרוצים — האתחול קורה רק פעם אחת.
     */
    public static boolean ensureInitialized(Context context) {
        if (!isConfigured()) {
            return false;                       // אין פרטים — לא מאתחלים, ולא קורסים
        }
        if (!FirebaseApp.getApps(context).isEmpty()) {
            return true;                        // כבר אותחל בהרצה הזו
        }
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey(API_KEY.trim())              // ← במקום api_key שב-json
                    .setApplicationId(APPLICATION_ID.trim())// ← במקום mobilesdk_app_id
                    .setDatabaseUrl(DATABASE_URL.trim())    // ← במקום firebase_url
                    .setProjectId(projectId())              // נגזר מהכתובת של המסד
                    .setStorageBucket(storageBucket())      // ← במקום storage_bucket
                    .build();

            FirebaseApp.initializeApp(context.getApplicationContext(), options);
            return true;
        } catch (Exception e) {
            // ערך שגוי שהודבק בטעות — עדיף מסך הסבר מאשר קריסה
            return false;
        }
    }

    /**
     * הקיצור שכל מסך ענן משתמש בו: אם Firebase מוכן — ממשיכים.
     * אם לא — פותחים את מסך ההסבר וסוגרים את המסך הנוכחי.
     */
    public static boolean requireFirebase(Activity activity) {
        if (ensureInitialized(activity)) {
            return true;
        }
        activity.startActivity(new Intent(activity, FirebaseSetupActivity.class));
        activity.finish();
        return false;
    }

    /**
     * שולף את שם הפרויקט מתוך כתובת המסד.
     * https://my-project-default-rtdb.europe-west1.firebasedatabase.app → my-project
     */
    public static String projectId() {
        String url = DATABASE_URL.trim();
        int start = url.indexOf("://");
        if (start >= 0) {
            url = url.substring(start + 3);
        }
        int slash = url.indexOf('/');
        if (slash >= 0) {
            url = url.substring(0, slash);          // נשאר רק שם המארח
        }
        int rtdb = url.indexOf("-default-rtdb");
        if (rtdb > 0) {
            return url.substring(0, rtdb);
        }
        int dot = url.indexOf('.');
        return dot > 0 ? url.substring(0, dot) : url;
    }

    /** כתובת דלי האחסון: מה שהוגדר ידנית, ואם לא — <שם-הפרויקט>.firebasestorage.app */
    public static String storageBucket() {
        if (!STORAGE_BUCKET.trim().isEmpty()) {
            return STORAGE_BUCKET.trim();
        }
        return projectId() + ".firebasestorage.app";
    }
}
