#version 330 core

uniform sampler2D uTexDiffuse;
uniform sampler3D uTexLightSample;
uniform sampler2D uTexLightmap;
uniform vec3 uLightTexSize;

in vec3 vertexPosition;
in vec2 uv;

void main() {
    vec3 diffuse = texture(uTexDiffuse, uv).rgb;

    vec3 texOffset = ivec3(uLightTexSize) / 2;

    vec3 sampleCoord = (vertexPosition + vec3(0, .5, 0) + texOffset) / uLightTexSize;

    vec2 lightRaw = clamp(texture(uTexLightSample, sampleCoord).rg, 0, 1);
    vec3 light = texture(uTexLightmap, lightRaw).rgb;
    vec3 another = vec3(0, 1, 0);

	// gl_FragColor = vec4(lightRaw, 0, 1.0);
	// gl_FragColor = vec4(light, 1.0);
    gl_FragColor = vec4(diffuse * light, 1.0);
}
