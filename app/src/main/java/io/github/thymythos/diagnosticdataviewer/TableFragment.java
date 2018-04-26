package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.content.res.TypedArray;
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
    public static final String ARG_RPM_MOT_ID = "ARG_RPM_MOT_ID";
    public static final String ARG_TPS_MOT_ID = "ARG_TPS_MOT_ID";

    private final DecimalFormat NUM_FORMAT = new DecimalFormat("#0.0");

    private int[] maxRPM = null;
    private int[] minRPM = null;
    private float[] maxTPS = null;
    private float[] minTPS = null;
    private TextView[][] textViews = null;

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
        int rpmMotId = getArguments().getInt(ARG_RPM_MOT_ID);
        int tpsMotId = getArguments().getInt(ARG_TPS_MOT_ID);
        TypedArray rpmBins = getResources().obtainTypedArray(rpmMotId);
        TypedArray tpsBins = getResources().obtainTypedArray(tpsMotId);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_table, container, false);

        float TPSclosed = 2.1f;

        maxRPM = new int[rpmBins.length() - 2];
        minRPM = new int[rpmBins.length() - 2];
        maxTPS = new float[tpsBins.length() - 2];
        minTPS = new float[tpsBins.length() - 2];
        textViews = new TextView[rpmBins.length() - 1][tpsBins.length() - 1];

        final float CONF_INTERVAL = 0.35f;
        for (int i = 1; i < rpmBins.length() - 1; i++) {
            float minDiff = (tpsBins.getFloat(i, 0) - tpsBins.getFloat(i - 1, 0)) * CONF_INTERVAL;
            minTPS[i-1] = tpsBins.getFloat(i, 0) - minDiff;
            float maxDiff = (tpsBins.getFloat(i + 1, 0) - tpsBins.getFloat(i, 0)) * CONF_INTERVAL;
            maxTPS[i-1] = tpsBins.getFloat(i, 0) + maxDiff;

            minDiff = (float) (rpmBins.getFloat(i, 0) - rpmBins.getFloat(i - 1, 0)) * CONF_INTERVAL;
            minRPM[i - 1] = (int) (rpmBins.getFloat(i, 0) - minDiff);
            maxDiff = (float) (rpmBins.getFloat(i + 1, 0) - rpmBins.getFloat(i, 0)) * CONF_INTERVAL;
            maxRPM[i - 1] = (int) (rpmBins.getFloat(i, 0) + maxDiff);

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
            else text.setText(NUM_FORMAT.format(rpmBins.getFloat(17 - row, 0) / 1000f));

            tableRow.addView(text);

            // Main table and row index
            for (int col = 0; col < 16; col++) {
                textViews[row][col] = new TextView(tableRow.getContext());
                textViews[row][col].setLayoutParams(colLayout);
                if (row < 16) {
                    textViews[row][col].setBackground(gdRed);
                } else {
                    textViews[row][col].setText(NUM_FORMAT.format(tpsBins.getFloat(16 - col, 0)));
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

