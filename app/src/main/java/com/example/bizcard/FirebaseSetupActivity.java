package com.example.bizcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * פרק 11 — מסך ההסבר שמחליף קריסה.
 *
 * כל מסך ענן בפרויקט בודק קודם אם FirebaseConfig מולא. אם לא — במקום
 * IllegalStateException ("Default FirebaseApp is not initialized") האפליקציה
 * פותחת את המסך הזה, שמסביר בדיוק מה חסר ואיפה משיגים את זה.
 */
public class FirebaseSetupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_setup);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // חוזרים לתפריט הראשי ומנקים את המסכים שמעליו
                Intent intent = new Intent(FirebaseSetupActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}
