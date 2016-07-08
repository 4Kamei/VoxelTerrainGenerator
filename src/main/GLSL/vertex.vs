#version 330

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 inColour;

out vec3 exColour;

uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main()
{
    gl_Position = projectionMatrix * worldMatrix * vec4(pos, 1.0);
    exColour = inColour;

}