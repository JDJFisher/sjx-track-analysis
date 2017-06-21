//TODO: Need to import distance calibration

package wbif.sjx.TrackAnalysis.IO;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.action.AbstractTMAction;
import ij.ImagePlus;
import ij.measure.Calibration;
import wbif.sjx.TrackAnalysis.TrackAnalysis;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Track;
import wbif.sjx.common.Object.TrackCollection;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by sc13967 on 12/06/2017.
 */
public class TrackMateLoader extends AbstractTMAction {
    private final SelectionModel selectionModel;

    private final Model model;

    private Logger logger;

    public TrackMateLoader(final Model model, final SelectionModel selectionModel ) {
        this.model = model;
        this.selectionModel = selectionModel;

    }

    @Override
    public void execute(TrackMate trackMate) {
        ImagePlus ipl = trackMate.getSettings().imp;
        Calibration calibration = ipl.getCalibration();

        // Creating the ArrayList to hold all Tracks
        TrackCollection tracks = new TrackCollection(calibration.getX(1),calibration.getZ(1));

        // Converting tracks in TrackMate model to internal Track Objects
        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        for (Integer trackID:trackIDs) {
            ArrayList<Spot> spots = new ArrayList<>(model.getTrackModel().trackSpots(trackID));

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot : spots) {
                // NEED TO GET TRACK ID
                double x = spot.getFeature(Spot.POSITION_X)/tracks.getDistXY();
                double y = spot.getFeature(Spot.POSITION_Y)/tracks.getDistXY();
                double z = spot.getFeature(Spot.POSITION_Z)/tracks.getDistZ();
                int f = (int) Math.round(spot.getFeature(Spot.FRAME));

                tracks.putIfAbsent(trackID, new Track());
                tracks.get(trackID).add(new Point(x,y,z,f));

            }
        }

        // Sorting spots in each track to ensure they are in chronological order
        for (Track track:tracks.values()) {
            track.sort((o1, o2) -> {
                double t1 = o1.getF();
                double t2 = o2.getF();
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });
        }

        // Running TrackAnalysis
        new TrackAnalysis(tracks, ipl);

    }

    @Override
    public void setLogger( final Logger logger ) {
        this.logger = logger;

    }
}

