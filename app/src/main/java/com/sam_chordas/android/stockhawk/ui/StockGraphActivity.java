package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by neko on 06/05/2016.
 */
public class StockGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final static String INTENT_STOCK_PARAM = "INTENT_STOCK_PARAM";

    private Context mContext;
    private String currentStock;
    private static final int CURSOR_LOADER_ID = 1;
    private LineChartView lineChartView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_line_graph);

        currentStock = getIntent().getStringExtra(StockGraphActivity.INTENT_STOCK_PARAM);

        if(currentStock==null){
            Toast toast =
                    Toast.makeText(StockGraphActivity.this, "Not selected stock!",
                            Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
            toast.show();
            finish();
        }

        lineChartView = (LineChartView) findViewById(R.id.linechart);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        setTitle(currentStock.toUpperCase());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.CREATED, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{currentStock},
                QuoteColumns.CREATED + " DESC LIMIT 100");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int qtyLabels = data.getCount()<5?data.getCount():5;

        String[] labels = new String[qtyLabels];
        float[] values = new float[qtyLabels];

        int i = data.getCount() - 1;
        int space = (int)Math.ceil(data.getCount()*1.0/qtyLabels);
        int aux2 = qtyLabels - 1;
        float minValue = -1;
        float maxValue = -1;
        if(data.moveToFirst()){
            do{
                long date = data.getLong(data.getColumnIndex(QuoteColumns.CREATED));
                String strValue = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
                float value = Float.valueOf(strValue);
                if(i%space==0){
                    labels[aux2] = Utils.extractDateLabel(date);
                    values[aux2] = value;
                    aux2--;
                }else if(i==0){
                    labels[0] = Utils.extractDateLabel(date);
                    values[0] = value;
                }
                if(minValue==-1){
                    minValue = value;
                }else if(minValue>value){
                    minValue = value;
                }
                if(maxValue==-1){
                    maxValue = value;
                }else if(maxValue<value){
                    maxValue = value;
                }
                i--;
            }while (data.moveToNext());
        }

        LineSet dataset = new LineSet(labels, values);
        dataset.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(4);
        lineChartView.addData(dataset);
        int step = (int) (maxValue - minValue) / 5;
        if(step==0){
            step = 1;
        }
        lineChartView.setAxisBorderValues((int)minValue - step, (int)maxValue + step);
        lineChartView.setStep(step);
        Animation anim = new Animation()
                .setEasing(new BounceEase());
        lineChartView.show(anim);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
