/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package weathfold.moderngl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static weathfold.moderngl.Utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

/**
 * A simple GL Shader Program wrapper.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ShaderProgram {

    private boolean compiled = false;
    private int programID;
    private List<Integer> attachedShaders = new ArrayList<>();

    public ShaderProgram() {
        programID = glCreateProgram();
    }

    public void linkShader(ResourceLocation location, int type) {
        try {
            boolean loaded;
            String str = IOUtils.toString(getResourceStream(location));
            int shaderID = glCreateShader(type);
            glShaderSource(shaderID, str);
            glCompileShader(shaderID);

            int successful = glGetShaderi(shaderID, GL_COMPILE_STATUS);
            if(successful == GL_FALSE) {
                String logstr = glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH));
                log.error("Error when linking shader '" + location + "'. code: " + successful + ", Error string: \n" + logstr);
                loaded = false;
            } else {
                loaded = true;
            }

            if (loaded) {
                attachedShaders.add(shaderID);
                glAttachShader(programID, shaderID);
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    public int getProgramID() {
        return programID;
    }

    public void useProgram() {
        if(compiled) {
            glUseProgram(programID);
        } else {
            log.error("Trying to use a uncompiled program");
            throw new RuntimeException();
        }
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(getProgramID(), name);
    }

    public void compile() {
        if(compiled) {
            log.error("Trying to compile shader " + this + " twice.");
            throw new RuntimeException();
        }

        glLinkProgram(programID);

        for(Integer i : attachedShaders)
            glDetachShader(programID, i);
        attachedShaders = null;

        int status = glGetProgrami(programID, GL_LINK_STATUS);

        if(status == GL_FALSE) {
            String logstr = glGetProgramInfoLog(programID, glGetProgrami(programID, GL_INFO_LOG_LENGTH));
            log.error("Error when linking program #" + programID + ". Error code: " + status + ", Error string: " + logstr);
            throw new RuntimeException();
        }

        compiled = true;
    }

}