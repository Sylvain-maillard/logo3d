package logo3d.desktop;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.*;
import com.jme3.scene.control.AbstractControl;
import logo3d.language.TurtleActionCallbacks;
import org.slf4j.Logger;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jme3.math.FastMath.PI;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 30/03/2015.
 */
public class TurtleControl extends AbstractControl implements TurtleActionCallbacks {

    private static final Logger LOG = getLogger(TurtleControl.class);

    private final Material lineMaterial;
    private final Node rootNode;

    private float speed = 4f;

    // initial direction
    private Vector3f direction = Vector3f.UNIT_Z;
    private final Node turtle;

    public enum Direction {
        LEFT(1), RIGHT(1), FORWARD(-1), BACKWARD(1);

        public final float speedCoefficient;

        Direction(float i) {
            speedCoefficient = i;
        }
    }

    public abstract class TurtleAction {

        protected final Direction activeDirection;

        private final AtomicBoolean done = new AtomicBoolean();

        public TurtleAction(Direction activeDirection) {
            this.activeDirection = activeDirection;
            this.done.set(false);
        }

        abstract void doIt(float tpf);

        public void start() {
        }

        public void setDone(boolean done) {
            this.done.set(done);
        }

        public boolean isDone() {
            return done.get();
        }
    }

    public class TranslateAction extends TurtleAction {

        private Vector3f initialPosition;

        private float currentTranslationLimit = 1.0f;

        public TranslateAction(Direction activeDirection, float increment) {
            super(activeDirection);
            currentTranslationLimit = increment;
        }

        @Override
        public void start() {
            initialPosition = turtle.getLocalTranslation().clone();
        }

        @Override
        void doIt(float tpf) {
            turtle.move(direction.mult(speed * tpf * activeDirection.speedCoefficient));
            // check if we have to stop
            // get distance between current position and current one
            float distance = initialPosition.distance(turtle.getLocalTranslation());
            if (distance >= currentTranslationLimit) {
                // stop !
                LOG.debug("STOP ! distance = {}", distance);
                // ok now draw:
                draw(initialPosition);
                setDone(true);
            }
        }
    }

    public class TurnAction extends TurtleAction {
        private float currentRotation = 0f;
        private float currentRotationLimit = PI /2;
        private Quaternion localRotation;
        private Quaternion targetRotation;
        private float currentSlerp = 0f;

        public TurnAction(Direction activeDirection, float degree) {
            super(activeDirection);
            this.currentRotationLimit = degree;
            LOG.info("will turn {}, {} deg", activeDirection, degree);
        }

        @Override
        public void start() {
            localRotation = turtle.getLocalRotation().clone();
            targetRotation = new Quaternion().fromAngles(0, toRadian(currentRotationLimit), 0).mult(localRotation);
            LOG.debug("localRotation: {}",localRotation);
            LOG.debug("target rotation  {}", targetRotation);
        }

        @Override
        void doIt(float tpf) {

            LOG.debug("current slerp: {}, tpf {} , speed {}",currentSlerp, tpf, activeDirection.speedCoefficient);

            currentSlerp += tpf * activeDirection.speedCoefficient;

            Quaternion thisRotation = new Quaternion();
            thisRotation.slerp(localRotation, targetRotation, currentSlerp);

            turtle.setLocalRotation(thisRotation);

            if (currentSlerp >= 1.0f) {
                direction = targetRotation.getRotationColumn(2);
                turtle.setLocalRotation(targetRotation);
                setDone(true);
            }
        }
    }

    private Stack<TurtleAction> actionQueue = new Stack<>();
    private TurtleAction currentAction;

    public TurtleControl(AssetManager assetManager, Node rootNode) {
        Spatial spatial = assetManager.loadModel("Models/turtle/turtle.j3o");

        spatial.addControl(this);
        this.rootNode = rootNode;

        //Get the center of the mesh (no matter the original pivot)
        Vector3f center = spatial.getWorldBound().getCenter();
        LOG.debug("{}",center);

//Create the node to use as pivot
        turtle = new Node();
        turtle.setLocalTranslation(center.setY(0.1f));
        turtle.attachChild(spatial);

//Reverse the pivot to match the center of the mesh
        spatial.setLocalTranslation(center.negate());

        rootNode.attachChild(turtle);

        // add material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Red);

        lineMaterial = mat;
    }

    @Override
    protected void controlUpdate(float tpf) {

        // if no action, do nothing
        if (currentAction == null && actionQueue.isEmpty()) return;

        // if no action, but we got something to do:
        if (currentAction == null) {
            currentAction = actionQueue.pop();
            currentAction.start();
        }
        // progress the current action.
        currentAction.doIt(tpf);

        // check if it is finished
        if (currentAction.isDone()) {
            // set to null and we will pickup the next action on the next frame
            currentAction = null;
            LOG.debug("current action is done");
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    private float toDegree(float radian) {
        return radian * 180 / PI;
    }
    private float toRadian(float degree) {
        return degree / 180 * PI;
    }

    public void translate(Direction direction, float increment) {
        actionQueue.push(new TranslateAction(direction, increment));
    }

    public void turn(Direction direction, float degree) {
        actionQueue.push(new TurnAction(direction, degree));
    }

    public void forward(float i) {
        translate(Direction.FORWARD, i);
    }

    public void backward(float i) {
        translate(Direction.BACKWARD, i);
    }
    public void turnLeft() {
        this.turnLeft(90);
    }

    public void turnLeft(float degree) {
        turn(Direction.LEFT, degree);
    }

    public void turnRight() {
        turnRight(90);
    }

    public void turnRight(float degree) {
       turn (Direction.RIGHT, degree);
    }

    public void draw(Vector3f prevPos) {

        Mesh lineMesh = new Mesh();

        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setLineWidth(4);

        Vector3f localTranslation = turtle.getLocalTranslation();
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{
                prevPos.getX(), prevPos.getY(), prevPos.getZ(),
                localTranslation.getX(), localTranslation.getY(), localTranslation.getZ()});

        LOG.debug("Draw from {} to {}", prevPos, localTranslation);

        lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{0, 1});

        lineMesh.updateBound();
        lineMesh.updateCounts();

        Geometry lineGeometry = new Geometry("line", lineMesh);

        lineGeometry.setMaterial(lineMaterial);

        rootNode.attachChild(lineGeometry);

    }
}
