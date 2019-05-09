package com.example.energymonitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MeuPerfilActivity extends AppCompatActivity {

    private EditText editEmail, newPassword, editname, edittelefone, editnascimento;
    private Button btnRemoveUser, btnsalvar, signOut;
    private TextView email;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meuperfil);

        getSupportActionBar().setTitle("Meu Perfil");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference dadosperfil = firebaseDatabase.getReference().child("usuarios").child(auth.getUid());

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MeuPerfilActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        email = findViewById(R.id.useremail);
        btnsalvar = findViewById(R.id.change_password_button);
        btnRemoveUser = findViewById(R.id.remove_user_button);
        signOut = findViewById(R.id.sign_out);

        editEmail = findViewById(R.id.old_email);
        newPassword = findViewById(R.id.newPassword);
        editname = findViewById(R.id.name);
        edittelefone = findViewById(R.id.telefone);
        editnascimento = findViewById(R.id.datanascimento);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        dadosperfil.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                assert userInformation != null;
                editname.setText(userInformation.getName());
                editnascimento.setText(userInformation.getDatanascimento());
                edittelefone.setText(userInformation.getTelefone());
                editEmail.setText(userInformation.getEmail());
                setDataToView(userInformation.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnsalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateProfile();
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Senha muito curta, mínimo 6 caracteres");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MeuPerfilActivity.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                                            //signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(MeuPerfilActivity.this, "Falha ao atualizar a senha!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }
        });


        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        removeProfile();
                                        Toast.makeText(MeuPerfilActivity.this, "Sua conta foi deletada :(", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MeuPerfilActivity.this, LoginActivity.class));
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(MeuPerfilActivity.this, "Falha ao deletar sua conta!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }

    private void setDataToView(String nome) {

        email.setText("Olá, " + nome);

    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(MeuPerfilActivity.this, LoginActivity.class));
                finish();
            } else {

                //setDataToView(editname.getText());

            }
        }
    };

    public void updateProfile() {

        String nome = editname.getText().toString().trim();
        String nascimento = editnascimento.getText().toString().trim();
        String telefone = edittelefone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        UserInformation userInformation = new UserInformation(nome, nascimento, telefone, email);

        final DatabaseReference dadosperfil = firebaseDatabase.getReference(auth.getUid());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        dadosperfil.setValue(userInformation);

        user.updateEmail(editEmail.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MeuPerfilActivity.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                            //signOut();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MeuPerfilActivity.this, "Falha ao atualizar o email!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

        progressBar.setVisibility(View.GONE);

    }

    public void removeProfile() {

        final DatabaseReference dadosperfil = firebaseDatabase.getReference(auth.getUid());
        dadosperfil.removeValue();

    }

    //sign out method
    public void signOut() {
        auth.signOut();

        // this listener will be called when there is change in firebase user session
        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MeuPerfilActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
