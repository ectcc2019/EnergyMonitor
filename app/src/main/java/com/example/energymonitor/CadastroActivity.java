package com.example.energymonitor;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputName, inputDataNascimento, inputTelefone;     //hit option + enter if you on mac , for windows hit ctrl + enter
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference, databaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Inicializando firebase auth objeto
        auth = FirebaseAuth.getInstance();
        //FirebaseUser user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputName = findViewById(R.id.name);
        inputDataNascimento = findViewById(R.id.datanascimento);
        inputTelefone = findViewById(R.id.telefone);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CadastroActivity.this, ResetSenhaActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Coloque o email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Coloque a senha!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Senha muito curta, no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(CadastroActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(auth.getCurrentUser() != null) {
                                    String name = inputName.getText().toString().trim();
                                    String datanascimento = inputDataNascimento.getText().toString().trim();
                                    String telefone = inputTelefone.getText().toString().trim();
                                    String email = inputEmail.getText().toString().trim();

                                    UserInformation userInformation = new UserInformation(name, datanascimento, telefone, email);
                                    GastosInformation gastosInformation = new GastosInformation(0.17759, 0.25, 0.0097, 0.0451);

                                    FirebaseUser user = auth.getCurrentUser();

                                    databaseReference.child(user.getUid()).setValue(userInformation);

                                    databaseUser = FirebaseDatabase.getInstance().getReference(user.getUid());

                                    databaseUser.child("Leitura").setValue(1);
                                    databaseUser.child("Gastos").setValue(gastosInformation);


                                    Toast.makeText(CadastroActivity.this, "Usuário criado com sucesso" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                }
                                //Toast.makeText(CadastroActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(CadastroActivity.this, "Falha na autenticação " + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}

