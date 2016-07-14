package ak.physSim.chunk;

import ak.physSim.render.meshes.Mesh;
import ak.physSim.util.Logger;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import static ak.physSim.util.Reference.CHUNK_SIZE;

/*
* After hours of frustration at being unable to code it myself, I have taken code from
* https://github.com/roboleary/GreedyMesh/blob/master/src/mygame/Main.java
* and modified it a bit.
*
*
* */
public class ChunkMesher{

    private static final int Z_MINUS = 0;
    private static final int Z_PLUS = 1;
    private static final int X_PLUS = 2;
    private static final int X_MINUS = 3;
    private static final int Y_PLUS = 4;
    private static final int Y_MINUS = 5;

    private ArrayList<Float> verticesBuffer;
    private ArrayList<Float> colourBuffer;
    private ArrayList<Integer> indicesBuffer;
    private ArrayList<Float> normalsBuffer;

    private int indexOffset = 0;
    private Chunk chunk;
    private Mesh mesh;
    private ChunkManager manager;

    public ChunkMesher(Chunk chunk, ChunkManager manager) {
        this.chunk = chunk;
        this.manager = manager;
        colourBuffer = new ArrayList<>();
        verticesBuffer = new ArrayList<>();
        indicesBuffer = new ArrayList<>();
        normalsBuffer = new ArrayList<>();
    }

    public void run(){
        Logger.log(Logger.LogLevel.DEBUG, "Mesh creation started");
        long time = System.currentTimeMillis();
        createGreedyMesh();
        Logger.log(Logger.LogLevel.DEBUG, "Mesh creation finished taking " + (System.currentTimeMillis() - time) + "ms to finish");
        finishMesh();
    }

    public boolean isReady(){
        return mesh != null;
    }

    private void finishMesh(){
        float[] vert = new float[verticesBuffer.size()];
        Iterator<Float> Vertiter = verticesBuffer.iterator();
        for (int i = 0; i < vert.length; i++) {
            vert[i] = Vertiter.next();
        }
        Logger.log(Logger.LogLevel.DEBUG, "Length of vert array " + vert.length);
        float[] colour = new float[colourBuffer.size()];
        Iterator<Float> colIter = colourBuffer.iterator();
        for (int i = 0; i < colour.length; i++) {
            colour[i] = colIter.next();
        }
        Logger.log(Logger.LogLevel.DEBUG, "Length of colour array " + colour.length);
        int[] indices = new int[indicesBuffer.size()];
        Iterator<Integer> intIter = indicesBuffer.iterator();
        for (int i = 0; i < indices.length; i++) {
            indices[i] = intIter.next();
        }
        Logger.log(Logger.LogLevel.DEBUG, "Length of indices array " + indices.length);
        float[] normals = new float[normalsBuffer.size()];
        Iterator<Float> normIter = normalsBuffer.iterator();
        for (int i = 0; i < normals.length; i++) {
            normals[i] = normIter.next();
        }
        Logger.log(Logger.LogLevel.DEBUG, "Length of indices array " + indices.length);
        mesh = new Mesh(vert, colour, indices, normals);
    }

    public Mesh getMesh() throws Exception {
        if (mesh == null)
            throw new Exception("Mesh not ready exception");
        return mesh;
    }

    private class VoxelFace {

        public boolean transparent;
        public VoxelType type;
        public int side;

        public boolean equals(final VoxelFace face) {
            return face.transparent == this.transparent && face.type == this.type;
        }
    }



    VoxelFace getVoxelFace(final int x, final int y, final int z, final int side) {
        Voxel vox = chunk.getVoxel(x, y, z);

        VoxelFace voxelFace = new VoxelFace();
        //TODO: PRIORITY KINDA IMPORTANT, CULLING?
        if (vox == null) {
            voxelFace.transparent = true;
            voxelFace.type = VoxelType.AIR;
        } else {
            int addX, addY, addZ;
            addX = x;
            addY = y;
            addZ = z;
            switch (side) {
                case X_PLUS:
                    addX++;
                    break;
                case X_MINUS:
                    addX--;
                    break;
                case Z_MINUS:
                    addZ--;
                    break;
                case Z_PLUS:
                    addZ++;
                    break;
                case Y_PLUS:
                    addY++;
                    break;
                case Y_MINUS:
                    addY--;
                    break;
            }
            Vector3i pos = chunk.getPosition();
            Voxel adj = manager.getVoxel(pos.x + addX, pos.y + addY, pos.z + addZ);
            voxelFace.transparent = adj != null && !adj.getIsVisible();
            voxelFace.type = vox.getType();
        }
        voxelFace.side = side;
        return voxelFace;
    }

