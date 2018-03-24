package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FastLoggingFragment extends Fragment {

    public static FastLoggingFragment newInstance() {
        FastLoggingFragment fragment = new FastLoggingFragment();
        return fragment;
    }

    public FastLoggingFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fastlogging, container, false);
    }
}
