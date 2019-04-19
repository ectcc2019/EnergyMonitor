package com.example.energymonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GastosActivity extends AppCompatActivity {

    private TextView text_tarifa;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dadosperfil = firebaseDatabase.getReference(auth.getUid()).child("Gastos");

        text_tarifa = findViewById(R.id.texttarifa);

        dadosperfil.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GastosInformation gastosInformation = dataSnapshot.getValue(GastosInformation.class);

                double kwh = gastosInformation.getpreco();
                double icms = gastosInformation.getIcms();
                double cofins = gastosInformation.getCofins();
                double pis = gastosInformation.getPis();
                double tarifa;
                double consumo = 2.4;

                kwh = kwh/(icms+cofins+pis);
                tarifa = consumo*kwh;

                text_tarifa.setText(Double.toString(tarifa));

                GraphView graph = (GraphView) findViewById(R.id.graph);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                        new DataPoint(0, tarifa),
                        new DataPoint(1, tarifa),
                        new DataPoint(2, tarifa),
                        new DataPoint(3, tarifa),
                        new DataPoint(4, tarifa)
                });
                graph.addSeries(series);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
