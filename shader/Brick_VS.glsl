#version 150 core

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;
uniform mat4 modelIT;

in vec3 positionMC;
in vec3 normalMC;

uniform vec3 LightPosition;

out float LightIntensity;
out vec2 MCposition;

const float SpecularContribution = 0.3;
const float DiffuseContribution = 1.0 - SpecularContribution;

void main(void) {
	vec3 ecPosition = vec3(view * model * vec4(positionMC, 1));
//	vec3 tnorm = normalize(vec3(view * modelIT * vec4(normalMC, 1)));
	vec3 tnorm = normalize(vec3(transpose(inverse(view * model)) * vec4(normalMC, 1)));
	
	vec3 lightVec = normalize(vec3(LightPosition - ecPosition));
	
	vec3 reflectVec = reflect(-lightVec, tnorm);
	
	vec3 viewVec = normalize(-ecPosition);
	
	float diffuse = max(dot(lightVec, tnorm), 0.0f);
	float spec = 0.0;
	if (diffuse > 0.0)
	{
		spec = max(dot(reflectVec, viewVec), 0.0f);
		spec = pow(spec, 32.0);
	}
	
	LightIntensity = DiffuseContribution * diffuse + SpecularContribution * spec;
	
	MCposition = positionMC.xy;

    gl_Position = projection * view * model * vec4(positionMC, 1);
}