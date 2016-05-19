#version 330 core

uniform sampler2D uTexDiffuse;
uniform sampler3D uTexLightSample;
uniform sampler2D uTexLightmap;
uniform vec3 uLightTexSize;

in vec3 vertexPosition;
in vec2 uv;

void main() {
    vec3 texOffset = ivec3(uLightTexSize) / 2;

    vec3 sampleCoord = (vertexPosition + vec3(0, .5, 0) + texOffset) / uLightTexSize;

    vec4 lightRaw = texture(uTexLightSample, sampleCoord);
    vec3 light = texture(uTexLightmap, lightRaw.xy).rgb;
	vec3 diffuse = texture2D(uTexDiffuse, uv).rgb;

    gl_FragColor = vec4(light * diffuse, 1.0);
}
