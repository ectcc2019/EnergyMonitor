package com.example.energymonitor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AjustesActivity extends AppCompatActivity {

    private EditText precokwh, icms, cofins, pis;
    private Button btnsalvar;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dadosperfil = firebaseDatabase.getReference(auth.getUid()).child("Gastos");

        precokwh = findViewById(R.id.editpreco);
        icms = findViewById(R.id.editicms);
        pis = findViewById(R.id.editpis);
        cofins = findViewById(R.id.editcofins);
        btnsalvar = findViewById(R.id.btn_ajuste_salvar);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        dadosperfil.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GastosInformation gastosInformation = dataSnapshot.getValue(GastosInformation.class);

                //DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols());

                precokwh.setText(Double.toString(gastosInformation.getpreco()));
                icms.setText(Double.toString(gastosInformation.getIcms()));
                cofins.setText(Double.toString(gastosInformation.getCofins()));
                pis.setText(Double.toString(gastosInformation.getPis()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btnsalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                double kwh_update = Double.parseDouble(precokwh.getText().toString().trim());
                double icms_update = Double.parseDouble(icms.getText().toString().trim());
                double cofins_update = Double.parseDouble(cofins.getText().toString().trim());
                double pis_update = Double.parseDouble(pis.getText().toString().trim());

                GastosInformation gastosInformation = new GastosInformation(kwh_update, icms_update, pis_update, cofins_update);

                DatabaseReference dadosperfil = firebaseDatabase.getReference(auth.getUid());

                dadosperfil.child("Gastos").setValue(gastosInformation);

                Toast.makeText(AjustesActivity.this, "Atualizado!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
