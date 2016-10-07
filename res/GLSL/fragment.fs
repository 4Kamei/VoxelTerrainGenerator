#version 400

in vec3 exColour;
in vec3 vertexPos;
in vec3 vertexNormal;
in vec3 localVertPos;
in vec3 localNormal;
in vec4 mlightviewVertexPos;

out vec4 fragColor;

uniform sampler2Dshadow shadowMap;

float calcShadow(vec4 position)
{
    vec3 projCoords = position.xyz;
    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    //float theta = acos( clamp( dot(localNormal,
    float bias = 0.0;

    float shadowFactor = 0.0;

    int size = 3;

    vec2 inc = 1.0 / textureSize(shadowMap, 0);

    for(int row = -size; row <= size; ++row)
    {
        for(int col = -size; col <= size; ++col)
        {
            float textDepth = texture(shadowMap, projCoords.xy + vec2(row, col) * inc).r;
            shadowFactor += projCoords.z - bias > textDepth ? 1 : 0.0;
        }
    }

    shadowFactor /= 4 * (size + 1) * (size + 1);

    return 1 - shadowFactor;
}
//S = 0, N = 1
//S = 1, N = 9
//S = 2, N = 25

void main()
{
    float shading = 1;
    if(abs(localNormal.x) == 1) {
        shading = 0.6;
    }
    if(abs(localNormal.y) == 1) {
        shading = 0.8;
    }
    if(abs(localNormal.z) == 1) {
        shading = 0.4;
    }
    float shadow = calcShadow(mlightviewVertexPos);
    //fragColor = vec4(shading);
    fragColor = vec4(exColour * (0.6*shadow + 0.2*shading + 0.2), 1.0);
}


