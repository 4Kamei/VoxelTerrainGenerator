#version 450

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 inColour;
layout (location = 2) in vec3 inNormal;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

out vec3 exColour;
out vec3 vertexPos;
out vec3 vertexNormal;
out vec3 localVertPos;
out vec3 localNormal;

void main()
{
    localNormal = inNormal;
    localVertPos = pos;
    exColour = inColour;
    vertexNormal = vec3(model * vec4(inNormal, 0));
    vertexPos = vec3(model * vec4(pos, 1));
    gl_Position = projection * view * vec4(vertexPos, 1.0);
}