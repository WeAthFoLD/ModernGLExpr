#version 330

uniform mat4 wvp;

layout (location = 0) in vec2 pos;
layout (location = 1) in vec2 inuv;

out vec2 uv;

void main() {
    gl_Position = wvp * vec4(pos, 0, 1);
    uv = inuv;
}