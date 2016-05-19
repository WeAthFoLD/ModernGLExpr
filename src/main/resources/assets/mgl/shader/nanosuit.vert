#version 330 core

uniform mat4 uMVPMatrix;
uniform float uScale;

layout (location=0) in vec3 vPosition;
layout (location=1) in vec2 vUV;

out vec2 uv;
out vec3 vertexPosition;

void main() {
	gl_Position = uMVPMatrix * vec4(uScale * vPosition, 1.0);

    vertexPosition = uScale * vPosition;
	uv = vUV;
}
