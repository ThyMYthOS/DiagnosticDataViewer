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

        String[] rpm = { "12.5", "11.8", "11.0", "9.5", "8.0", "7.0", "6.0", "5.0", "4.5", "4.0", "3.5", "3.0", "2.5", "1.8", "1.2","0.9" };
        String[] tps = { "78","61", "48", "37", "28", "21", "16", "12", "9.1", "7.1", "5.6", "4.2", "3.2", "2.5", "2.3", "1.8"};

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
