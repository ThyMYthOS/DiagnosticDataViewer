package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextDataItemFragment extends Fragment {

    public static TextDataItemFragment newInstance() {
        TextDataItemFragment fragment = new TextDataItemFragment();
        return fragment;
    }

    public TextDataItemFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_textdataitem, container, false);
    }
}
