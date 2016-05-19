#version 330

uniform sampler2D lightTexSample;

in vec2 uv;

void main() {
    gl_FragColor = texture(lightTexSample, uv);
}