package logo3d.desktop;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Created by SylvainMaillard on 30/03/2015.
 */
public class Turtle {

    private Spatial turtleSpatial;

    public Turtle(AssetManager assetManager, Node rootNode) {
        this.turtleSpatial = assetManager.loadModel("Models/turtle/turtle.j3o");

        // add material

        rootNode.attachChild(this.turtleSpatial);
    }

    public Spatial getTurtleSpatial() {
        return turtleSpatial;
    }
}
