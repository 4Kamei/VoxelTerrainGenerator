#version 400

in vec3 vertexCol;

out vec4 fragColor;

void main()
{
    fragColor = vec4(vertexCol, 1.0);
}