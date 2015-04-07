package logo3d.desktop;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.*;
import com.jme3.scene.control.AbstractControl;
import logo3d.language.Program;
import org.slf4j.Logger;

import static com.jme3.math.FastMath.PI;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 30/03/2015.
 */
public class TurtleControl extends AbstractControl implements Program.TurtleActionCallbacks {

    private static final Logger LOG = getLogger(TurtleControl.class);

    private final Material lineMaterial;
    private final Node rootNode;

    private float speed = 3f;
    private TurtleAction currentAction = TurtleAction.IDLE;
    private boolean startMoving = false;
    private Vector3f initialPosition;

    // initial direction set to X
    private Vector3f direction = Vector3f.UNIT_Z;
    private final Node pivot;
    private float currentTranslationLimit = 1.0f;
    private float currentRotationLimit = PI /2;

    public enum Direction {
        LEFT(1), RIGHT(-1), FORWARD(-1), BACKWARD(1);

        public final float speedCoefficient;

        Direction(float i) {
            speedCoefficient = i;
        }
    }

    public enum TurtleAction {
        IDLE, TRANSLATE, TURN
    }

    public TurtleControl(AssetManager assetManager, Node rootNode) {
        Spatial spatial = assetManager.loadModel("Models/turtle/turtle.j3o");

        spatial.addControl(this);
        this.rootNode = rootNode;

        //Get the center of the mesh (no matter the original pivot)
        Vector3f center = spatial.getWorldBound().getCenter();
        LOG.debug("{}",center);

//Create the node to use as pivot
        pivot = new Node();
        pivot.setLocalTranslation(center.setY(0.1f));
        pivot.attachChild(spatial);

//Reverse the pivot to match the center of the mesh
        spatial.setLocalTranslation(center.negate());

        rootNode.attachChild(pivot);

        // add material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Red);

        lineMaterial = mat;
    }

    private float currentRotation = 0f;

    @Override
    protected void controlUpdate(float tpf) {

        // if it is the first move, save initial point:
        if (startMoving) {
            startMoving = false;
            initialPosition = pivot.getLocalTranslation().clone();
        }

        switch (currentAction) {
            case IDLE: return;
            case TRANSLATE:
                pivot.move(direction.mult(speed * tpf * activeDirection.speedCoefficient));
                // check if we have to stop
                // get distance between current position and current one
                float distance = initialPosition.distance(pivot.getLocalTranslation());
                if (distance >= currentTranslationLimit) {
                    // stop !
                    currentAction = TurtleAction.IDLE;
                    LOG.debug("STOP ! distance = {}", distance);
                    // ok now draw:
                    draw(initialPosition);
                }
                break;
            case TURN:
                currentRotation += speed * tpf;
                pivot.rotate(0, speed * tpf * activeDirection.speedCoefficient, 0);
                // check if we have to stop
                if (currentRotation >= currentRotationLimit) {
                    currentAction = TurtleAction.IDLE;
                    // reset current rotation
                    currentRotation = 0;
                    // update direction
                    direction = pivot.getLocalRotation().getRotationColumn(2);;
                }
                break;
        }
    }

    private float toDegree(float radian) {
        return radian * 180 / PI;
    }
    private float toRadian(float degree) {
        return degree / 180 * PI;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private boolean changeState(TurtleAction newState) {
        if (currentAction != TurtleAction.IDLE) {
            LOG.info("an action is already on going! {}", currentAction);
            return false;
        }

        currentAction = newState;
        startMoving = true;

        return true;
    }

    private Direction activeDirection;

    public void translate(Direction direction, float increment) {
        changeState(TurtleAction.TRANSLATE);
        currentTranslationLimit = increment;
        activeDirection = direction;
    }

    public void turn(Direction direction, float degree) {
        changeState(TurtleAction.TURN);
        currentRotationLimit = toRadian(degree);
        activeDirection = direction;
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

        Vector3f localTranslation = pivot.getLocalTranslation();
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
