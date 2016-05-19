package weathfold.moderngl.nanosuit;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import weathfold.moderngl.ObjModel;
import weathfold.moderngl.ObjModel.Face;
import weathfold.moderngl.ObjModel.Vertex;
import weathfold.moderngl.ObjParser;
import weathfold.moderngl.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.*;
import static weathfold.moderngl.Utils.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RendererNanosuit extends TileEntitySpecialRenderer {

    private static final int sampleSize = 6;
    private static final int sampleArea = sampleSize * sampleSize * sampleSize;

    private final ObjModel model;

    private final Map<String, TexGroup> texGroups = new HashMap<>();

    private int VAO;

    private int VBO;
    private final Map<String, Integer> IBOs = new HashMap<>();

    private final ShaderProgram shader = new ShaderProgram();

    private boolean init = false;

    private final int posMVPMatrix, posTexDiffuse,
            posLightSample, posLightTexSize, posLightmap;

    private Matrix4f
            modelViewMatrix = new Matrix4f(),
            projectionMatrix = new Matrix4f(),
            mvpMatrix = new Matrix4f();

    private final int lightSampleTex;

    private final ShaderProgram shaderLightmapTest = new ShaderProgram();

    private int VAO2, VBO2, IBO2;

    public RendererNanosuit() {
        model = ObjParser.parse(new ResourceLocation("mgl:mdl/nanosuit2.obj"));

        shader.linkShader(getShader("nanosuit.vert"), GL_VERTEX_SHADER);
        shader.linkShader(getShader("nanosuit.frag"), GL_FRAGMENT_SHADER);
        shader.compile();

        shaderLightmapTest.linkShader(getShader("lms.vert"), GL_VERTEX_SHADER);
        shaderLightmapTest.linkShader(getShader("lms.frag"), GL_FRAGMENT_SHADER);
        shaderLightmapTest.compile();

        posMVPMatrix = shader.getUniformLocation("uMVPMatrix");
        posTexDiffuse = shader.getUniformLocation("uTexDiffuse");
        posLightSample = shader.getUniformLocation("uTexLightSample");
        posLightTexSize = shader.getUniformLocation("uLightTexSize");
        posLightmap = shader.getUniformLocation("uTexLightmap");

        glUseProgram(shader.getProgramID());
        glUniform1i(posTexDiffuse, 0);
        glUniform1i(posLightSample, 0);
        glUniform3f(posLightTexSize, sampleSize, sampleSize, sampleSize);
        glUniform1f(shader.getUniformLocation("uScale"), 0.15f);
        glUseProgram(0);

        newTexGroup("Helmet", "helmet");
        newTexGroup("Visor", "glass");
        newTexGroup("Lights", "glass");
        newTexGroup("Body", "body");
        newTexGroup("Arms", "arm");
        newTexGroup("Hands", "hand");
        newTexGroup("Legs", "leg");
        newTexGroup("hands", "hand");

        lightSampleTex = glGenTextures();

        glBindTexture(GL_TEXTURE_3D, lightSampleTex);

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glBindTexture(GL_TEXTURE_3D, 0);
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

        // Debug setups



        VAO2 = glGenVertexArrays();

        glBindVertexArray(VAO2);

        VBO2 = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO2);
        {
            float s = 4;
            FloatBuffer buf = BufferUtils.createFloatBuffer(4 * 4);
            buf.put(new float[] {
                    0, 0, 0, 0,
                    0, s, 0, 1,
                    s, s, 1, 1,
                    s, 0, 1, 0
            });
            buf.flip();
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);

            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);

            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        IBO2 = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO2);
        {
            ByteBuffer buf = BufferUtils.createByteBuffer(6);
            buf.put(new byte[] { 0, 1, 2, 0, 2, 3 });
            buf.flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glBindVertexArray(0);




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

        final int lastShader = glGetInteger(GL_CURRENT_PROGRAM);
        final TextureManager textureManager = texManager();

        glPushMatrix();

        glTranslated(x, y, z);

        updateLightMap(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

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

        { // Debug

            glUseProgram(shaderLightmapTest.getProgramID());
            glUniform1i(shaderLightmapTest.getUniformLocation("lightTexSample"), 1);
            glUniformMatrix4(shaderLightmapTest.getUniformLocation("wvp"), false, buffer);

            glBindVertexArray(VAO2);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO2);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 0);

            glBindVertexArray(0);

            glUseProgram(0);

        }


        // Bind the shader
        glUseProgram(shader.getProgramID());

        glActiveTexture(GL_TEXTURE1);
        int bound = glGetInteger(GL_TEXTURE_BINDING_2D);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, bound);
        // texManager().bindTexture(texGroups.get("Visor").diff);

        glActiveTexture(GL_TEXTURE0);

        glUniform1i(posLightmap, 2);

        // Upload uniform
        glUniformMatrix4(posMVPMatrix, false, buffer);

        // Bind VAO
        glBindVertexArray(VAO);

        // Bind light sample texture
        glBindTexture(GL_TEXTURE_3D, lightSampleTex);

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

        glBindTexture(GL_TEXTURE_3D, 0);

        glBindVertexArray(0);

        glUseProgram(lastShader);

        glPopMatrix();
    }

    private void updateLightMap(World world, int x0, int y0, int z0) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(sampleArea * 2);
        final int halfSize = sampleSize / 2;

        float maxBrightness = -1;

        for (int zz = 0; zz < sampleSize; ++zz) {
            for (int yy = 0; yy < sampleSize; ++yy) {
                for (int xx = 0; xx < sampleSize; ++xx) {
                    int i = world.getLightBrightnessForSkyBlocks(x0 - halfSize + xx,
                            y0 - halfSize + yy,
                            z0 - halfSize + zz, 0);
                    int j = i % 65536;
                    int k = i / 65536;

                    // buffer.put(brightness);
                    buffer.put(j / 240.0f).put(k / 240.0f);
                }
            }
        }
        buffer.flip();

        glBindTexture(GL_TEXTURE_3D, lightSampleTex);
        glTexImage3D(GL_TEXTURE_3D, 0, GL_RG,
                sampleSize, sampleSize, sampleSize,
                0, GL_RG, GL_FLOAT, buffer);
    }

}
