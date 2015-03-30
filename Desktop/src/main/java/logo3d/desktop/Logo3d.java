package logo3d.desktop;

import com.jme3.app.SimpleApplication;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 29/03/2015.
 */
public class Logo3d extends SimpleApplication {

    private static final Logger LOG = getLogger(Logo3d.class);

    public static void main(String[] args) {
        bridgeJUL();
        LOG.debug("Starting...");
        Logo3d logo3d = new Logo3d();
        logo3d.setShowSettings(false);
        logo3d.start();
    }

    /**
     * remove default Java logging util, replace with slf4j
     */
    private static void bridgeJUL() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getLogger("global").setLevel(java.util.logging.Level.FINEST);
    }

    @Override
    public void simpleInitApp() {
        getStateManager().attach(new MainScreen());
    }
}
