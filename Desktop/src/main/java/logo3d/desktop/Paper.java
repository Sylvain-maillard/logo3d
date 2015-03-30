package logo3d.desktop;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 * Created by SylvainMaillard on 30/03/2015.
 */
public class Paper {
    public Paper(AssetManager assetManager, Node rootNode) {
        Texture texture = assetManager.loadTexture("Textures/watercolor-paper.jpg");
        Box cube1Mesh = new Box(50,0.2f,50f);
        Geometry cube1Geo = new Geometry("My Textured Paper", cube1Mesh);
        cube1Geo.setLocalTranslation(new Vector3f(0f,-0.2f,0f));
        Material cube1Mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        cube1Mat.setTexture("ColorMap", texture);
        cube1Geo.setMaterial(cube1Mat);
        rootNode.attachChild(cube1Geo);
    }
}
