#version 450

in vec3 exColour;
in vec3 vertexPos;
in vec3 vertexNormal;

//Above line creates an object type with 2 fields: position and colIntensities.


struct AreaLight {
                   vec3 colIntensities;
                   vec3 position;
};

uniform AreaLight light;

uniform mat4 model;


out vec4 fragColor;

void main(void)
{

    mat3 normalMat = transpose(inverse(mat3(model)));
    vec3 normal = normalize(normalMat * vertexNormal);
    //TODO: Make this normalMat a uniform to save computation power;

    vec3 fragPosition = vec3(model * vec4(vertexPos, 1));

    vec3 surfaceToLight = light.position - vertexPos;

    float brightness = dot(normal, surfaceToLight) / (length(surfaceToLight));
    brightness = clamp(brightness, 0, 1);
    fragColor = vec4(brightness * exColour, 1.0);

}
