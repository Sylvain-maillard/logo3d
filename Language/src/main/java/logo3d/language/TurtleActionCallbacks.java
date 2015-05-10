package logo3d.language;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 08/04/2015.
 */
public interface TurtleActionCallbacks {

    Logger LOG = getLogger(TurtleActionCallbacks.class);

    default void forward(float value) {
        LOG.info("Should forward by {}", value);
    }

    default void backward(float value) {
        LOG.info("Should backward by {}", value);
    }

    default void turnLeft(float degree) {
        LOG.info("Should turn left by {} degree", degree);
    }

    default void turnRight(float degree) {
        LOG.info("Should turn right by {} degree", degree);
    }

    default void print(String msg) {
        LOG.info("Should print {}", msg);
    }
}
