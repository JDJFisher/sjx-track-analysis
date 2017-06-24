package wbif.sjx.TrackAnalysis.GUI;

import ij.Prefs;
import ij.gui.Plot;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.TrackCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by sc13967 on 24/06/2017.
 */
public class TotalPathLengthControl extends ModuleControl {
    public TotalPathLengthControl(TrackCollection tracks, int panelWidth, int elementHeight) {
        super(tracks, panelWidth, elementHeight);
    }

    @Override
    public String getTitle() {
        return "Total path length";
    }

    @Override
    public JPanel getExtraControls() {
        return null;

    }

    @Override
    public void run(int ID) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        if (ID == -1) {
            double[][] totalPathLength = tracks.getAverageTotalPathLength(pixelDistances);
            double[] f = new double[totalPathLength[0].length];
            double[] errMin = new double[totalPathLength[0].length];
            double[] errMax = new double[totalPathLength[0].length];

            for (int i=0;i<f.length;i++) {
                f[i] = i;
                errMin[i] = totalPathLength[0][i] - totalPathLength[1][i];
                errMax[i] = totalPathLength[0][i] + totalPathLength[1][i];
            }

            String units = tracks.values().iterator().next().getUnits(pixelDistances);
            Plot plot = new Plot("Total path length (all tracks)","Time relative to start of track (frames)","Total path length ("+units+")");
            plot.setColor(Color.BLACK);
            plot.addPoints(f,totalPathLength[0],Plot.LINE);
            plot.setColor(Color.RED);
            plot.addPoints(f,errMin,Plot.LINE);
            plot.addPoints(f,errMax,Plot.LINE);
            plot.setLimitsToFit(true);
            plot.show();

        } else {
            Track track = tracks.get(ID);
            double[] f = track.getFAsDouble();
            double[] totalPathLength = track.getRollingTotalPathLength(pixelDistances);

            String units = track.getUnits(pixelDistances);
            Plot plot = new Plot("Total path length (track "+ID+")","Time relative to start of track (frames)","Total path length ("+units+")",f,totalPathLength);
            plot.show();

        }
    }

    @Override
    public void extraActions(ActionEvent e) {

    }
}