    private void createGreedyMesh() {
        /*
         * These are just working variables for the algorithm - almost all taken
         * directly from Mikola Lysenko's javascript implementation.
         */
        int i, j, k, l, w, h, u, v, n, side = 0;

        final int[] x = new int[]{0, 0, 0};
        final int[] q = new int[]{0, 0, 0};
        final int[] du = new int[]{0, 0, 0};
        final int[] dv = new int[]{0, 0, 0};

        /*
         * We create a mask - this will contain the groups of matching voxel faces
         * as we proceed through the chunk in 6 directions - once for each face.
         */
        final VoxelFace[] mask = new VoxelFace[CHUNK_SIZE * CHUNK_SIZE];

        /*
         * These are just working variables to hold two faces during comparison.
         */
        VoxelFace voxelFace, voxelFace1;

        /**
         * We start with the lesser-spotted boolean for-loop (also known as the old flippy floppy).
         *
         * The variable backFace will be TRUE on the first iteration and FALSE on the second - this allows
         * us to track which direction the indices should run during creation of the quad.
         *
         * This loop runs twice, and the inner loop 3 times - totally 6 iterations - one for each
         * voxel face.
         */
        for (boolean backFace = true, b = false; b != backFace; backFace = backFace && b, b = !b) {

            /*
             * We sweep over the 3 dimensions - most of what follows is well described by Mikola Lysenko
             * in his post - and is ported from his Javascript implementation.  Where this implementation
             * diverges, I've added commentary.
             */
            for (int d = 0; d < 3; d++) {

                u = (d + 1) % 3;
                v = (d + 2) % 3;

                x[0] = 0;
                x[1] = 0;
                x[2] = 0;

                q[0] = 0;
                q[1] = 0;
                q[2] = 0;
                q[d] = 1;

                /*
                 * Here we're keeping track of the side that we're meshing.
                 */
                if (d == 0) {
                    side = backFace ? X_MINUS : X_PLUS;
                } else if (d == 1) {
                    side = backFace ? Y_MINUS : Y_PLUS;
                } else if (d == 2) {
                    side = backFace ? Z_MINUS : Z_PLUS;
                }

                /*
                 * We move through the dimension from front to back
                 */
                for (x[d] = -1; x[d] < CHUNK_SIZE; ) {

                    /*
                     * -------------------------------------------------------------------
                     *   We compute the mask
                     * -------------------------------------------------------------------
                     */
                    n = 0;

                    for (x[v] = 0; x[v] < CHUNK_SIZE; x[v]++) {

                        for (x[u] = 0; x[u] < CHUNK_SIZE; x[u]++) {

                            /*
                             * Here we retrieve two voxel faces for comparison.
                             */
                            voxelFace = (x[d] >= 0) ? getVoxelFace(x[0], x[1], x[2], side) : null;
                            voxelFace1 = (x[d] < CHUNK_SIZE - 1) ? getVoxelFace(x[0] + q[0], x[1] + q[1], x[2] + q[2], side) : null;

                            /*
                             * Note that we're using the equals function in the voxel face class here, which lets the faces
                             * be compared based on any number of attributes.
                             *
                             * Also, we choose the face to add to the mask depending on whether we're moving through on a backface or not.
                             */
                            mask[n++] = ((voxelFace != null && voxelFace1 != null && voxelFace.equals(voxelFace1)))
                                    ? null
                                    : backFace ? voxelFace1 : voxelFace;
                        }
                    }

                    x[d]++;

                    /*
                     * Now we generate the mesh for the mask
                     */
                    n = 0;

                    for (j = 0; j < CHUNK_SIZE; j++) {

                        for (i = 0; i < CHUNK_SIZE; ) {

                            if (mask[n] != null) {

                                /*
                                 * We compute the width
                                 */
                                for (w = 1; i + w < CHUNK_SIZE && mask[n + w] != null && mask[n + w].equals(mask[n]); w++) {
                                }

                                /*
                                 * Then we compute height
                                 */
                                boolean done = false;

                                for (h = 1; j + h < CHUNK_SIZE; h++) {

                                    for (k = 0; k < w; k++) {

                                        if (mask[n + k + h * CHUNK_SIZE] == null || !mask[n + k + h * CHUNK_SIZE].equals(mask[n])) {
                                            done = true;
                                            break;
                                        }
                                    }

                                    if (done) {
                                        break;
                                    }
                                }

                                /*
                                 * Here we check the "transparent" attribute in the VoxelFace class to ensure that we don't mesh
                                 * any culled faces.
                                 */
                                if (!mask[n].transparent) {
                                    /*
                                     * Add quad
                                     */
                                    x[u] = i;
                                    x[v] = j;

                                    du[0] = 0;
                                    du[1] = 0;
                                    du[2] = 0;
                                    du[u] = w;

                                    dv[0] = 0;
                                    dv[1] = 0;
                                    dv[2] = 0;
                                    dv[v] = h;

                                    /*
                                     * And here we call the quad function in order to render a merged quad in the scene.
                                     *
                                     * We pass mask[n] to the function, which is an instance of the VoxelFace class containing
                                     * all the attributes of the face - which allows for variables to be passed to shaders - for
                                     * example lighting values used to create ambient occlusion.
                                     */
                                    quad(new Vector3f(x[0], x[1], x[2]),
                                            new Vector3f(x[0] + du[0], x[1] + du[1], x[2] + du[2]),
                                            new Vector3f(x[0] + du[0] + dv[0], x[1] + du[1] + dv[1], x[2] + du[2] + dv[2]),
                                            new Vector3f(x[0] + dv[0], x[1] + dv[1], x[2] + dv[2]),
                                            w,
                                            h,
                                            mask[n],
                                            backFace);
                                }

                                /*
                                 * We zero out the mask
                                 */
                                for (l = 0; l < h; ++l) {

                                    for (k = 0; k < w; ++k) {
                                        mask[n + k + l * CHUNK_SIZE] = null;
                                    }
                                }

                                /*
                                 * And then finally increment the counters and continue
                                 */
                                i += w;
                                n += w;

                            } else {

                                i++;
                                n++;
                            }
                        }
                    }
                }
            }
        }

    }

