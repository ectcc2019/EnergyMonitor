package com.example.energymonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MeuConsumoActivity extends AppCompatActivity {

    private TextView text_tarifa, textpotencia, textmedia, textprojecao;
    private Button btn_month, btn_week, btn_day;
    private GraphView graphView, graphWeek, graphDay;
    private LineGraphSeries series, seriesAC;
    private FirebaseDatabase database;
    SimpleDateFormat formatar = new SimpleDateFormat("dd/MM");
    SimpleDateFormat formatarday = new SimpleDateFormat("HH:mm");
    SimpleDateFormat formatarweek = new SimpleDateFormat("dd/MM HH");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumo);

        series = new LineGraphSeries(); //declara uma nova linha do gráfico
        seriesAC = new LineGraphSeries();

        database = FirebaseDatabase.getInstance();

        getSupportActionBar().setTitle("Meu consumo");

        //declaração dos campos da view
        text_tarifa = findViewById(R.id.textkwh);
        textmedia = findViewById(R.id.textmedia);
        textpotencia = findViewById(R.id.textpotencia);
        textprojecao = findViewById(R.id.textprojecao);
        graphView = findViewById(R.id.graph);
        graphWeek = findViewById(R.id.graphweek);
        graphDay = findViewById(R.id.graphday);
        btn_month = findViewById(R.id.btn_month);
        btn_week = findViewById(R.id.btn_week);
        btn_day = findViewById(R.id.btn_day);

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
        graphView.getScaleX();

        final GridLabelRenderer vg = graphView.getGridLabelRenderer();
        vg.setHorizontalLabelsAngle(135);
        vg.setVerticalAxisTitle("Kw/h");
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
        vpw.setScalable(true); //grafico escalavel
        vpw.setScalableY(true); //eixo Y escalavel
        vpw.setScrollable(false); //scroll desativado
        vpw.setScrollableY(true); //scroll eixo Y ativado
        vpw.setMinX(startweek()); //definição do eixo X minimo
        vpw.setMaxX(finishweek()); //definição do eixo X maximo

        final GridLabelRenderer vgw = graphWeek.getGridLabelRenderer();
        vgw.setHorizontalLabelsAngle(135);
        vgw.setVerticalAxisTitle("Kw/h");
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
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()]; //declaração do tamanho do vetor dos pontos
                DataPoint[] dpAC = new DataPoint[(int) dataSnapshot.getChildrenCount()];

                int index = 0;
                float total = 0, media = 0, potencia = 0, projecao = 0;

                for(DataSnapshot ds : dataSnapshot.getChildren() ){

                    String SV = ds.child("kw").getValue().toString();
                    Long DV = (Long) ds.child("timestamp").getValue();
                    float SensorValue = Float.parseFloat(SV);

                    total = SensorValue + total;
                    media = ((total / diaatual()) * diasrestantes()) + total;
                    potencia = (total * 1000);
                    projecao = total + (media * diasrestantes());

                    dp[index] = new DataPoint((new Date(DV * 1000L)), SensorValue); //declaração do novo ponto
                    dpAC[index] = new DataPoint((new Date(DV * 1000L)), total); //declaração do novo ponto

                    index++;
                }

                text_tarifa.setText("Total:\n" + String.format("%.2f", total) + " kWh");
                textmedia.setText("Média:\n" + String.format("%.2f",media) + " Kw/h" );
                textpotencia.setText("Potência:\n" + String.format("%.0f", potencia) + "W");
                textprojecao.setText("Projeção:\n" + String.format("%.2f", projecao) + " kWh");

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);
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
                float total = 0, media = 0, potencia = 0, projecao = 0;

                for(DataSnapshot ds : dataSnapshot.getChildren() ){

                    String SV = ds.child("kw").getValue().toString();
                    Long DV = (Long) ds.child("timestamp").getValue();
                    float SensorValue = Float.parseFloat(SV);

                    total = SensorValue + total;
                    media = ((total / diaatual()) * diasrestantes()) + total;
                    potencia = (total * 1000);
                    projecao = total + (media * diasrestantes());

                    dp[index] = new DataPoint((new Date(DV * 1000L)), SensorValue); //declaração do novo ponto
                    dpAC[index] = new DataPoint((new Date(DV * 1000L)), total); //declaração do novo ponto

                    index++;
                }

                text_tarifa.setText("Total:\n" + String.format("%.1f",total) + " kWh");
                textmedia.setText("Média:\n" + String.format("%.1f",media) + " Kw/h" );
                textpotencia.setText("Potência:\n" + String.format("%.1f", potencia) + "W");
                textprojecao.setText("Projeção:\n" + String.format("%.1f", projecao) + " kWh");

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);
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
                float total = 0, media = 0, potencia = 0, projecao = 0;

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String SV = ds.child("kw").getValue().toString();
                    Long DV = (Long) ds.child("timestamp").getValue();
                    float SensorValue = Float.parseFloat(SV);

                    total = SensorValue + total;
                    media = ((total / diaatual()) * diasrestantes()) + total;
                    potencia = (total * 1000);
                    projecao = total + (media * diasrestantes());

                    dp[index] = new DataPoint((new Date(DV * 1000L)), SensorValue); //declaração do novo ponto
                    dpAC[index] = new DataPoint((new Date(DV * 1000L)), total); //declaração do novo ponto

                    index++;
                }

                text_tarifa.setText("Total:\n" + String.format("%.1f",total) + " kWh");
                textmedia.setText("Média:\n" + String.format("%.1f", media) + " Kw/h");
                textpotencia.setText("Potência:\n" + String.format("%.1f", potencia) + "W");
                textprojecao.setText("Projeção:\n" + String.format("%.1f", projecao) + " kWh");

                series.resetData(dp); //inclui os pontos do vetor na linha do grafico
                seriesAC.resetData(dpAC);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
