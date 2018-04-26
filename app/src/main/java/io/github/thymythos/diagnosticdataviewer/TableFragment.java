package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;

import static android.view.Gravity.CENTER_VERTICAL;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment implements LiveDataFragment {

    private final DecimalFormat NUM_FORMAT = new DecimalFormat("#0.0");
    private final int[] RPM_BINS = new int[]{0, 900, 1200, 1800, 2500, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9500, 11000, 11800, 12500, 13500};
    private final float[] TPS_BINS = new float[]{0, 1.8f, 2.3f, 2.5f, 3.3f, 4.2f, 5.6f, 7.1f, 9.1f, 12, 16, 21, 28, 37, 48, 61, 78, 141};

    private int[] maxRPM = new int[RPM_BINS.length - 2];
    private int[] minRPM = new int[RPM_BINS.length - 2];
    private float[] maxTPS = new float[TPS_BINS.length - 2];
    private float[] minTPS = new float[TPS_BINS.length - 2];
    private TextView[][] textViews = new TextView[RPM_BINS.length - 1][TPS_BINS.length - 1];

    private GradientDrawable gdGreen;
    private GradientDrawable gdRed;
    private float rpm = 0;

    public TableFragment() {
        // Required empty public constructor
    }

    @Override
    public void setRPM(float rpm) {
        this.rpm = rpm;
    }

    @Override
    public void setTPS(float tps) {
        int row = -1;
        int col = -1;
        for (int i = 0; i < maxRPM.length; i++) {
            if (minRPM[i] <= rpm && rpm <= maxRPM[i]) row = 16-i;
            if (minTPS[i] <= tps && tps <= maxTPS[i]) col = 15-i;
        }
        // TODO: Change color gradually when the cell is hit more than once
        if (row != -1 && col != -1) textViews[row][col].setBackground(gdGreen);
    }

    @Override
    public void setAFR1(float afr) {
    }

    @Override
    public void setAFR2(float afr) {
    }

    @Override
    public void setCoolantTemp(String temp) {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TableFragment.
     */
    public static TableFragment newInstance() {
        TableFragment fragment = new TableFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_table, container, false);

        float TPSclosed = 2.1f;

        for (int i = 1; i < RPM_BINS.length - 1; i++) {
            float minDiff = (TPS_BINS[i] - TPS_BINS[i - 1]) * 0.35f;
            minTPS[i-1] = TPS_BINS[i] - minDiff;
            float maxDiff = (TPS_BINS[i + 1] - TPS_BINS[i]) * 0.35f;
            maxTPS[i-1] = TPS_BINS[i] + maxDiff;

            minDiff = (float) (RPM_BINS[i] - RPM_BINS[i - 1]) * 0.35f;
            minRPM[i - 1] = (int) (RPM_BINS[i] - minDiff);
            maxDiff = (float) (RPM_BINS[i + 1] - RPM_BINS[i]) * 0.35f;
            maxRPM[i - 1] = (int) (RPM_BINS[i] + maxDiff);

        }
        minTPS[1] = TPSclosed * 0.9f;

        gdGreen = new GradientDrawable();
        gdGreen.setColor(0xFF00FF00);
        gdGreen.setCornerRadius(5);
        gdGreen.setStroke(1, 0xFF000000);
        gdRed = new GradientDrawable();
        gdRed.setColor(0xFFFF1010);
        gdRed.setCornerRadius(5);
        gdRed.setStroke(1, 0xFF000000);

        TableLayout.LayoutParams rowLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1f / 17f);
        TableRow.LayoutParams colLayout = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f / 17f);
        TableLayout table = view.findViewById(R.id.table);

        for (int row = 0; row < 17; row++) {
            TableRow tableRow = new TableRow(table.getContext());
            tableRow.setLayoutParams(rowLayout);
            tableRow.setGravity(CENTER_VERTICAL);

            // RPM column
            TextView text = new TextView(tableRow.getContext());
            text.setTextSize(10);
            text.setPadding(15, 0, 15, 0);

            if (row == 16) text.setText("");
            else text.setText(NUM_FORMAT.format(RPM_BINS[17 - row] / 1000f));

            tableRow.addView(text);

            // Main table and row index
            for (int col = 0; col < 16; col++) {
                textViews[row][col] = new TextView(tableRow.getContext());
                textViews[row][col].setLayoutParams(colLayout);
                if (row < 16) {
                    textViews[row][col].setBackground(gdRed);
                } else {
                    textViews[row][col].setText(NUM_FORMAT.format(TPS_BINS[16 - col]));
                    textViews[row][col].setGravity(Gravity.CENTER_HORIZONTAL);
                    if (col % 2 != 0) text.setText("  ");
                    textViews[row][col].setTextSize(10);
                }

                textViews[row][col].setPadding(0, 5, 25, 5);
                tableRow.addView(textViews[row][col]);
            }

            table.addView(tableRow);
        }

        return view;
    }

}

