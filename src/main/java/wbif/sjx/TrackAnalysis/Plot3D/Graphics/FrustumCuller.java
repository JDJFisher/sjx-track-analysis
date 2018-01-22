package wbif.sjx.TrackAnalysis.Plot3D.Graphics;


import wbif.sjx.TrackAnalysis.Plot3D.Core.Item.Entity;
import wbif.sjx.TrackAnalysis.Plot3D.Graphics.Component.Mesh;
import wbif.sjx.TrackAnalysis.Plot3D.Math.Matrix4f;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector3f;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector4f;


public class FrustumCuller {

    private static final FrustumCuller INSTANCE = new FrustumCuller();

    public static FrustumCuller getInstance() {
        return INSTANCE;
    }

    private Matrix4f projectedViewMatrix;
    private Vector4f[] frustumPlanes;

    private FrustumCuller(){
        projectedViewMatrix = new Matrix4f();
        frustumPlanes = new Vector4f[6];
        for (int i = 0; i < frustumPlanes.length; i++){
            frustumPlanes[i] = new Vector4f();
        }
    }

    public void setProjectedViewMatrix(Matrix4f projectedViewMatrix) {
        this.projectedViewMatrix = projectedViewMatrix;
        update();
    }



    public void update(){
        float[][] E = projectedViewMatrix.elements;

        frustumPlanes[0].set(E[3][0] + E[0][0],  E[3][1] + E[0][1],E[3][2] + E[0][2],E[3][3] + E[0][3]);
        frustumPlanes[1].set(E[3][0] - E[0][0],  E[3][1] - E[0][1],E[3][2] - E[0][2],E[3][3] - E[0][3]);
        frustumPlanes[2].set(E[3][0] + E[1][0],  E[3][1] + E[1][1],E[3][2] + E[1][2],E[3][3] + E[1][3]);
        frustumPlanes[3].set(E[3][0] - E[1][0],  E[3][1] - E[1][1],E[3][2] - E[1][2],E[3][3] - E[1][3]);
        frustumPlanes[4].set(E[3][0] + E[2][0],  E[3][1] + E[2][1],E[3][2] + E[2][2],E[3][3] + E[2][3]);
        frustumPlanes[5].set(E[3][0] - E[2][0],  E[3][1] - E[2][1],E[3][2] - E[2][2],E[3][3] - E[2][3]);

        for (int i = 0; i < frustumPlanes.length; i++){
            frustumPlanes[i].normalize3();
        }
    }

    public boolean isInsideFrustum(Entity e) {
        return isInsideFrustum(e.getPosition(), e.getScale(), e.getMesh());
    }

    public boolean isInsideFrustum(Vector3f position, Vector3f scale, Mesh mesh) {
        return isInsideFrustum(position, scale.getLargestComponent(), mesh);
    }

    public boolean isInsideFrustum(Vector3f position, float scale, Mesh mesh) {
        return isInsideFrustum(position, scale * mesh.getBoundingSphereRadius());
    }

    public boolean isInsideFrustum(Vector3f position, float scaledBoundingSphereRaduis) {
        for (Vector4f plane: frustumPlanes) {
            if (plane.getX() * position.getX() + plane.getY() * position.getY() + plane.getZ() * position.getZ() + plane.getW() <= - scaledBoundingSphereRaduis) {
                return false;
            }
        }
        return true;
    }
}
