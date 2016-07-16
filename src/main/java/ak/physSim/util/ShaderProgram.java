package ak.physSim.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GLUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
//Code from https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter4/chapter4.html

public class ShaderProgram {

    private final int programId;

    private final Map<String, Integer> uniforms;

    private int vertexShaderId;

    private int fragmentShaderId;

    public ShaderProgram() throws Exception {
        uniforms = new HashMap<>();
        programId = glCreateProgram();
        Logger.log(Logger.LogLevel.DEBUG, "Shader pID = " + programId);
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
    }

    public void createLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".colIntensities");
        createUniform(uniformName + ".position");
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        Logger.log(Logger.LogLevel.DEBUG, uniformName + " location " + uniformLocation);
        if (uniformLocation < 0){
            Logger.log(Logger.LogLevel.ERROR, "Could not find uniform " + uniformName);
        } else {
            uniforms.put(uniformName, uniformLocation);
        }
    }

    public void setUniform(String uniformName, Matrix4f value)  {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        if (uniforms.containsKey(uniformName))
            glUniformMatrix4fv(uniforms.get(uniformName), false, buffer);
        else
            Logger.log(Logger.LogLevel.ERROR, "Uniform name not found " + uniformName);
    }

    public void setUniform(String uniformName, Vector3f value) {
        if (uniforms.containsKey(uniformName)) {
            glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
        } else
            Logger.log(Logger.LogLevel.ERROR, "Uniform name not found " + uniformName);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
        Logger.log(Logger.LogLevel.DEBUG, "Vertex Shader ID = " + vertexShaderId);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
        Logger.log(Logger.LogLevel.DEBUG, "Fragment Shader ID = " + fragmentShaderId);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Code: " + shaderId);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetShaderInfoLog(programId, 1024));
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetShaderInfoLog(programId, 1024));
        }

    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            if (vertexShaderId != 0) {
                glDetachShader(programId, vertexShaderId);
            }
            if (fragmentShaderId != 0) {
                glDetachShader(programId, fragmentShaderId);
            }
            glDeleteProgram(programId);
        }
    }
}