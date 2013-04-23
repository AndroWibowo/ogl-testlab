#version 150 core

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;
uniform mat4 modelIT;

in vec3 positionMC;

void main(void) {
    gl_Position = projection * view * model * vec4(positionMC, 1);
}