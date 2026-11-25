package com.example.bizcard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CardActivity extends AppCompatActivity {

    private String key, name, job, phone, site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        key = intent.getStringExtra(MainActivity.KEY_KEY);
        name = intent.getStringExtra(MainActivity.KEY_NAME);
        job = intent.getStringExtra(MainActivity.KEY_JOB);
        phone = intent.getStringExtra(MainActivity.KEY_PHONE);
        site = intent.getStringExtra(MainActivity.KEY_SITE);

        TextView tvName = findViewById(R.id.tvName);
        TextView tvJob = findViewById(R.id.tvJob);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvSite = findViewById(R.id.tvSite);

        tvName.setText(name != null ? name : "");
        tvJob.setText(job != null ? job : "");
        tvPhone.setText("טלפון: " + (phone != null ? phone : ""));
        tvSite.setText("אתר: " + (site != null ? site : ""));


        Button btnBack = findViewById(R.id.btnBack);


        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareCard();
            return true;
        } else if (id == R.id.action_call) {
            openDialer();
            return true;
        } else if (id == R.id.action_web) {
            openSite();
            return true;
        } else if (id == R.id.action_edit) {
            // שלב 6 (TODO 10): פתיחת MainActivity במצב עריכה וסגירת הכרטיס הנוכחי
            Intent editIntent = new Intent(this, MainActivity.class);
            editIntent.putExtra(MainActivity.KEY_KEY, key);
            editIntent.putExtra(MainActivity.KEY_NAME, name);
            editIntent.putExtra(MainActivity.KEY_JOB, job);
            editIntent.putExtra(MainActivity.KEY_PHONE, phone);
            editIntent.putExtra(MainActivity.KEY_SITE, site);
            startActivity(editIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialer() {
        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(this, "אין מספר טלפון בכרטיס", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        tryStart(dial);
    }

    private void openSite() {
        if (site == null || site.trim().isEmpty()) {
            Toast.makeText(this, "אין כתובת אתר בכרטיס", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = site.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        tryStart(web);
    }

    private void shareCard() {
        String text = name + "\n" + job + "\n" + phone + "\n" + site;

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);

        tryStart(Intent.createChooser(send, getString(R.string.share_title)));
    }

    private void tryStart(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_no_app, Toast.LENGTH_SHORT).show();
        }
    }
}
