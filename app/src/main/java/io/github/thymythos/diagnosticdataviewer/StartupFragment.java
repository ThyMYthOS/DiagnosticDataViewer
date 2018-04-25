package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StartupFragment extends Fragment {

    public static StartupFragment newInstance() {
        StartupFragment fragment = new StartupFragment();
        return fragment;
    }

    public StartupFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_startup, container, false);
    }
}
