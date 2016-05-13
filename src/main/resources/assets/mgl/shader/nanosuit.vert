#version 330 core

uniform mat4 uMVPMatrix;

layout (location=0) in vec3 vPosition;
layout (location=1) in vec2 vUV;

out vec2 uv;

void main() {
	gl_Position = uMVPMatrix * vec4(vPosition, 1.0);

	uv = vUV;
}
