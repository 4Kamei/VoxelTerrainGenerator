#version 400


out vec4 fragColor;

void main()
{
    gl_FragDepth = gl_FragCoord.z;
    fragColor = vec4(1.0, 0.0, 1.0, 1.0);
}

