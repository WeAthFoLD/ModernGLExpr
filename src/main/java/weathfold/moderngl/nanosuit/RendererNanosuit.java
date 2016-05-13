package weathfold.moderngl.nanosuit;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import weathfold.moderngl.ObjModel;
import weathfold.moderngl.ObjModel.Face;
import weathfold.moderngl.ObjModel.Vertex;
import weathfold.moderngl.ObjParser;
import weathfold.moderngl.ShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static weathfold.moderngl.Utils.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RendererNanosuit extends TileEntitySpecialRenderer {

    private final ObjModel model;

    private final Map<String, TexGroup> texGroups = new HashMap<>();

    private int VAO;

    private int VBO;
    private final Map<String, Integer> IBOs = new HashMap<>();

    private final ShaderProgram shader = new ShaderProgram();

    private boolean init = false;

    private final int posMVPMatrix, posTexDiffuse;

    private Matrix4f
            modelViewMatrix = new Matrix4f(),
            projectionMatrix = new Matrix4f(),
            mvpMatrix = new Matrix4f();

    public RendererNanosuit() {
        model = ObjParser.parse(new ResourceLocation("mgl:mdl/nanosuit2.obj"));

        shader.linkShader(getShader("nanosuit.vert"), GL_VERTEX_SHADER);
        shader.linkShader(getShader("nanosuit.frag"), GL_FRAGMENT_SHADER);
        shader.compile();

        posMVPMatrix = shader.getUniformLocation("uMVPMatrix");
        posTexDiffuse = shader.getUniformLocation("uTexDiffuse");

        glUseProgram(shader.getProgramID());
        glUniform1i(posTexDiffuse, 0);
        glUseProgram(0);

        newTexGroup("Helmet", "helmet");
        newTexGroup("Visor", "glass");
        newTexGroup("Lights", "glass");
        newTexGroup("Body", "body");
        newTexGroup("Arms", "arm");
        newTexGroup("Hands", "hand");
        newTexGroup("Legs", "leg");
        newTexGroup("hands", "hand");
    }

    private void newTexGroup(String name, String texname) {
        texGroups.put(name, new TexGroup(texname));
    }

    private class TexGroup {

        public final ResourceLocation spec, diff, normal;

        TexGroup(String name) {
            spec = getTexture(name + "_spec");
            diff = getTexture(name + "_dif");
            normal = getTexture(name + "_ddn");
        }

    }

    private void checkInit() {
        if (init && glIsVertexArray(VAO)) {
            return;
        }

        // Setup VAO
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);


        {   // Upload vertex data
            VBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, VBO);

            FloatBuffer buffer = BufferUtils.createFloatBuffer(model.vertices.size() * 5);
            for (Vertex v : model.vertices) {
                v.store(buffer);
            }
            buffer.flip();

            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

            // Init vertex attribute layout
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);

            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        {   // Upload indice (mesh) data for each group
            for (String group : model.faces.keySet()) {
                Collection<Face> faces = model.faces.get(group);

                int IBO = glGenBuffers();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);

                IntBuffer buffer = BufferUtils.createIntBuffer(faces.size() * 3);
                for (Face f : faces) {
                    f.store(buffer);
                }
                buffer.flip();

                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

                IBOs.put(group, IBO);
            }

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        // Finish setup VAO
        glBindVertexArray(0);

        init = true;
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float pt) {
        checkInit();

        final double s = 0.15;
        final int lastShader = glGetInteger(GL_CURRENT_PROGRAM);
        final TextureManager textureManager = texManager();

        glPushMatrix();

        glTranslated(x, y, z);
        glScaled(s, s, s);

        // Update MVP matrix
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        glGetFloat(GL_MODELVIEW_MATRIX, buffer);
        modelViewMatrix.load(buffer);
        buffer.flip();

        glGetFloat(GL_PROJECTION_MATRIX, buffer);
        projectionMatrix.load(buffer);
        buffer.flip();

        Matrix4f.mul(projectionMatrix, modelViewMatrix, mvpMatrix);
        mvpMatrix.store(buffer);
        buffer.flip();

        // Bind the shader
        glUseProgram(shader.getProgramID());

        // Upload uniform
        glUniformMatrix4(posMVPMatrix, false, buffer);

        // Bind VAO
        glBindVertexArray(VAO);

        // render groups!
        for (String group : model.faces.keySet()) {
            TexGroup texs = texGroups.get(group);
            if (texs == null) {
                continue;
            }

            textureManager.bindTexture(texs.diff);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBOs.get(group));
            glDrawElements(GL_TRIANGLES, model.faces.get(group).size() * 3, GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);

        glUseProgram(lastShader);

        glPopMatrix();
    }

}
