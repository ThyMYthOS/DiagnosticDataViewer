package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class IdleFragment extends Fragment {

    public static IdleFragment newInstance() {
        IdleFragment fragment = new IdleFragment();
        return fragment;
    }

    public IdleFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_idle, container, false);
    }
}
