package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ActuatorTestFragment extends Fragment {

    public static ActuatorTestFragment newInstance() {
        ActuatorTestFragment fragment = new ActuatorTestFragment();
        return fragment;
    }

    public ActuatorTestFragment() {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actuatortest, container, false);
    }
}
