package com.example.energymonitor;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GastosActivity extends AppCompatActivity {

    private TextView text_tarifa, textprojecao, text_semimposto;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GraphView graphView, graphDay, graphWeek;
    private Button btn_month, btn_week, btn_day;
    private LineGraphSeries series, seriesAC;
    private Double tarifa, kwh;
    SimpleDateFormat formatar = new SimpleDateFormat("dd/MM");
    SimpleDateFormat formatarday = new SimpleDateFormat("HH:mm");
    SimpleDateFormat formatarweek = new SimpleDateFormat("dd/MM HH");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos);

        getSupportActionBar().setTitle("Gastos");
        series = new LineGraphSeries(); //declara uma nova linha do gráfico
        seriesAC = new LineGraphSeries();

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference dadosperfil = database.getReference().child("usuarios").child(auth.getUid()).child("configuracoes");

        text_tarifa = findViewById(R.id.texttarifa);
        textprojecao = findViewById(R.id.text_projecao);
        text_semimposto = findViewById(R.id.textsemimposto);
        graphView = findViewById(R.id.graph);
        graphDay = findViewById(R.id.graphdia);
        graphWeek = findViewById(R.id.graphsemana);
        btn_month = findViewById(R.id.btn_mes);
        btn_week = findViewById(R.id.btn_semana);
        btn_day = findViewById(R.id.btn_dia);

        dadosperfil.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ConfigInformation ConfigInformation = dataSnapshot.getValue(ConfigInformation.class);

                double icms, cofins, pis;

                kwh = ConfigInformation.getpreco();
                icms = ConfigInformation.getIcms();
                cofins = ConfigInformation.getCofins();
                pis = ConfigInformation.getPis();

                tarifa = kwh /(1 - (icms + cofins + pis));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        leitura();

        graphView.addSeries(series); //adiciona a linha ao gráfico
        graphView.addSeries(seriesAC);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graphWeek.addSeries(series);
        graphWeek.addSeries(seriesAC);
        graphWeek.getLegendRenderer().setVisible(true);
        graphWeek.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graphDay.addSeries(series);
        graphDay.addSeries(seriesAC);
        graphDay.getLegendRenderer().setVisible(true);
        graphDay.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        series.setTitle("Real"); //define o titulo da serie
        series.setColor(Color.CYAN); // define a cor da serie
        series.setDrawDataPoints(true); //marcador dos pontos
        //series.setColor(Color.BLACK); //cor do marcador
        series.setDrawBackground(true); //inserir fundo
        series.setBackgroundColor(Color.CYAN); // pintar fundo

        seriesAC.setTitle("Acumulado");
        seriesAC.setColor(Color.MAGENTA);
        //seriesAC.setDrawBackground(true);
        //seriesAC.setBackgroundColor(Color.GREEN);

        final Viewport vp = graphView.getViewport();
        vp.setScalable(true);
        vp.setScalableY(true);
        vp.setScrollable(false);
        vp.setScrollableY(true);
        vp.setMinX(firstdayofmonth());
        vp.setMaxX(lastdayofmonth());

        final GridLabelRenderer vg = graphView.getGridLabelRenderer();
        vg.setHorizontalLabelsAngle(135);
        vg.setVerticalAxisTitle("Preço");
        vg.setHorizontalAxisTitle("Mês");
        vg.setHumanRounding(true); //arredondar eixo
        vp.setXAxisBoundsManual(true);
        vp.setYAxisBoundsManual(false);
        //vp.setMaxXAxisSize(1);
        //vp.setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        //vp.setYAxisBoundsStatus(Viewport.AxisBoundsStatus.INITIAL);
        vg.setNumHorizontalLabels(10); //numero de labels
        //vg.setLabelFormatter(new DateAsXAxisLabelFormatter(this));

        vg.setLabelFormatter(new DefaultLabelFormatter(){

            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX) {
                    return formatar.format(new Date((long) value)); //formatação do eixo X em data
                }
                return super.formatLabel(value, isValueX);
            }
        });

        final Viewport vpw = graphWeek.getViewport();
        vpw.setScalable(true);
        vpw.setScalableY(true);
        vpw.setScrollable(false);
        vpw.setScrollableY(true);
        vpw.setMinX(startweek());
        vpw.setMaxX(finishweek());

        final GridLabelRenderer vgw = graphWeek.getGridLabelRenderer();
        vgw.setHorizontalLabelsAngle(135);
        vgw.setVerticalAxisTitle("Preço");
        vgw.setHorizontalAxisTitle("Semana");
        vgw.setHumanRounding(true); //arredondar eixo
        vpw.setXAxisBoundsManual(true);
        vpw.setYAxisBoundsManual(false);
        //vp.setMaxXAxisSize(1);
        //vp.setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        //vp.setYAxisBoundsStatus(Viewport.AxisBoundsStatus.INITIAL);
        vgw.setNumHorizontalLabels(10); //numero de labels
        //vg.setLabelFormatter(new DateAsXAxisLabelFormatter(this));

        vgw.setLabelFormatter(new DefaultLabelFormatter(){

            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX) {
                    return formatarweek.format(new Date((long) value)); //formatação do eixo X em data
                }
                return super.formatLabel(value, isValueX);
            }
        });

        final Viewport vpd = graphDay.getViewport();
        vpd.setScalable(true);
        vpd.setScalableY(true);
        vpd.setScrollable(false);
        vpd.setScrollableY(true);
        vpd.setMinX(startday());
        vpd.setMaxX(endday());

        final GridLabelRenderer vgd = graphDay.getGridLabelRenderer();
        vgd.setHorizontalLabelsAngle(135);
        vgd.setVerticalAxisTitle("Kw/h");
        vgd.setHorizontalAxisTitle("Dia");
        vgd.setHumanRounding(true); //arredondar eixo
        vpd.setXAxisBoundsManual(true);
        vpd.setYAxisBoundsManual(false);
        //vp.setMaxXAxisSize(1);
        //vp.setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        //vp.setYAxisBoundsStatus(Viewport.AxisBoundsStatus.INITIAL);
        vgd.setNumHorizontalLabels(10); //numero de labels
        //vg.setLabelFormatter(new DateAsXAxisLabelFormatter(this));

        vgd.setLabelFormatter(new DefaultLabelFormatter(){

            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX) {
                    return formatarday.format(new Date((long) value)); //formatação do eixo X em data
                }
                return super.formatLabel(value, isValueX);
            }
        });

        btn_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leitura_day();
                graphDay.setVisibility(View.VISIBLE);
                graphView.setVisibility(View.GONE);
                graphWeek.setVisibility(View.GONE);
            }

        });

        btn_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leitura_week();
                graphWeek.setVisibility(View.VISIBLE);
                graphView.setVisibility(View.GONE);
                graphDay.setVisibility(View.GONE);
            }

        });

        btn_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leitura();
                graphView.setVisibility(View.VISIBLE);
                graphDay.setVisibility(View.GONE);
                graphWeek.setVisibility(View.GONE);
            }

        });

    }

    public long firstdayofmonth() {
        //função para pegar o primeiro dia do mÊs
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final long startEndDate = cal.getTimeInMillis();
        return  startEndDate;
    }

    public long lastdayofmonth(){
        //função para pegar o ultimo dia do mês
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        final long lastDayOfMonth = calendar.getTimeInMillis();
        return lastDayOfMonth;
    }

    public long startday(){
        // get today and clear time of day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        final long startday = cal.getTimeInMillis();
        return startday;
    }

    public long endday(){
        // get today and clear time of day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.add(Calendar.HOUR_OF_DAY, 23);
        cal.add(Calendar.MINUTE, 59);
        cal.add(Calendar.SECOND, 59);
        final long endday = cal.getTimeInMillis();
        return endday;
    }

    public long startweek(){
        // get today and clear time of day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        final long startweek = cal.getTimeInMillis();
        return startweek;
    }

    public long finishweek(){
        // get today and clear time of day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DATE, 6);
        final long finishweek = cal.getTimeInMillis();
        return finishweek;
    }

    public int diasrestantes() {

        //ultimo dia do mes
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // data de hoje e limpa a hora do dia
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        int startday = cal.get(Calendar.DAY_OF_MONTH);

        int restantes = lastDayOfMonth - startday;

        return restantes;
    }

    public int diaatual() {

        // get today and clear time of day
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        int diaatual = cal.get(Calendar.DAY_OF_MONTH);


        return diaatual;
    }

    public void leitura() {
        Query leitura = database.getReference().child("medidores").child("leituras").orderByChild("timestamp").startAt(firstdayofmonth()/1000).endAt(lastdayofmonth()/1000);
        leitura.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()];
                DataPoint[] dpAC = new DataPoint[(int) dataSnapshot.getChildrenCount()];

                int index = 0;
                double total =0, projecao = 0, semimposto = 0, totalsemimposto = 0;
                double bandeira_verde = 0.27776;

                for(DataSnapshot ds : dataSnapshot.getChildren() ){
                    String SV = ds.child("kw").getValue().toString();
                    float SensorValue = Float.parseFloat(SV);
                    Long DV = (Long) ds.child("timestamp").getValue();

                    double kwhpreco = (SensorValue * tarifa) + (SensorValue * bandeira_verde);
                    total = kwhpreco + total;
                    totalsemimposto = SensorValue + totalsemimposto;
                    semimposto = totalsemimposto * kwh;
                    projecao = ((total / diaatual()) * diasrestantes()) + total;

                    dp[index] = new DataPoint((DV * 1000L), kwhpreco);
                    dpAC[index] = new DataPoint((DV * 1000L), total);

                    index++;

                }

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);

                text_tarifa.setText("Preço Total:\nR$" + String.format ("%.2f", total));
                textprojecao.setText("Projeção:\nR$ " + String.format ("%.2f", projecao));
                text_semimposto.setText("Sem imposto:\nR$ " + String.format ("%.2f", semimposto));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void leitura_week() {
        Query leitura_week = database.getReference().child("medidores").child("leituras").orderByChild("timestamp").startAt(startday()/1000).endAt(endday()/1000);
        leitura_week.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()]; //declaração do tamanho do vetor dos pontos
                DataPoint[] dpAC = new DataPoint[(int) dataSnapshot.getChildrenCount()];

                int index = 0;
                double total =0, projecao = 0, semimposto = 0, totalsemimposto = 0;
                double bandeira_verde = 0.27776;

                for(DataSnapshot ds : dataSnapshot.getChildren() ){
                    String SV = ds.child("kw").getValue().toString();
                    float SensorValue = Float.parseFloat(SV);
                    Long DV = (Long) ds.child("timestamp").getValue();

                    double kwhpreco = (SensorValue * tarifa) + (SensorValue * bandeira_verde);
                    total = kwhpreco + total;
                    totalsemimposto = kwhpreco + totalsemimposto;
                    semimposto = totalsemimposto * kwh;
                    projecao = ((total / diaatual()) * diasrestantes()) + total;

                    dp[index] = new DataPoint((DV * 1000L), kwhpreco);
                    dpAC[index] = new DataPoint((DV * 1000L), total);

                    index++;

                }

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);

                text_tarifa.setText("Preço Total:\nR$" + String.format ("%.2f", total));
                textprojecao.setText("Projeção:\nR$ " + String.format ("%.2f", projecao));
                text_semimposto.setText("Sem imposto:\nR$ " + String.format ("%.2f", semimposto));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void leitura_day() {
        Query leitura_day = database.getReference().child("medidores").child("leituras").orderByChild("timestamp").startAt(startday() / 1000).endAt(endday() / 1000);
        leitura_day.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()]; //declaração do tamanho do vetor dos pontos
                DataPoint[] dpAC = new DataPoint[(int) dataSnapshot.getChildrenCount()];

                int index = 0;
                double total =0, projecao = 0, semimposto = 0, totalsemimposto = 0;
                double bandeira_verde = 0.27776;

                for(DataSnapshot ds : dataSnapshot.getChildren() ){
                    String SV = ds.child("kw").getValue().toString();
                    float SensorValue = Float.parseFloat(SV);
                    Long DV = (Long) ds.child("timestamp").getValue();

                    double kwhpreco = (SensorValue * tarifa) + (SensorValue * bandeira_verde);
                    total = kwhpreco + total;
                    totalsemimposto = kwhpreco + totalsemimposto;
                    semimposto = totalsemimposto * kwh;
                    projecao = ((total / diaatual()) * diasrestantes()) + total;

                    dp[index] = new DataPoint((DV * 1000L), kwhpreco);
                    dpAC[index] = new DataPoint((DV * 1000L), total);

                    index++;

                }

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);

                text_tarifa.setText("Preço Total:\nR$" + String.format ("%.2f", total));
                textprojecao.setText("Projeção:\nR$ " + String.format ("%.2f", projecao));
                text_semimposto.setText("Sem imposto:\nR$ " + String.format ("%.2f", semimposto));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
