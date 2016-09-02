package ak.physSim.render;

import ak.physSim.render.meshes.FullMesh;
import ak.physSim.render.meshes.Mesh;
import ak.physSim.render.meshes.SimpleMesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

//Code from http://stackoverflow.com/a/39043788/6513503
//Made by Hello World
public class Text {

    static SimpleMesh rectMesh = new SimpleMesh(new float[]{
            0,  1f, 0,
            1f, 1f, 0,
            1f, 0,  0,
            0,  0,  0
    },new int[]{
            0,
            1,
            2,
            3
    });

    public static void drawString(String s, float x, float y, float scale, float width) {

        glTranslatef(x, y, 0);

        glBindVertexArray(rectMesh.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            glTranslatef(width * i + x, y, 0);
            glDrawElements(GL_TRIANGLES, rectMesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        }

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

}
