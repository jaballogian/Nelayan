package jaballogian.nelayan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.io.LineNumberReader;

import me.anwarshahriar.calligrapher.Calligrapher;

public class LogInActivity extends AppCompatActivity {

    private EditText masukanEmail, masukanPassword;
    private Button masuk;
    private TextView belumPunyaAkun;

    private ProgressDialog logInProgressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_log_in);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Calligrapher calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "PRODUCT_SANS.ttf", true);

        masukanEmail = (EditText) findViewById(R.id.masukanUsernameEditTextLogInActivity);
        masukanPassword = (EditText) findViewById(R.id.masukanPasswordEditTextLogInActivity);
        masuk = (Button) findViewById(R.id.masukButtonLogInActivity);
        belumPunyaAkun = (TextView) findViewById(R.id.belumPunyaAkunTextViewLogInActivity);

        logInProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = masukanEmail.getText().toString();
                String password = masukanPassword.getText().toString();

                logInProgressDialog.setTitle("Pengecekan Data");
                logInProgressDialog.setMessage("Mohon menunggu hingga proses selesai");
                logInProgressDialog.setCanceledOnTouchOutside(false);
                logInProgressDialog.show();

                if(email.isEmpty() ||  password.isEmpty()){

                    logInProgressDialog.hide();

                    Toast.makeText(LogInActivity.this, "Tidak boleh ada data yang kosong", Toast.LENGTH_LONG).show();
                }
                else {

                    logInUser(email, password);
                }
            }
        });

        belumPunyaAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRegisterActivity = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(toRegisterActivity);
                finish();
            }
        });

    }

    private void logInUser(String logInEmail, String logInPassword){

        mAuth.signInWithEmailAndPassword(logInEmail, logInPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    logInProgressDialog.dismiss();

                    Intent toMapsActivity = new Intent(LogInActivity.this, MapsActivity.class);
                    toMapsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(toMapsActivity);
                    finish();
                }
                else{

                    logInProgressDialog.hide();

                    Toast.makeText(LogInActivity.this, "Mohon maaf, Anda gagal masuk. Mohon coba lagi", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
