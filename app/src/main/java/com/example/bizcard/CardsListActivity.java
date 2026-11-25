package com.example.bizcard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CardsListActivity extends AppCompatActivity {

    private ListView lvCards;
    private TextView tvCount;
    private ArrayList<Card> cards = new ArrayList<>();
    private ArrayAdapter<Card> adapter;
    private DatabaseReference ref;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);

        setTitle("הכרטיסים שלי");

        lvCards = findViewById(R.id.lvCards);
        tvCount = findViewById(R.id.tvCount);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cards);
        lvCards.setAdapter(adapter);

        if (!FirebaseConfig.isConfigured()) {
            tvCount.setText("חסרים ערכים ב-FirebaseConfig.java");
            return;
        }

        ref = CardsDb.cards(this);

        // ===== קריאה (Read) — שלב 4 (TODO 4-7) =====
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO 4: ריקון הרשימה למניעת כפילויות
                cards.clear();

                // TODO 5: מעבר על כל הכרטיסים בענן ושמירת המפתח בכל עצם
                for (DataSnapshot child : snapshot.getChildren()) {
                    Card card = child.getValue(Card.class);
                    if (card != null) {
                        card.setKey(child.getKey());
                        cards.add(card);
                    }
                }

                // TODO 6: עדכון המתאם ומונה הכרטיסים
                adapter.notifyDataSetChanged();
                if (cards.isEmpty()) {
                    tvCount.setText("אין כרטיסים בענן עדיין");
                } else {
                    tvCount.setText(cards.size() + " כרטיסים בענן");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CardsListActivity.this,
                        "הקריאה נכשלה: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };

        // TODO 7: הרשמה לעדכונים חיים
        ref.addValueEventListener(listener);

        // ===== שלב 5: לחיצה רגילה (פתיחת כרטיס) — TODO 9 =====
        lvCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Card selected = cards.get(position);
                Intent intent = new Intent(CardsListActivity.this, CardActivity.class);
                intent.putExtra(MainActivity.KEY_KEY, selected.getKey());
                intent.putExtra(MainActivity.KEY_NAME, selected.getName());
                intent.putExtra(MainActivity.KEY_JOB, selected.getJob());
                intent.putExtra(MainActivity.KEY_PHONE, selected.getPhone());
                intent.putExtra(MainActivity.KEY_SITE, selected.getSite());
                startActivity(intent);
            }
        });

        // ===== שלב 7: לחיצה ארוכה (מחיקה) — TODO 13 =====
        lvCards.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Card selected = cards.get(position);
                askDelete(selected);
                return true;
            }
        });
    }

    // ===== מחיקה (Delete) — שלב 7 (TODO 14) =====
    private void askDelete(final Card c) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת כרטיס")
                .setMessage("למחוק את הכרטיס של " + c.getName() + "?")
                .setPositiveButton("מחק", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (c.getKey() != null && ref != null) {
                            ref.child(c.getKey()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(CardsListActivity.this, "נמחק", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CardsListActivity.this,
                                                    "המחיקה נכשלה: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO 8: ביטול ההרשמה למאזין בעת סגירת המסך
        if (ref != null && listener != null) {
            ref.removeEventListener(listener);
        }
    }
}
