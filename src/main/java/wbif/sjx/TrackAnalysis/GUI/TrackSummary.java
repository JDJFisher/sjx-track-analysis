package wbif.sjx.TrackAnalysis.GUI;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.ResultsTable;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.lwjgl.system.CallbackI;
import wbif.sjx.common.Analysis.MSDCalculator;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Timepoint;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.TrackCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sc13967 on 24/06/2017.
 */
public class TrackSummary extends ModuleControl {
    private JCheckBox frameByFrameCheckbox;
    private JCheckBox numberOfObjectsCheckbox;
    private JCheckBox trackDurationCheckbox;
    private JCheckBox spatialStatisticsCheckbox;
    private JCheckBox diffusionCoefficientCheckbox;
    private JTextField nPointsTextField;

    public TrackSummary(TrackCollection tracks, ImagePlus ipl, int panelWidth, int elementHeight) {
        super(tracks, ipl, panelWidth, elementHeight);
    }

    private void showTrackSummary(ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        DecimalFormat df = new DecimalFormat("#.###");
        String units = tracks.values().iterator().next().getUnits(pixelDistances);

        int i=0;
        for (Track track:tracks.values()) {
            double[][] meanPos = track.getMeanPosition(pixelDistances);

            rt.setValue("Mean x-pos ("+units+")", i, df.format(meanPos[0][0])); //Distances reported in image units
            rt.setValue("Mean y-pos ("+units+")", i, df.format(meanPos[0][1])); //Distances reported in image units
            rt.setValue("Mean z-pos ("+units+")", i, df.format(meanPos[0][2])); //Distances reported in image units
            rt.setValue("Stdev x-pos ("+units+")", i, df.format(meanPos[1][0])); //Distances reported in image units
            rt.setValue("Stdev y-pos ("+units+")", i, df.format(meanPos[1][1])); //Distances reported in image units
            rt.setValue("Stdev z-pos ("+units+")", i++, df.format(meanPos[1][2])); //Distances reported in image units

        }
    }

    private void showTrackSummary(int ID, ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        Track track = tracks.get(ID);

        DecimalFormat df = new DecimalFormat("#.###");
        String units = track.getUnits(pixelDistances);

        double[][] meanPos = track.getMeanPosition(pixelDistances);

        rt.setValue("Mean x-pos ("+units+")", 0, df.format(meanPos[0][0])); //Distances reported in image units
        rt.setValue("Mean y-pos ("+units+")", 0, df.format(meanPos[0][1])); //Distances reported in image units
        rt.setValue("Mean z-pos ("+units+")", 0, df.format(meanPos[0][2])); //Distances reported in image units
        rt.setValue("Stdev x-pos ("+units+")", 0, df.format(meanPos[1][0])); //Distances reported in image units
        rt.setValue("Stdev y-pos ("+units+")", 0, df.format(meanPos[1][1])); //Distances reported in image units
        rt.setValue("Stdev z-pos ("+units+")", 0, df.format(meanPos[1][2])); //Distances reported in image units

    }

    private void showFullTrackResults(int ID, ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);
        String units = tracks.values().iterator().next().getUnits(pixelDistances);

        Set<Integer> IDs = new TreeSet<>();
        if (ID == -1) {
            IDs = tracks.keySet();
        } else {
            IDs.add(ID);
        }

