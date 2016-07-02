package ak.physSim.render.meshes;

import ak.physSim.render.meshes.Mesh;
import ak.physSim.util.Logger;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Aleksander on 01/07/2016.
 */
public class DirectionalMesh {

    private HashMap<Side, Mesh> meshLists;

    public DirectionalMesh () {
        meshLists = new HashMap<>();
    }

    public void addMesh (Side normal, Mesh mesh) {
        if (meshLists.containsKey(normal)){
            meshLists.remove(normal);
            Logger.log(Logger.LogLevel.ERROR, "MESH ALREADY CONTAINS FACE " + normal);
        }
        meshLists.put(normal, mesh);
        Logger.log(Logger.LogLevel.DEBUG, String.format("Putting %s into %s with normal %s", mesh.getVaoId(), this, normal));
    }

    public FaceMesh[] getVisible(Vector3i axisVector){
        FaceMesh[] meshes = new FaceMesh[3];

        meshes[0] = new FaceMesh();
        Mesh mesh;

        if (axisVector.x == -1){
            mesh = meshLists.get(Side.X_PLUS);
        } else {
            mesh = meshLists.get(Side.X_MINUS);
        }
        meshes[0].vaoID = mesh.getVaoId();
        meshes[0].vertexCount = mesh.getVertCount();

        meshes[1] = new FaceMesh();
        if (axisVector.y == -1){
            mesh = meshLists.get(Side.Y_PLUS);
        } else {
            mesh = meshLists.get(Side.Y_MINUS);
        }
        meshes[1].vaoID = mesh.getVaoId();
        meshes[1].vertexCount = mesh.getVertCount();

        meshes[2] = new FaceMesh();
        if (axisVector.z == -1){
            mesh = meshLists.get(Side.Z_PLUS);
        } else {
            mesh = meshLists.get(Side.Z_MINUS);
        }
        meshes[2].vaoID = mesh.getVaoId();
        meshes[2].vertexCount = mesh.getVertCount();

        return meshes;
    }

    public FaceMesh[] getAllMeshes(){
        FaceMesh[] meshes = new FaceMesh[6];
        Collection<Mesh> values = meshLists.values();
        Mesh[] mesh = values.toArray(new Mesh[values.size()]);
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = new FaceMesh();
            meshes[i].vaoID = mesh[i].getVaoId();
            meshes[i].vertexCount = mesh[i].getVertCount();
        }
        return meshes;
    }

    public void cleanup() {
        meshLists.values().forEach(Mesh::cleanup);
    }


}
