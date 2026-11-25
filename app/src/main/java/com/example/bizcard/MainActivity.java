package com.example.bizcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_KEY = "key";
    public static final String KEY_NAME = "name";
    public static final String KEY_JOB = "job";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_SITE = "site";

    private EditText etName, etJob, etPhone, etSite;
    private Button btnCreate;
    private String editKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etJob = findViewById(R.id.etJob);
        etPhone = findViewById(R.id.etPhone);
        etSite = findViewById(R.id.etSite);

        btnCreate = findViewById(R.id.btnCreate);
        Button btnAbout = findViewById(R.id.btnAbout);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        // זיהוי מצב עריכה (שלב 6 - TODO 11)
        editKey = getIntent().getStringExtra(KEY_KEY);
        if (editKey != null) {
            etName.setText(getIntent().getStringExtra(KEY_NAME));
            etJob.setText(getIntent().getStringExtra(KEY_JOB));
            etPhone.setText(getIntent().getStringExtra(KEY_PHONE));
            etSite.setText(getIntent().getStringExtra(KEY_SITE));
            btnCreate.setText("שמור שינויים");
            setTitle("עריכת כרטיס");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_list) {
            startActivity(new Intent(this, CardsListActivity.class));
            return true;
        } else if (id == R.id.action_reset) {
            clearForm();
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCard() {
        String name  = etName.getText().toString().trim();
        String job   = etJob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String site  = etSite.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "חובה למלא שם", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!FirebaseConfig.isConfigured()) {
            Toast.makeText(this, "חסרים ערכים ב-FirebaseConfig.java", Toast.LENGTH_LONG).show();
            return;
        }

        final Card card = new Card(name, job, phone, site);
        DatabaseReference ref = CardsDb.cards(this);

        if (editKey == null) {
            // ===== הוספה (Create) — שלב 3 (TODO 1-3) =====
            String key = ref.push().getKey();
            if (key == null) {
                Toast.makeText(this, "שגיאה ביצירת מפתח בענן", Toast.LENGTH_SHORT).show();
                return;
            }
            card.setKey(key);
            btnCreate.setEnabled(false);

            ref.child(key).setValue(card)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            btnCreate.setEnabled(true);
                            Toast.makeText(MainActivity.this, "נשמר בענן", Toast.LENGTH_SHORT).show();
                            clearForm();
                            openCard(card);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btnCreate.setEnabled(true);
                            Toast.makeText(MainActivity.this, "השמירה נכשלה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // ===== עדכון (Update) — שלב 6 (TODO 12) =====
            card.setKey(editKey);
            btnCreate.setEnabled(false);

            ref.child(editKey).setValue(card)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            btnCreate.setEnabled(true);
                            Toast.makeText(MainActivity.this, "הכרטיס עודכן", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btnCreate.setEnabled(true);
                            Toast.makeText(MainActivity.this, "העדכון נכשל: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void openCard(Card c) {
        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra(KEY_KEY, c.getKey());
        intent.putExtra(KEY_NAME, c.getName());
        intent.putExtra(KEY_JOB, c.getJob());
        intent.putExtra(KEY_PHONE, c.getPhone());
        intent.putExtra(KEY_SITE, c.getSite());
        startActivity(intent);
    }

    private void clearForm() {
        etName.setText("");
        etJob.setText("");
        etPhone.setText("");
        etSite.setText("");
        etName.requestFocus();
    }
}
