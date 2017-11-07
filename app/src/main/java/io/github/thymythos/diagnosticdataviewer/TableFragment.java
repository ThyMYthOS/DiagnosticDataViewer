package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.graphics.Color;
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

        String[] rpm = { "0.9k", "1.2k", "1.8k", "2.5k", "3k", "3.5k", "4k", "4.5k", "5k", "6k", "7k", "8k", "9.5k", "11k", "11.8k", "12.5k" };
        String[] tps = { "1.8", "2.3", "2.5", "3.2", "4.2", "5.6", "7.1", "9.1", "12.1", "16.1", "21.2", "28.3", "37.3", "48.4", "60.6", "78.2" };

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
        TableLayout table = (TableLayout) view.findViewById(R.id.table);
        for (int row = 0; row < 16; row++) {
            TableRow tableRow = new TableRow(table.getContext());
            tableRow.setLayoutParams(rowLayout);

            // TPS column
            TextView text = new TextView(tableRow.getContext());
            text.setText(tps[row]);
            text.setLayoutParams(colLayout);
            text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            text.setPadding(15,5, 15, 5);
            tableRow.addView(text);

            // Main table
            for (int col = 0; col < 16; col++) {
                text = new TextView(tableRow.getContext());
                text.setBackground((col / (row + 1) > 1) ? gdGreen : gdRed);
                text.setLayoutParams(colLayout);
                text.setPadding(15,5, 15, 5);
                tableRow.addView(text);
            }

            table.addView(tableRow);
        }

        // RPM row
        TableRow row = new TableRow(table.getContext());
        row.setLayoutParams(rowLayout);
        TextView text = new TextView(row.getContext());
        text.setLayoutParams(colLayout);
        text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        text.setPadding(15,5, 15, 5);
        row.addView(text);
        for (int col = 0; col < 16; col++) {
            text = new TextView(row.getContext());
            text.setText(rpm[col]);
            text.setLayoutParams(colLayout);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setPadding(15,5, 15, 5);
            row.addView(text);
        }
        table.addView(row);

        return view;
    }

}
