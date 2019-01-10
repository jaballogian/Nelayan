package jaballogian.nelayan;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Set;

import me.anwarshahriar.calligrapher.Calligrapher;

public class SettingsActivity extends AppCompatActivity {

    private TextView logOut, semuaPengguna;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        Calligrapher calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "PRODUCT_SANS.ttf", true);

        logOut = (TextView) findViewById(R.id.logOutTextViewSettingsActivity);
        semuaPengguna = (TextView) findViewById(R.id.semuaPenggunaTextViewSettingsActivity);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();

                Intent toLogInActivity = new Intent (SettingsActivity.this, LogInActivity.class);
                startActivity(toLogInActivity);
                finish();
            }
        });

        semuaPengguna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toUsersRegisteredActivity = new Intent (SettingsActivity.this, UsersRegisteredActivity.class);
                startActivity(toUsersRegisteredActivity);
                finish();
            }
        });
    }
}
