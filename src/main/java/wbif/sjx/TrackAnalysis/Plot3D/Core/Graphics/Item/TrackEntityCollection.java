package wbif.sjx.TrackAnalysis.Plot3D.Core.Graphics.Item;

import wbif.sjx.TrackAnalysis.Plot3D.Core.Graphics.FrustumCuller;
import wbif.sjx.TrackAnalysis.Plot3D.Core.Graphics.Item.TrackEntity;
import wbif.sjx.TrackAnalysis.Plot3D.Core.Graphics.ShaderProgram;
import wbif.sjx.common.Object.TrackCollection;

import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 31/07/2017.
 */
public class TrackEntityCollection extends LinkedHashMap<Integer, TrackEntity>{
    private TrackCollection tracks;
    private final int highestFrame;

    public TrackEntityCollection(TrackCollection tracks){
        this.tracks = tracks;
        for(int trackID: tracks.keySet()){
            put(trackID, new TrackEntity(this, tracks.get(trackID)));
        }

        this.highestFrame = tracks.getHighestFrame();
        this.displayColour = displayColour_DEFAULT;
        this.showTrail = showTrail_DEFAULT;
        this.motilityPlot = motilityPlot_DEFAULT;
        this.trailLength_MAXIMUM = highestFrame < 1 ? 1 : highestFrame;
        this.trailLength = trailLength_MAXIMUM;
    }

    public void render(ShaderProgram shaderProgram, FrustumCuller frustumCuller, int frame){
        for(TrackEntity trackEntity: values()){
            trackEntity.render(shaderProgram, frustumCuller, frame);
        }
    }

    public TrackCollection getTracks() {
        return tracks;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final boolean showTrail_DEFAULT = true;

    private boolean showTrail;

    public boolean isTrailVisibile(){
        return showTrail;
    }

    public void setTrailVisibility(boolean state){
        showTrail = state;
    }

    public void toggleTrailVisibility(){
        showTrail = !showTrail;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final boolean motilityPlot_DEFAULT = false;

    private boolean motilityPlot;

    public boolean ifMotilityPlot(){
        return motilityPlot;
    }

    public void setMotilityPlot(boolean state){
        motilityPlot = state;
    }

    public void toggleMotilityPlot(){
        motilityPlot = !motilityPlot;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int trailLength_MINIMUM = 1;
    public final int trailLength_MAXIMUM;

    private int trailLength;

    public int getTrailLength(){
        return trailLength;
    }

    public void setTrailLength(int value) {
        if (value < trailLength_MINIMUM) {
            trailLength = trailLength_MINIMUM;
        } else if (value > trailLength_MAXIMUM) {
            trailLength = trailLength_MAXIMUM;
        } else {
            trailLength = value;
        }
    }

    public void changeTrailLength(int deltaValue){
        setTrailLength(getTrailLength() + deltaValue);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public displayColourOptions displayColour;

    public static final displayColourOptions displayColour_DEFAULT = displayColourOptions.VELOCITY;

    public enum displayColourOptions{
        SOLID,
        VELOCITY
    }

}
