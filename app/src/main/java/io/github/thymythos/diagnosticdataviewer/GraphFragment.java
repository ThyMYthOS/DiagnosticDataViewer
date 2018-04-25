package io.github.thymythos.diagnosticdataviewer;

import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;


public class GraphFragment extends Fragment {

    private XYPlot plot;

    class MyRangeLabelRenderer extends XYGraphWidget.LineLabelRenderer {
        @Override
        public void drawLabel(Canvas canvas, XYGraphWidget.LineLabelStyle style,
                              Number val, float x, float y, boolean isOrigin) {
            if (val.doubleValue() % 2 == 1) {
                Number new_val = val.floatValue() + 2f;
                super.drawLabel(canvas, style, new_val, x + 100, y, isOrigin);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        // initialize our XYPlot reference:
        plot = (XYPlot) view.findViewById(R.id.plot);

        plot.setDomainBoundaries(0,16, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, 16, BoundaryMode.FIXED);
        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.BOTTOM, new MyRangeLabelRenderer());

        plot.setDomainStep(StepMode.SUBDIVIDE, 17);
        plot.setRangeStep(StepMode.SUBDIVIDE, 17);

        // create a couple arrays of y-values to plot:
        final Number[] domainLabels = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14};
        Number[] series1Numbers = {10, 11, 8, 13, 9, 10, 9, 12, 8, 11};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, null, null, null);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
//
//        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
//            @Override
//            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                int i = Math.round(((Number) obj).floatValue());
//                return toAppendTo.append(domainLabels[i]);
//            }
//            @Override
//            public Object parseObject(String source, ParsePosition pos) {
//                return null;
//            }
//        });
        return view;
    }
}