    /** ADD A QUAD TO THE LIST OF VERTICES
     *  TODO CHECKING DUPLICATE VERTICES AS THEY ARE THERE
     */
    private void quad(final Vector3f bottomLeft,
              final Vector3f topLeft,
              final Vector3f topRight,
              final Vector3f bottomRight,
              final int width,
              final int height,
              final VoxelFace voxel,
              final boolean backFace) {
        Vector3f[] vector3fs = new Vector3f[]{topLeft, bottomLeft, bottomRight, topRight};
        float[] col = voxel.type.getColour();

        Vector3f normal = new Vector3f();
        switch (voxel.side){
            case X_PLUS : normal = new Vector3f(1, 0, 0);
                break;
            case X_MINUS : normal = new Vector3f(-1, 0, 0);
                break;
            case Y_PLUS : normal = new Vector3f(0, 1, 0);
                break;
            case Y_MINUS : normal = new Vector3f(0, -1, 0);
                break;
            case Z_PLUS : normal = new Vector3f(0, 0, 1);
                break;
            case Z_MINUS : normal = new Vector3f(0, 0, -1);
                break;
        }
        for (Vector3f vector3f : vector3fs) {
            verticesBuffer.add(vector3f.x);
            verticesBuffer.add(vector3f.y);
            verticesBuffer.add(vector3f.z);

            normalsBuffer.add(normal.x);
            normalsBuffer.add(normal.y);
            normalsBuffer.add(normal.z);

            colourBuffer.add(col[0]);
            colourBuffer.add(col[1]);
            colourBuffer.add(col[2]);
        }
        if (backFace) {
            indicesBuffer.add(0 + indexOffset);
            indicesBuffer.add(1 + indexOffset);
            indicesBuffer.add(3 + indexOffset);
            indicesBuffer.add(3 + indexOffset);
            indicesBuffer.add(1 + indexOffset);
            indicesBuffer.add(2 + indexOffset);
        }else{
            indicesBuffer.add(2 + indexOffset);
            indicesBuffer.add(1 + indexOffset);
            indicesBuffer.add(3 + indexOffset);
            indicesBuffer.add(3 + indexOffset);
            indicesBuffer.add(1 + indexOffset);
            indicesBuffer.add(0 + indexOffset);
        }

        indexOffset += 4;
    }
}
