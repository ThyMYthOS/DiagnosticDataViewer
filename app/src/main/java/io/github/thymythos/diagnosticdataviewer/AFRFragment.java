package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AFRFragment extends Fragment {

    public static AFRFragment newInstance() {
        AFRFragment fragment = new AFRFragment();
        return fragment;
    }

    public AFRFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_afr, container, false);
    }
}
