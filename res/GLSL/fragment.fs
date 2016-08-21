#version 450

in vec3 exColour;
in vec3 vertexPos;
in vec3 vertexNormal;
in vec3 localVertPos;
in vec3 localNormal;
struct AreaLight {
                   vec3 colIntensities;
                   vec3 position;
                   float power;
};

struct DirectionalLight {
                    vec3 direction;
                    vec3 colIntensity;
                    float power;
};

uniform int voxelLight[4096]; //TODO: pass from vertex shader instead

uniform AreaLight light;

out vec4 fragColor;

void main()
{
    vec3 vertPos = localVertPos + localNormal;
    int index = int(vertPos.x) + int(vertPos.y)*16+ int(vertPos.z)*256;
    int lightIntensity = voxelLight[index];

    vec3 normal = vertexNormal;
    vec3 surfaceToLight = light.position - vertexPos;

    float brightness = light.power *  dot(normal, surfaceToLight)/ (length(normal) * length(surfaceToLight) * length(surfaceToLight));
    brightness = clamp(brightness, 0, 1);
    fragColor = vec4((brightness * 0.001 + 0.500 * lightIntensity/16 + 0.499) * light.colIntensities * exColour, 1.0);

}
