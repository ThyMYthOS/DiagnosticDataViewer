package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoggerInfoFragment extends Fragment {

    public static LoggerInfoFragment newInstance() {
        LoggerInfoFragment fragment = new LoggerInfoFragment();
        return fragment;
    }

    public LoggerInfoFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loggerinfo, container, false);
    }
}
