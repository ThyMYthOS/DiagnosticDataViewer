package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.nitri.gauge.Gauge;

public class AFRFragment extends Fragment implements LiveDataFragment {

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

    @Override
    public void setRPM(float rpm) {
    }

    @Override
    public void setTPS(float tps) {
    }

    @Override
    public void setAFR1(float afr) {
        if (afr <= 0) return;
        Gauge gauge = getView().findViewById(R.id.gaugeAFR1);
        gauge.setDeltaTimeInterval(1);
        gauge.setLowerText(String.valueOf(afr));
        if (afr < 11) afr = 11;
        if (afr > 15) afr = 15;
        gauge.moveToValue(afr);
    }

    @Override
    public void setAFR2(float afr) {
        if (afr <= 0) return;
        Gauge gauge = getView().findViewById(R.id.gaugeAFR2);
        gauge.setDeltaTimeInterval(1);
        gauge.setLowerText(String.valueOf(afr));
        if (afr < 11) afr = 11;
        if (afr > 15) afr = 15;
        gauge.moveToValue(afr);
    }

    @Override
    public void setCoolantTemp(String temp) {
    }
}
