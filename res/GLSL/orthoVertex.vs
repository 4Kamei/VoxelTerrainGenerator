#version 450

layout (location = 1) in vec3 position;
layout (location = 2) in vec3 colour;

out vec3 vertexCol;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main()
{
    vertexCol = colour;
    gl_Position = projection * view * model * vec4(position, 1.0);
}