package wbif.sjx.TrackAnalysis.GUI;

import ij.Prefs;
import ij.gui.Plot;
import wbif.sjx.common.Analysis.MSDCalculator;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.TrackCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Created by sc13967 on 25/06/2017.
 */
public class MSDControl extends ModuleControl {
    JCheckBox fitLineCheckbox;
    private JTextField nPointsTextField;

    MSDControl(TrackCollection tracks, int panelWidth, int elementHeight) {
        super(tracks, panelWidth, elementHeight);
    }

    @Override
    public String getTitle() {
        return "Mean squared displacement plot";
    }

    @Override
    public JPanel getExtraControls() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(0,5,0,5);

        // Checkbox for number of objects
        boolean fitLine = Prefs.get("TrackAnalysis.MSD.fitLine",true);
        fitLineCheckbox = new JCheckBox("Fit line to start");
        fitLineCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        fitLineCheckbox.setSelected(fitLine);
        panel.add(fitLineCheckbox,c);

        // Label and text field to get number of points to use for diffusion coefficient calculation
        JTextField label = new JTextField("Number of frames");
        label.setPreferredSize(new Dimension(3*panelWidth/4,elementHeight));
        label.setEditable(false);
        label.setBorder(null);
        label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        c.gridwidth = 1;
        c.gridy++;
        c.insets = new Insets(5,5,20,5);
        panel.add(label,c);

        int nPoints = (int) Prefs.get("TrackAnalysis.MSD.nPoints",10);
        nPointsTextField = new JTextField();
        nPointsTextField.setPreferredSize(new Dimension(panelWidth/4-5,elementHeight));
        nPointsTextField.setText(String.valueOf(nPoints));
        c.gridx++;
        c.insets = new Insets(5,0,20,5);
        panel.add(nPointsTextField,c);

        return panel;

    }

    @Override
    public void run(int ID) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        boolean fitLine = fitLineCheckbox.isSelected();
        Prefs.set("TrackAnalysis.MSD.fitLine",fitLine);

        int nPoints = (int) Math.round(Double.parseDouble(nPointsTextField.getText()));

        if (ID == -1) {
            double[][] msd = tracks.getAverageMSD(pixelDistances);
            double[] errMin = new double[msd[0].length];
            double[] errMax = new double[msd[0].length];

            for (int i=0;i<errMin.length;i++) {
                errMin[i] = msd[1][i] - msd[2][i];
                errMax[i] = msd[1][i] + msd[2][i];
            }

            String units = tracks.values().iterator().next().getUnits(pixelDistances);
            Plot plot = new Plot("Mean squared displacement (all tracks)","Interval (frames)","Mean squared displacement ("+units+"^2)");
            plot.setColor(Color.BLACK);
            plot.addPoints(msd[0],msd[1],Plot.LINE);
            plot.setColor(Color.RED);
            plot.addPoints(msd[0],errMin,Plot.LINE);
            plot.addPoints(msd[0],errMax,Plot.LINE);

            if (fitLine) {
                double[] fit = MSDCalculator.getLinearFit(msd[0], msd[1], nPoints);
                double[] x = new double[nPoints];
                double[] y = new double[nPoints];

                for (int i = 0; i < x.length; i++) {
                    x[i] = msd[0][i];
                    y[i] = fit[0] * msd[0][i] + fit[1];
                }
                plot.setColor(Color.CYAN);
                plot.addPoints(x, y, Plot.LINE);

            }

            plot.setLimitsToFit(true);
            plot.show();

        } else {
            Track track = tracks.get(ID);
            double[] f = track.getFAsDouble();
            CumStat[] cs = track.getMSD(pixelDistances);
            double[] msd = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();

            String units = track.getUnits(pixelDistances);
            Plot plot = new Plot("Mean squared displacement (track "+ID+")","Interval (frames)","Mean squared displacement ("+units+")",f,msd);
            plot.show();

        }
    }

    @Override
    public void extraActions(ActionEvent e) {

    }
}