package logo3d.desktop;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Node;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.sound.SoundSystem;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 29/03/2015.
 */
public class MainScreen extends AbstractAppState implements ScreenController {

    private static final Logger LOG = getLogger(MainScreen.class);

    private Node rootNode;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

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
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @NiftyEventSubscriber(id="consoleCommande")
    public void onConsoleExecuteCommandEvent(final String id, final ConsoleExecuteCommandEvent cEvent ){
        String consoleInput = cEvent.getCommandLine();
        LOG.debug(consoleInput);
    }


    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }
}
