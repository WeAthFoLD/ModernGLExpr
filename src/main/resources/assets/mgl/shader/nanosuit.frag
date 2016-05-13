#version 330 core

uniform sampler2D uTexDiffuse;

in vec2 uv;

void main() {
    vec3 diffuse = texture2D(uTexDiffuse, uv).xyz;

	gl_FragColor = vec4(diffuse, 1.0);
}
