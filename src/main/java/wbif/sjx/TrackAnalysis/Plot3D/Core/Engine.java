package wbif.sjx.TrackAnalysis.Plot3D.Core;

import wbif.sjx.TrackAnalysis.GUI.TrackPlotControl;
import wbif.sjx.TrackAnalysis.Plot3D.Core.Item.CollectionBounds;
import wbif.sjx.TrackAnalysis.Plot3D.Input.Cursor;
import wbif.sjx.TrackAnalysis.Plot3D.Input.Keyboard;
import wbif.sjx.TrackAnalysis.Plot3D.Input.MouseButtons;
import wbif.sjx.TrackAnalysis.Plot3D.Input.MouseWheel;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector3f;
import wbif.sjx.TrackAnalysis.Plot3D.Utils.StopWatch;

import static org.lwjgl.glfw.GLFW.*;
import static wbif.sjx.TrackAnalysis.Plot3D.Core.Renderer.BIAS;

/**
 * Created by JDJFisher on 31/07/2017.
 */
public class Engine {

    private static final int TARGET_UPDATES_PER_SECOND = 300;

    private TrackPlotControl trackPlotControl;
    private boolean running;
    private GLFWWindow window;
    private Renderer renderer;
    private Camera camera;
    private Scene scene;
    private StopWatch fpsTimer;
    private StopWatch loopTimer;

    private int frameCount;

    public Engine(TrackPlotControl trackPlotControl){
        this.trackPlotControl = trackPlotControl;
    }

    public void init() throws Exception {
        window = new GLFWWindow("3D Track Plot", 600, 600, false);
        renderer = new Renderer();
        camera = new Camera();
        scene = new Scene(trackPlotControl.getTracks(), trackPlotControl.getIpl());
        fpsTimer = new StopWatch();
        loopTimer = new StopWatch();

        if (trackPlotControl.is2D()) {
            viewXYplane();
        } else {
            viewXZplane();
        }
    }

    public void start() {
        window.setVisibility(true);
        running = true;
        mainLoop();
    }

    public void stop() {
        running = false;
    }

    private void mainLoop() {
        float cumulativeLagTime = 0f;
        final float secondsPerUpdate = 1f / TARGET_UPDATES_PER_SECOND;
        final float secondsPerFrame = 1f / window.getRefreshRate();
        fpsTimer.start();

        while (running && window.isOpen()){
            cumulativeLagTime += loopTimer.getElapsedTime();
            loopTimer.start();

            handleInput();

            while (cumulativeLagTime >= secondsPerUpdate) {
                update(secondsPerUpdate);
                cumulativeLagTime -= secondsPerUpdate;
            }

            window.update();

            if(!window.isMinimized()) {
                render();

                if (window.isVSyncEnabled()) {
                    sync(secondsPerFrame);
                }
            }

            loopTimer.stop();
        }

        window.setVisibility(false);
        running = false;
    }

    private void sync(float secondsPerFrame) {
        double expectedEndTime = loopTimer.getStartTime() + secondsPerFrame;
        while (loopTimer.getTime() < expectedEndTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose(){
        renderer.dispose();
        scene.dispose();
        window.dispose();
    }

    private void handleInput(){
        camera.handleInput();

        //Handles frame switching
        if(Keyboard.isKeyDown(GLFW_KEY_UP)){
            scene.incrementFrame();
        }else if(Keyboard.isKeyDown(GLFW_KEY_DOWN)){
            scene.decrementFrame();
        }

        if(Keyboard.isKeyTapped(GLFW_KEY_PAGE_UP)){
            scene.incrementFrame();
        }else if(Keyboard.isKeyTapped(GLFW_KEY_PAGE_DOWN)){
            scene.decrementFrame();
        }

        if(MouseButtons.isButtonTapped(GLFW_MOUSE_BUTTON_MIDDLE)){
            camera.setFOV(Camera.fov_DEF);
        }

        if(Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL)) {
            camera.changeFOV(-MouseWheel.getDeltaScroll());
        }else {
            scene.changeFrame(MouseWheel.getDeltaScroll());
        }

        if(Keyboard.isKeyTapped(GLFW_KEY_F)) {
            scene.togglePlayFrames();
        }

        if (MouseButtons.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            renderer.disableOrthoProj();
        }

        Keyboard.update();
        Cursor.update();
        MouseButtons.update();
        MouseWheel.update();
    }

    private void update(float interval){
        camera.update(interval, scene.getPlotFocus());
        scene.update(interval);
        trackPlotControl.updateGui();
    }

    private void render() {
        if (fpsTimer.getElapsedTime() > 1) {
            fpsTimer.restart();
            window.appendToTitle(String.format("FPS: %d FOV: %d", frameCount, camera.getFOV()));
            frameCount = 0;
        }

        frameCount++;

        renderer.render(window, camera, scene);
    }

    public Camera getCamera() {
        return camera;
    }

    public Scene getScene() {
        return scene;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    private float calcOptimalViewingDistance(float xRange, float yRange){
        return ((Math.max(xRange / window.getAspectRatio(), yRange) + BIAS) / 2) / (float) Math.tan(Math.toRadians(camera.getFOV() / 2));
    }

    // the plane view methods position the camera to face to relevant faces of the collection in the z-up coordinate system

    public void viewXYplane(){
        camera.setTilt(-90);
        camera.setPan(0);

        CollectionBounds bounds = scene.getBounds();

        float optimalViewingDistance = calcOptimalViewingDistance(bounds.getWidth(), bounds.getLength()) + bounds.getMaxPosition().getY();
        Vector3f centrePosition = bounds.getCentrePosition();

        camera.getPosition().set(centrePosition.getX(), optimalViewingDistance, centrePosition.getZ());

        renderer.setOrthoBoundingConstraints(bounds.getWidth(), bounds.getLength());
    }

    public void viewYZplane(){
        camera.setTilt(0);
        camera.setPan(-90);

        CollectionBounds bounds = scene.getBounds();

        float optimalViewingDistance = calcOptimalViewingDistance(bounds.getLength(), bounds.getHeight()) + bounds.getMaxPosition().getX();
        Vector3f centrePosition = bounds.getCentrePosition();

        camera.getPosition().set(optimalViewingDistance, centrePosition.getY(), centrePosition.getZ());

        renderer.setOrthoBoundingConstraints(bounds.getLength(), bounds.getHeight());
    }

    public void viewXZplane(){
        camera.setTilt(0);
        camera.setPan(0);

        CollectionBounds bounds = scene.getBounds();

        float optimalViewingDistance = calcOptimalViewingDistance(bounds.getWidth(), bounds.getHeight()) + bounds.getMaxPosition().getZ();
        Vector3f centrePosition = bounds.getCentrePosition();

        camera.getPosition().set(centrePosition.getX(), centrePosition.getY(), optimalViewingDistance);

        renderer.setOrthoBoundingConstraints(bounds.getWidth(), bounds.getHeight());
    }
}