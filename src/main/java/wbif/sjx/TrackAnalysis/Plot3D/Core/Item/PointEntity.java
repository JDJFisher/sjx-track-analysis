package wbif.sjx.TrackAnalysis.Plot3D.Core.Item;


import wbif.sjx.TrackAnalysis.Plot3D.Graphics.Component.Mesh;
import wbif.sjx.TrackAnalysis.Plot3D.Graphics.FrustumCuller;
import wbif.sjx.TrackAnalysis.Plot3D.Graphics.GenerateMesh;
import wbif.sjx.TrackAnalysis.Plot3D.Graphics.ShaderProgram;
import wbif.sjx.TrackAnalysis.Plot3D.Math.Matrix4f;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector3f;
import wbif.sjx.TrackAnalysis.Plot3D.Utils.DataTypeUtils;
import wbif.sjx.common.Object.Point;

import java.awt.*;

/**
 * Created by sc13967 on 02/08/2017.
 */
public class PointEntity {
    private final static float TRAIL_RADIUS = 0.5f;
    private final static float PARTICLE_RADIUS = 2.5f;

    private static final Mesh PARTICLE_MESH = GenerateMesh.sphere(PARTICLE_RADIUS, 20);

    private static final int LOWEST_RESOLUTION = 3;
    private static final Mesh PIPE_MESH_LOWEST = GenerateMesh.pipe(TRAIL_RADIUS, LOWEST_RESOLUTION, 1);
    private static final Mesh HINGE_POINT_MESH_LOWEST = GenerateMesh.sphere(TRAIL_RADIUS, LOWEST_RESOLUTION);

    private static final int LOW_RESOLUTION = 6;
    private static final Mesh PIPE_MESH_LOW = GenerateMesh.pipe(TRAIL_RADIUS, LOW_RESOLUTION, 1);
    private static final Mesh HINGE_POINT_MESH_LOW = GenerateMesh.sphere(TRAIL_RADIUS, LOW_RESOLUTION);

    private static final int MEDIUM_RESOLUTION = 10;
    private static final Mesh PIPE_MESH_MEDIUM = GenerateMesh.pipe(TRAIL_RADIUS, MEDIUM_RESOLUTION, 1);
    private static final Mesh HINGE_POINT_MESH_MEDIUM = GenerateMesh.sphere(TRAIL_RADIUS, MEDIUM_RESOLUTION);

    private static final int HIGH_RESOLUTION = 20;
    private static final Mesh PIPE_MESH_HIGH = GenerateMesh.pipe(TRAIL_RADIUS, HIGH_RESOLUTION, 1);
    private static final Mesh HINGE_POINT_MESH_HIGH = GenerateMesh.sphere(TRAIL_RADIUS, HIGH_RESOLUTION);

    private TrackEntity trackEntity;
    private Vector3f globalPosition;
    private Vector3f motilityPosition;
    private Vector3f displayPosition;
    private Color velocityColour;

    public PointEntity(TrackEntity trackEntity, Point point){
        this.trackEntity = trackEntity;
        globalPosition = DataTypeUtils.toVector3f(point);
        motilityPosition = Vector3f.Add(globalPosition, trackEntity.getMotilityPlotVector());
        velocityColour = measurementToColour(trackEntity.getInstantaneousVelocity(point.getF()), trackEntity.getTrackEntityCollection().getTracks().getMaximumInstantaneousVelocity());
    }

    public Color measurementToColour(double measurement, double highestMeasurement){
        int colourIndex = (int)((measurement / highestMeasurement) * 255);
        return new Color(255, colourIndex, colourIndex);
    }

    private boolean hasPipe = false;
    private float pipeLength;
    private Vector3f pipeRotation;

    public void createPipe(Vector3f nextPointGlobalPosition) {
        Vector3f pointToNextPoint = Vector3f.Subtract(nextPointGlobalPosition, globalPosition);
        pipeRotation = new Vector3f(0, pointToNextPoint.getPhi(),180 - pointToNextPoint.getTheta());
        pipeLength = pointToNextPoint.getLength();
        hasPipe = true;
    }

    public void renderParticle(ShaderProgram shaderProgram, FrustumCuller frustumCuller){
        updateDisplayPosition();

        if(frustumCuller.isInsideFrustum(displayPosition, 1, PARTICLE_MESH)) {
            setDisplayColourUniform(shaderProgram);
            Matrix4f combinedTransformationMatrix = Matrix4f.translation(displayPosition.getX(), displayPosition.getZ(), displayPosition.getY());
            shaderProgram.setMatrix4fUniform("combinedTransformationMatrix", combinedTransformationMatrix);

            PARTICLE_MESH.render();
        }
    }

    public void renderPipe(ShaderProgram shaderProgram, FrustumCuller frustumCuller){
        updateDisplayPosition();
        setDisplayColourUniform(shaderProgram);
        Matrix4f combinedTransformationMatrix = Matrix4f.translation(displayPosition.getX(), displayPosition.getZ(), displayPosition.getY());

        if(frustumCuller.isInsideFrustum(displayPosition, 1, HINGE_POINT_MESH_HIGH)) {
            shaderProgram.setMatrix4fUniform("combinedTransformationMatrix", combinedTransformationMatrix);

            switch (trackEntity.getTrackEntityCollection().displayQuality) {
                case LOWEST:
                    HINGE_POINT_MESH_LOWEST.render();
                    break;
                case LOW:
                    HINGE_POINT_MESH_LOW.render();
                    break;
                case MEDIUM:
                    HINGE_POINT_MESH_MEDIUM.render();
                    break;
                case HIGH:
                    HINGE_POINT_MESH_HIGH.render();
                    break;
            }
        }

        if(frustumCuller.isInsideFrustum(displayPosition, new Vector3f(1,pipeLength,1), PIPE_MESH_HIGH)) {
            combinedTransformationMatrix.apply(Matrix4f.rotation(pipeRotation.getX(), pipeRotation.getZ(), pipeRotation.getY()));
            combinedTransformationMatrix.apply(Matrix4f.stretchY(pipeLength));
            shaderProgram.setMatrix4fUniform("combinedTransformationMatrix", combinedTransformationMatrix);

            switch (trackEntity.getTrackEntityCollection().displayQuality) {
                case LOWEST:
                    PIPE_MESH_LOWEST.render();
                    break;
                case LOW:
                    PIPE_MESH_LOW.render();
                    break;
                case MEDIUM:
                    PIPE_MESH_MEDIUM.render();
                    break;
                case HIGH:
                    PIPE_MESH_HIGH.render();
                    break;
            }
        }
    }

    private void updateDisplayPosition(){
        if(trackEntity.getTrackEntityCollection().ifMotilityPlot()){
            displayPosition = motilityPosition;
        }else {
            displayPosition = globalPosition;
        }
    }

    private void setDisplayColourUniform(ShaderProgram shaderProgram){
        Color displayColour;

        switch (trackEntity.getTrackEntityCollection().displayColour){
            case SOLID:
                displayColour = trackEntity.getColour();
                break;
            case VELOCITY:
                displayColour = velocityColour;
                break;
            default:
                displayColour = trackEntity.getColour();
                break;
        }

        shaderProgram.setColourUniform("colour", displayColour);
    }

    private Matrix4f getPositionMatrix(){
        if(trackEntity.getTrackEntityCollection().ifMotilityPlot()){
            return Matrix4f.translation(motilityPosition);
        }else {
            return Matrix4f.translation(globalPosition);
        }
    }

    public Vector3f getGlobalPosition() {
        return globalPosition;
    }

    public boolean hasPipe() {
        return hasPipe;
    }
}
