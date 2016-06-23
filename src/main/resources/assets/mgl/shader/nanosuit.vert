#version 330 core

uniform mat4 uMVPMatrix;
uniform mat4 uWorldMatrix;
uniform float uScale;

layout (location=0) in vec3 vPosition;
layout (location=1) in vec2 vUV;
layout (location=2) in vec3 tangent;
layout (location=3) in vec3 normal;

out vec2 uv;
out vec3 vertexPosition;
out mat3 v_tbn;

void main() {
	gl_Position = uMVPMatrix * vec4(uScale * vPosition, 1.0);

    vertexPosition = uScale * vPosition;
	uv = vUV;

	vec3 T = normalize(tangent);
	vec3 N = normalize(normal);
	vec3 B = normalize(cross(T, N));

	v_tbn = mat3(T, B, N);
}
