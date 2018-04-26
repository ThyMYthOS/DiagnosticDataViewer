package io.github.thymythos.diagnosticdataviewer;

public interface LiveDataFragment {

    public void setRPM(float rpm);

    public void setTPS(float tps);

    public void setAFR1(float afr);

    public void setAFR2(float afr);

    public void setCoolantTemp(String temp);

}
