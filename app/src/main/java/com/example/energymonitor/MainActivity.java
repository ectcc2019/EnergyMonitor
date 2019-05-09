package com.example.energymonitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.energymonitor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ImageButton menuConsumo, menuGastos, menuPerfil, menuAjuste, imagePower, imagePoweroff;
    private TextView user;
    private FirebaseAuth auth;
    private DatabaseReference dadosperfil;
    private FirebaseDatabase firebaseDatabase;
    private int stats = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dadosperfil = firebaseDatabase.getReference().child("usuarios").child(auth.getUid());


        menuAjuste =  findViewById(R.id.imageAjuste);
        menuConsumo = findViewById(R.id.imageConsumo);
        menuPerfil = findViewById(R.id.imagePerfil);
        menuGastos = findViewById(R.id.imageGastos);
        imagePower = findViewById(R.id.imagePower);
        imagePoweroff = findViewById(R.id.imagePowerOff);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        dadosperfil.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);
                user = findViewById(R.id.useremail);
                user.setText("Olá, " + userInformation.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        menuConsumo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MeuConsumoActivity.class));
            }
        });

        menuGastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GastosActivity.class));
            }
        });

        menuPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MeuPerfilActivity.class));
            }
        });

        menuAjuste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AjustesActivity.class));
            }
        });

        imagePower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference leitura = firebaseDatabase.getReference().child("medidores").child("configuracao");

                if (stats == 1) {
                    imagePower.setVisibility(View.GONE);
                    imagePoweroff.setVisibility(View.VISIBLE);
                    leitura.child("status").setValue(0);
                    stats = 0;
                } else {
                    imagePower.setVisibility(View.VISIBLE);
                    imagePoweroff.setVisibility(View.GONE);
                    leitura.child("status").setValue(1);
                    stats = 1;
                }
            }
        });

        imagePoweroff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference leitura = firebaseDatabase.getReference().child("medidores").child("configuracao");

                if (stats == 1) {
                    imagePower.setVisibility(View.GONE);
                    imagePoweroff.setVisibility(View.VISIBLE);
                    leitura.child("status").setValue(0);
                    stats = 0;
                } else {
                    imagePower.setVisibility(View.VISIBLE);
                    imagePoweroff.setVisibility(View.GONE);
                    leitura.child("status").setValue(1);
                    stats = 1;
                }
            }
        });

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
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {

                //setDataToView(editname.getText());

            }
        }
    };

}