        int i = 0;
        for (int currentID:IDs) {
            Track track = tracks.get(currentID);

            TreeMap<Integer,double[]> nnDistances = track.getNearestNeighbourDistance(tracks,pixelDistances);
            TreeMap<Integer,Double> rollingTotalPath = track.getRollingTotalPathLength(pixelDistances);
            TreeMap<Integer,Double> rollingEuclideanDistance = track.getRollingEuclideanDistance(pixelDistances);
            TreeMap<Integer,Double> rollingDirectionalityRatio= track.getRollingDirectionalityRatio(pixelDistances);
            TreeMap<Integer,Double> instantaneousVelocity = track.getInstantaneousVelocity(pixelDistances);

            for (int f:track.getF()){
                rt.setValue("ID", i, currentID);
                rt.setValue("X (" + units + ")", i, track.getX(f,pixelDistances));
                rt.setValue("Y (" + units + ")", i, track.getY(f,pixelDistances));
                rt.setValue("Z (" + units + ")", i, track.getZ(f,pixelDistances));
                rt.setValue("NN ID", i, nnDistances.get(f)[0]);
                rt.setValue("NN distance (" + units + ")", i, nnDistances.get(f)[1]);
                rt.setValue("Rolling path length (" + units + ")", i, rollingTotalPath.get(f));
                rt.setValue("Rolling Euclidean distance (" + units + ")", i, rollingEuclideanDistance.get(f));
                rt.setValue("Rolling directionality ratio", i, rollingDirectionalityRatio.get(f));
                rt.setValue("Instantaneous velocity ("+units+"/frame)", i, instantaneousVelocity.get(f));

                i++;

            }
        }
    }

    private void showNumberOfObjects() {
        int[][] frameAndNumber = tracks.getNumberOfObjects(false);
        DecimalFormat df = new DecimalFormat("#.###");

        IJ.log("Number of objects (total): "+String.valueOf(tracks.size()));

        CumStat cs = new CumStat();
        for (int n:frameAndNumber[1]) {
            cs.addMeasure(n);
        }

        IJ.log("Mean number of objects per frame: "+String.valueOf(df.format(cs.getMean())));
        IJ.log("Std. dev. number of objects per frame: "+String.valueOf(df.format(cs.getStd())));

        IJ.log(" ");

    }

    private void showAllTracksDuration(ResultsTable rt) {
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat dfS = new DecimalFormat("#");
        CumStat cs = new CumStat();

        int i = 0;
        for (Track track:tracks.values()) {
            int[] f = track.getF();
            rt.setValue("Start (frame)", i, dfS.format(f[0]));
            rt.setValue("End (frame)", i, dfS.format(f[f.length-1]));
            rt.setValue("Duration (frames)",i++,track.getDuration());

            cs.addMeasure(track.getDuration());
        }

        IJ.log("Mean track duration: "+String.valueOf(df.format(cs.getMean()))+" frames");
        IJ.log("Std. dev. of track duration: "+String.valueOf(df.format(cs.getStd()))+" frames");

        IJ.log(" ");

    }

    private void showTrackDuration(int ID, ResultsTable rt) {
        DecimalFormat df = new DecimalFormat("#.###");
        Track track = tracks.get(ID);

        rt.setValue("Duration (frames)",0,track.getDuration());

        IJ.log("Track duration: "+String.valueOf(df.format(track.getDuration()))+" frames");
        IJ.log(" ");

    }

    private void showAllTracksSpatialStatistics(ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);
        DecimalFormat df = new DecimalFormat("#.###");
        String units = tracks.values().iterator().next().getUnits(pixelDistances);

        // Using CumStats to keep memory requirements smaller
        CumStat csStep = new CumStat();
        CumStat csVelocity = new CumStat();
        CumStat csEuclDist = new CumStat();
        CumStat csTotalDist = new CumStat();
        CumStat csInstDirRatio = new CumStat();
        CumStat csDirRatio = new CumStat();

        int i = 0;
        for (Track track:tracks.values()) {
            rt.setValue("Euclidean distance ("+units+")",i,track.getEuclideanDistance(pixelDistances));
            rt.setValue("Total path length ("+units+")",i,track.getTotalPathLength(pixelDistances));
            rt.setValue("Directionality ratio ",i,track.getDirectionalityRatio(pixelDistances));

            // Whole track statistics
            csEuclDist.addMeasure(track.getEuclideanDistance(pixelDistances));
            csTotalDist.addMeasure(track.getTotalPathLength(pixelDistances));
            csDirRatio.addMeasure(track.getDirectionalityRatio(pixelDistances));

            // Step-by-step statistics
            TreeMap<Integer,Double> steps = track.getInstantaneousStepSizes(pixelDistances);
            steps.values().stream().forEach(csStep::addMeasure);
            rt.setValue("Mean step size ("+units+")",i,new CumStat(steps.values()).getMean());

            TreeMap<Integer,Double> velocities = track.getInstantaneousVelocity(pixelDistances);
            velocities.values().stream().forEach(csVelocity::addMeasure);
            rt.setValue("Mean inst. vel. ("+units+"/frame)",i,new CumStat(velocities.values()).getMean());

            TreeMap<Integer,Double> dirRatio = track.getRollingDirectionalityRatio(pixelDistances);
            dirRatio.values().stream().forEach(csInstDirRatio::addMeasure);
            rt.setValue("Mean inst. dir. ratio",i++,new CumStat(dirRatio.values()).getMean());

        }

        IJ.log("Mean Euclidean distance: "+String.valueOf(df.format(csEuclDist.getMean()))+" "+units);
        IJ.log("Std. dev. of Euclidean distances: "+String.valueOf(df.format(csEuclDist.getStd()))+" "+units);
        IJ.log("Maximum Euclidean distance: "+String.valueOf(df.format(csEuclDist.getMax()))+" "+units);
        IJ.log("Minimum Euclidean distance: "+String.valueOf(df.format(csEuclDist.getMin()))+" "+units);
        IJ.log(" ");

        IJ.log("Mean total path length: "+String.valueOf(df.format(csTotalDist.getMean()))+" "+units);
        IJ.log("Std. dev. of total path lengths: "+String.valueOf(df.format(csTotalDist.getStd()))+" "+units);
        IJ.log("Maximum total path length: "+String.valueOf(df.format(csTotalDist.getMax()))+" "+units);
        IJ.log("Minimum total path length: "+String.valueOf(df.format(csTotalDist.getMin()))+" "+units);
        IJ.log(" ");

        IJ.log("Mean directionality ratio: "+String.valueOf(df.format(csDirRatio.getMean())));
        IJ.log("Std. dev. of directionality ratios: "+String.valueOf(df.format(csDirRatio.getStd())));
        IJ.log("Maximum directionality ratio: "+String.valueOf(df.format(csDirRatio.getMax())));
        IJ.log("Minimum directionality ratio: "+String.valueOf(df.format(csDirRatio.getMin())));
        IJ.log(" ");

        IJ.log("Mean step length: "+String.valueOf(df.format(csStep.getMean()))+" "+units);
        IJ.log("Std. dev. of step lengths: "+String.valueOf(df.format(csStep.getStd()))+" "+units);
        IJ.log("Maximum step length: "+String.valueOf(df.format(csStep.getMax()))+" "+units);
        IJ.log("Minimum step length: "+String.valueOf(df.format(csStep.getMin()))+" "+units);
        IJ.log(" ");

        IJ.log("Mean instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMean()))+" "+units+"/frame");
        IJ.log("Std. dev. of instantaneous velocities: "+String.valueOf(df.format(csVelocity.getStd()))+" "+units+"/frame");
        IJ.log("Maximum instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMax()))+" "+units+"/frame");
        IJ.log("Minimum instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMin()))+" "+units+"/frame");
        IJ.log(" ");

        IJ.log("Mean instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMean())));
        IJ.log("Std. dev. of instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getStd())));
        IJ.log("Maximum instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMax())));
        IJ.log("Minimum instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMin())));
        IJ.log(" ");

    }

    private void showTrackSpatialStatistics(int ID, ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        Track track = tracks.get(ID);
        DecimalFormat df = new DecimalFormat("#.###");
        String units = track.getUnits(pixelDistances);

        // Using CumStats to keep memory requirements smaller
        CumStat csStep = new CumStat();
        CumStat csVelocity = new CumStat();
        CumStat csInstDirRatio = new CumStat();

        // Step-by-step statistics
        TreeMap<Integer,Double> steps = track.getInstantaneousStepSizes(pixelDistances);
        steps.values().stream().forEach(csStep::addMeasure);

        TreeMap<Integer,Double> velocities = track.getInstantaneousVelocity(pixelDistances);
        velocities.values().stream().forEach(csVelocity::addMeasure);

        TreeMap<Integer,Double> dirRatio = track.getRollingDirectionalityRatio(pixelDistances);
        dirRatio.values().stream().forEach(csInstDirRatio::addMeasure);

        rt.setValue("Euclidean distance ("+units+")",0,track.getEuclideanDistance(pixelDistances));
        rt.setValue("Total path length ("+units+")",0,track.getTotalPathLength(pixelDistances));
        rt.setValue("Directionality ratio ",0,track.getDirectionalityRatio(pixelDistances));
        rt.setValue("Mean step size ("+units+")",0,new CumStat(steps.values()).getMean());
        rt.setValue("Mean inst. vel. ("+units+"/frame)",0,new CumStat(velocities.values()).getMean());
        rt.setValue("Mean inst. dir. ratio",0,new CumStat(dirRatio.values()).getMean());

        IJ.log("Euclidean distance: "+String.valueOf(df.format(track.getEuclideanDistance(pixelDistances)))+" "+units);
        IJ.log("Total path length: "+String.valueOf(df.format(track.getTotalPathLength(pixelDistances)))+" "+units);
        IJ.log("Directionality ratio: "+String.valueOf(df.format(track.getDirectionalityRatio(pixelDistances))));
        IJ.log(" ");

        IJ.log("Mean step length: "+String.valueOf(df.format(csStep.getMean()))+" "+units);
        IJ.log("Std. dev. of step lengths: "+String.valueOf(df.format(csStep.getStd()))+" "+units);
        IJ.log("Maximum step length: "+String.valueOf(df.format(csStep.getMax()))+" "+units);
        IJ.log("Minimum step length: "+String.valueOf(df.format(csStep.getMin()))+" "+units);
        IJ.log(" ");

        IJ.log("Mean instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMean()))+" "+units+"/frame");
        IJ.log("Std. dev. of instantaneous velocities: "+String.valueOf(df.format(csVelocity.getStd()))+" "+units+"/frame");
        IJ.log("Maximum instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMax()))+" "+units+"/frame");
        IJ.log("Minimum instantaneous velocity: "+String.valueOf(df.format(csVelocity.getMin()))+" "+units+"/frame");
        IJ.log(" ");

        IJ.log("Mean instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMean())));
        IJ.log("Std. dev. of instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getStd())));
        IJ.log("Maximum instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMax())));
        IJ.log("Minimum instantaneous directionality ratio: "+String.valueOf(df.format(csInstDirRatio.getMin())));
        IJ.log(" ");

    }

    private void showDiffusionCoefficient(ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        DecimalFormat df = new DecimalFormat("0.000E0");
        DecimalFormat dfS = new DecimalFormat("#");
        String units = tracks.values().iterator().next().getUnits(pixelDistances);

        int nPoints = (int) Math.round(Double.parseDouble(nPointsTextField.getText()));

        double[][] dfAndMSD = tracks.getAverageMSD(pixelDistances);
        double[] linearFit = MSDCalculator.getLinearFit(dfAndMSD[0],dfAndMSD[1],nPoints);

        IJ.log("Diffusion coefficient (single average MSD): "+String.valueOf(df.format(linearFit[0]/4))+" "+units+"^2/frame");
        IJ.log("Least squares gradient (single average MSD): "+String.valueOf(df.format(linearFit[0]))+" "+units+"^2/frame");
        IJ.log("Least squares intercept (single average MSD): "+String.valueOf(df.format(linearFit[1]))+" "+units+"^2");
        IJ.log("Calculated over "+String.valueOf(dfS.format(linearFit[2]))+" frames");
        IJ.log(" ");

        int i = 0;
        for (Track track:tracks.values()) {
            double[] fit = track.getMSDLinearFit(pixelDistances,nPoints);
            rt.setValue("Diffusion coefficient ("+units+"^2/frame)",i++,df.format(fit[0]/4));
        }
    }

    private void showDiffusionCoefficient(int ID, ResultsTable rt) {
        boolean pixelDistances = calibrationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.pixelDistances",pixelDistances);

        Track track = tracks.get(ID);
        DecimalFormat df = new DecimalFormat("0.000E0");
        DecimalFormat dfS = new DecimalFormat("#");
        String units = track.getUnits(pixelDistances);

        int nPoints = (int) Math.round(Double.parseDouble(nPointsTextField.getText()));

        double[] linearFit = track.getMSDLinearFit(pixelDistances,nPoints);

        rt.setValue("Diffusion coefficient ("+units+"^2/frame)",0,df.format(linearFit[0]/4));

        IJ.log("Diffusion coefficient: "+String.valueOf(df.format(linearFit[0]/4))+" "+units+"^2/frame");
        IJ.log("Least squares gradient: "+String.valueOf(df.format(linearFit[0]))+" "+units+"^2/frame");
        IJ.log("Least squares intercept: "+String.valueOf(df.format(linearFit[1]))+" "+units+"^2");
        IJ.log("Calculated over "+String.valueOf(dfS.format(linearFit[2]))+" frames");
        IJ.log(" ");

    }

    @Override
    public String getTitle() {
        return "Track summary";
    }

    @Override
    public JPanel getExtraControls() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(0,5,0,5);


        // Full track details
        boolean fullTrackDetails = Prefs.get("TrackAnalysis.TrackSummary.fullTrackDetails",true);
        frameByFrameCheckbox = new JCheckBox("Full track details (frame-by-frame)");
        frameByFrameCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        frameByFrameCheckbox.setSelected(fullTrackDetails);
        c.gridy++;
        panel.add(frameByFrameCheckbox,c);

        // Checkbox for number of objects
        boolean numberOfObjects = Prefs.get("TrackAnalysis.TrackSummary.numberOfObjects",true);
        numberOfObjectsCheckbox = new JCheckBox("Output number of objects");
        numberOfObjectsCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        numberOfObjectsCheckbox.setSelected(numberOfObjects);
        panel.add(numberOfObjectsCheckbox,c);

        // Track duration
        boolean trackDuration = Prefs.get("TrackAnalysis.TrackSummary.trackDuration",true);
        trackDurationCheckbox = new JCheckBox("Output track duration");
        trackDurationCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        trackDurationCheckbox.setSelected(trackDuration);
        c.gridy++;
        panel.add(trackDurationCheckbox,c);

        // Spatial statistics
        boolean spatialStatistics = Prefs.get("TrackAnalysis.TrackSummary.spatialStatistics",true);
        spatialStatisticsCheckbox = new JCheckBox("Output spatial statistics");
        spatialStatisticsCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        spatialStatisticsCheckbox.setSelected(spatialStatistics);
        c.gridy++;
        panel.add(spatialStatisticsCheckbox,c);

        // Diffusion coefficient
        boolean diffusionCoefficient = Prefs.get("TrackAnalysis.TrackSummary.diffusionCoefficient",false);
        diffusionCoefficientCheckbox = new JCheckBox("Output diffusion coefficient");
        diffusionCoefficientCheckbox.setPreferredSize(new Dimension(panelWidth,elementHeight));
        diffusionCoefficientCheckbox.setSelected(diffusionCoefficient);
        c.gridy++;
        panel.add(diffusionCoefficientCheckbox,c);

        // Label and text field to get number of points to use for diffusion coefficient calculation
        JTextField label = new JTextField("Number of frames");
        label.setPreferredSize(new Dimension(3*panelWidth/4,elementHeight));
        label.setEditable(false);
        label.setBorder(null);
        c.gridwidth = 1;
        c.gridy++;
        c.insets = new Insets(5,5,20,5);
        panel.add(label,c);

        int nPoints = (int) Prefs.get("TrackAnalysis.TrackSummary.nPoints",25);
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
        boolean fullTrackDetails = frameByFrameCheckbox.isSelected();
        Prefs.set("TrackAnalysis.fullTrackDetails",fullTrackDetails);

        boolean numberOfObjects = numberOfObjectsCheckbox.isSelected();
        Prefs.set("TrackAnalysis.numberOfObjects",numberOfObjects);

        boolean trackDuration = trackDurationCheckbox.isSelected();
        Prefs.set("TrackAnalysis.trackDuration",trackDuration);

        boolean spatialStatistics = spatialStatisticsCheckbox.isSelected();
        Prefs.set("TrackAnalysis.spatialStatistics",spatialStatistics);

        boolean diffusionCoefficient = diffusionCoefficientCheckbox.isSelected();
        Prefs.set("TrackAnalysis.diffusionCoefficient",diffusionCoefficient);

        ResultsTable rt = new ResultsTable();

        if (ID == -1) {
            IJ.log("++SUMMARY OF ALL TRACKS++");
            IJ.log(" ");

            int i=0;
            for (int key:tracks.keySet()) {
                rt.setValue("ID",i++,key);
            }

            showTrackSummary(rt);
            if (numberOfObjects) showNumberOfObjects();
            if (trackDuration) showAllTracksDuration(rt);
            if (spatialStatistics) showAllTracksSpatialStatistics(rt);
            if (diffusionCoefficient) showDiffusionCoefficient(rt);

            ResultsTable fullRT = new ResultsTable();
            if (fullTrackDetails) showFullTrackResults(ID, fullRT);
            fullRT.show("Full track details");

        } else {
            IJ.log("++SUMMARY OF TRACK "+ID+"++");
            IJ.log(" ");

            rt.setValue("ID",0,ID);

            showTrackSummary(ID,rt);
            if (trackDuration) showTrackDuration(ID,rt);
            if (spatialStatistics) showTrackSpatialStatistics(ID,rt);
            if (diffusionCoefficient) showDiffusionCoefficient(ID,rt);

            ResultsTable fullRT = new ResultsTable();
            if (fullTrackDetails) showFullTrackResults(ID,fullRT);
            fullRT.show("Full track details");

        }

        rt.show("Track summary");

    }

    @Override
    public void extraActions(ActionEvent e) {

    }

}
