package logo3d.desktop;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.ChaseCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 29/03/2015.
 */
public class MainScreen extends AbstractAppState implements ScreenController {

    private static final Logger LOG = getLogger(MainScreen.class);

    private Node rootNode;
    private TurtleControl turtleControl;
    private SimpleApplication app;
    private CommandInterpreter commandInterpreter;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication)app;

        // setup gui.
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(app.getAssetManager(),
                app.getInputManager(),
                app.getAudioRenderer(),
                app.getGuiViewPort());
        niftyDisplay.getNifty().fromXml("Interface/" + getClass().getSimpleName() + ".xml", getClass().getSimpleName(), this);

        // attach the nifty display to the gui view port as a processor
        app.getGuiViewPort().addProcessor(niftyDisplay);

        // setup skybox:
        rootNode = ((SimpleApplication)app).getRootNode();
        rootNode.attachChild(SkyFactory.createSky(app.getAssetManager(), "Skybox/Skybox.dds", false));

        // setup turtleControl
        this.turtleControl = new TurtleControl(app.getAssetManager(), rootNode);
        this.commandInterpreter = new CommandInterpreter(turtleControl);

        // setup paper
        new Paper(app.getAssetManager(), rootNode);

        // setup lights
        DirectionalLight sun1 = new DirectionalLight();
        sun1.setDirection(new Vector3f(0f, -10f, 10.0f));
        rootNode.addLight(sun1);

        // setup cam
        // Disable the default flyby cam
        ((SimpleApplication) app).getFlyByCamera().setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(app.getCamera(), this.turtleControl.getSpatial(), app.getInputManager());
        chaseCam.setSmoothMotion(true);

        // setup debug axis
        attachCoordinateAxes(Vector3f.UNIT_Y);
    }

    private void attachCoordinateAxes(Vector3f pos){
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        rootNode.attachChild(g);
        return g;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    private static final Color RED = new Color("#f00");
    private static final Color GREEN = new Color("#0f0");

    @NiftyEventSubscriber(id="consoleCommande")
    public void onConsoleExecuteCommandEvent(final String id, final ConsoleExecuteCommandEvent cEvent ){
        commandInterpreter.interpret(cEvent);
    }


    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }
}
