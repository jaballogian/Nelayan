package jaballogian.nelayan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import me.anwarshahriar.calligrapher.Calligrapher;

public class RegisterActivity extends AppCompatActivity {

    private CircleImageView profilPicture;
    private EditText masukanNama, masukanNamaKapal, masukanNomorHP, masukanEmail, masukanUsername, masukanPassword, masukanKonfirmasi;
    private Button daftarkanDiri;
    private CheckBox persetujuan;
    private TextView choosePhoto;

    private static final int GALLERY_PICK = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase, mDatabasePhoto;

    private ProgressDialog pembuatanProgressDialog, uploadFoto;

    private StorageReference mImageStorage;

    private String randomString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Calligrapher calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "PRODUCT_SANS.ttf", true);

        profilPicture = (CircleImageView) findViewById(R.id.profilPictureImageViewRegisterActivity);
        choosePhoto = (TextView) findViewById(R.id.pilihFotoTextViewRegisterActivity);

        masukanNama = (EditText) findViewById(R.id.masukanNamaEditTextRegisterActivity);
        masukanNamaKapal = (EditText) findViewById(R.id.masukanNamaKapalEditTextRegisterActivity);
        masukanNomorHP = (EditText) findViewById(R.id.masukanNomorHPEditTextRegisterActivity);
        masukanEmail = (EditText) findViewById(R.id.masukanEmailEditTextRegisterActivity);
        masukanUsername = (EditText) findViewById(R.id.masukanUsernameEditTextRegisterActivity);
        masukanPassword = (EditText) findViewById(R.id.masukanPasswordEditTextRegisterActivity);
        masukanKonfirmasi = (EditText) findViewById(R.id.masukanKonfirmasiPasswordEditTextRegisterActivity);
        daftarkanDiri = (Button) findViewById(R.id.daftarkanDiriButtonRegisterActivity);
        persetujuan = (CheckBox) findViewById(R.id.persetujuanCheckBoxRegisterActivity);

        randomString = random();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        pembuatanProgressDialog = new ProgressDialog(this);

        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Pilih Gambar sebagai Foto"), GALLERY_PICK);

                /**
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(RegisterActivity.this);
                 */
            }
        });

        daftarkanDiri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //convert all edittext to string
                String nama = masukanNama.getText().toString();
                String namaKapal = masukanNamaKapal.getText().toString();
                String nomorHP = masukanNomorHP.getText().toString();
                String email = masukanEmail.getText().toString();
                String username = masukanUsername.getText().toString();
                String password = masukanPassword.getText().toString();
                String konfirmasiPassword = masukanKonfirmasi.getText().toString();

                //check the checkbox
                if(persetujuan.isChecked()){

                    //check all edittext fields
                    if(nama.isEmpty() || namaKapal.isEmpty() || nomorHP.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty()){

                        Toast.makeText(RegisterActivity.this, "Tidak boleh ada data yang kosong", Toast.LENGTH_LONG).show();
                    }
                    else {

                        //check the password and konfirmasi edittext field
                        if(password.equals(konfirmasiPassword)){

                            //process the registration
                            registerUser(nama, namaKapal, nomorHP, email, username, password, konfirmasiPassword);

                            pembuatanProgressDialog.setTitle("Pendaftaran Sedang Diproses");
                            pembuatanProgressDialog.setMessage("Mohon menunggu hingga pendaftaran selesai");
                            pembuatanProgressDialog.setCanceledOnTouchOutside(false);
                            pembuatanProgressDialog.show();
                        }
                        else {

                            Toast.makeText(RegisterActivity.this, "Password dan Konfirmasi Password harus sama", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else{

                    Toast.makeText(RegisterActivity.this, "Anda harus menyetujui persyaratan", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void registerUser(final String registerNama, final String registerNamaKapal, final String registerNomorHP, final String registerEmail, final String registerUsername, final String registerPassword, final String registerKonfirmasiPassword){

        mAuth.createUserWithEmailAndPassword(registerEmail, registerPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    //save user data to databse
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uID = currentUser.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);

                    HashMap<String, String> identitasUser = new HashMap<>();
                    identitasUser.put("Nama", registerNama);
                    identitasUser.put("Nama Kapal", registerNamaKapal);
                    identitasUser.put("Nomor HP", registerNomorHP);
                    identitasUser.put("Email", registerEmail);
                    identitasUser.put("Username", registerUsername);
                    identitasUser.put("Password", registerPassword);
                    identitasUser.put("Konfirmasi Password", registerKonfirmasiPassword);
                    identitasUser.put("Foto Profil", randomString);

                    mDatabase.setValue(identitasUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                pembuatanProgressDialog.dismiss();

                                Intent toMapsActivity = new Intent(RegisterActivity.this, MapsActivity.class);
                                toMapsActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(toMapsActivity);
                                finish();
                            }
                        }
                    });
                }
                else {

                    pembuatanProgressDialog.hide();

                    Toast.makeText(RegisterActivity.this, "Maaf ada masalah pada proses registrasi, mohon coba lagi", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1).start(RegisterActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                uploadFoto = new ProgressDialog(RegisterActivity.this);
                uploadFoto.setTitle("Mengunggah foto");
                uploadFoto.setMessage("Mohon tunggu hingga foto selesai diunggah");
                uploadFoto.setCanceledOnTouchOutside(false);
                uploadFoto.show();

                Uri resultUri = result.getUri();

                StorageReference filepath = mImageStorage.child("Profile Pictures").child(randomString+ ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if ((task.isSuccessful())){

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            /**
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uID = currentUser.getUid();

                            mDatabasePhoto = FirebaseDatabase.getInstance().getReference().child("Users").child(uID).child("Foto Profil");
                             */

                            mDatabasePhoto = FirebaseDatabase.getInstance().getReference().child("Photos").child("Foto Profil").child(randomString);

                            HashMap<String, String> lokasiFoto = new HashMap<>();
                            lokasiFoto.put("Lokasi Foto", downloadUrl);

                            mDatabasePhoto.setValue(lokasiFoto).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        uploadFoto.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Sukses mengunggah foto", Toast.LENGTH_LONG).show();

                                        Picasso.with(RegisterActivity.this).load(downloadUrl).into(profilPicture);
                                    }
                                }
                            });

                        }
                        else {
                            uploadFoto.dismiss();
                            Toast.makeText(RegisterActivity.this, "Mohon maaf terjadi kesalahan, mohon untuk mencoba lagi", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
