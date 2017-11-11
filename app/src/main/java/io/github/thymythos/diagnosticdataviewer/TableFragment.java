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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment {

    public TableFragment() {
        // Required empty public constructor
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

        int[] rpm = {0, 900, 1200, 1800, 2500, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9500, 11000, 11800, 12500, 13500};
        double[] tps = {0, 1.8, 2.3, 2.5, 3.3, 4.2, 5.6, 7.1, 9.1, 12, 16, 21, 28, 37, 48, 61, 78, 100};

        double TPSclosed = 2.3;

        double[] TestData = {1250, 2.3, 1350, 2.3, 1300, 2.3, 1325, 2.3, 1450, 5, 1550, 5, 2000, 8.0, 2000, 8};
        int colCell;
        int rowCell;
        int RPM;
        double TPS;

        double[] maxTPS = new double[16];
        double[] minTPS = new double[16];
        int[] maxRPM = new int[16];
        int[] minRPM = new int[16];

        for (int i = 1; i < 16; i++) {
            double minDiff = tps[i] - tps[i - 1] * 0.35;
            minTPS[i - 1] = tps[i] - minDiff;
            double maxDiff = tps[i + 1] - tps[i] * 0.35;
            maxTPS[i - 1] = tps[i] + maxDiff;

            minDiff = (double) (rpm[i] - rpm[i - 1]) * 0.35;
            minRPM[i - 1] = (int) (rpm[i] - minDiff);
            maxDiff = (double) (rpm[i + 1] - rpm[i]) * 0.35;
            maxRPM[i - 1] = (int) (rpm[i] + maxDiff);

        }
        minTPS[2] = TPSclosed * 0.9;


        GradientDrawable gdGreen = new GradientDrawable();
        gdGreen.setColor(0xFF00FF00);
        gdGreen.setCornerRadius(5);
        gdGreen.setStroke(1, 0xFF000000);
        GradientDrawable gdRed = new GradientDrawable();
        gdRed.setColor(0xFFFF1010);
        gdRed.setCornerRadius(5);
        gdRed.setStroke(1, 0xFF000000);

        TableLayout.LayoutParams rowLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, 1f / 17f);
        TableRow.LayoutParams colLayout = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f / 17f);
        TableLayout table = view.findViewById(R.id.table);
        for (int row = 0; row < 16; row++) {
            TableRow tableRow = new TableRow(table.getContext());
            tableRow.setLayoutParams(rowLayout);

            // RPM column
            TextView text = new TextView(tableRow.getContext());
            text.setText(Double.toString(rpm[16 - row] / 1000));
            text.setLayoutParams(colLayout);
            text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            text.setPadding(15, 5, 15, 5);
            tableRow.addView(text);

            // Main table
            for (int col = 0; col < 16; col++) {
                text = new TextView(tableRow.getContext());
                //text.setBackground((col / (row + 1) > 1) ? gdGreen : gdRed);
                text.setBackground(gdRed);
                text.setLayoutParams(colLayout);
                text.setPadding(15, 5, 15, 5);
                tableRow.addView(text);
            }

            table.addView(tableRow);
        }

        // TPS row
        TableRow row = new TableRow(table.getContext());
        row.setLayoutParams(rowLayout);
        TextView text = new TextView(row.getContext());
        text.setLayoutParams(colLayout);
        text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        text.setPadding(15, 5, 15, 5);
        row.addView(text);
        for (int col = 0; col < 16; col++) {
            text = new TextView(row.getContext());
            text.setText(Double.toString(tps[16 - col]));
            text.setLayoutParams(colLayout);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setPadding(15, 5, 15, 5);
            row.addView(text);
        }
        table.addView(row);

        /////////////////////////////
        /// Simulate bluetooth data
        /////////////////////////////
        for (int i = 0; i < 15; i += 2) {
            colCell = 0;
            rowCell = 0;
            RPM = (int) (TestData[i]);
            TPS = TestData[i + 1];
            for (int ii = 0; ii < 16; ii++) {
                if (TPS >= minTPS[i] && TPS <= maxTPS[i]) colCell = i;
                if (RPM >= minRPM[i] && RPM <= maxRPM[i]) rowCell = i;
            }
            // TODO:
            // if rowCell > 0 then
            // change colour of cell (colCell rowCell) here from red to yellow to orange to green ???
            // delay 500ms so we can see it change during this test
        }

        return view;
    }

}
