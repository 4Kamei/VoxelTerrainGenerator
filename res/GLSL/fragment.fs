#version 450

in vec3 exColour;
in vec3 vertexPos;
in vec3 vertexNormal;


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
uniform AreaLight light;

uniform mat4 model;

out vec4 fragColor;

void main(void)
{
     //Removing the scaling breaks it
    vec3 normal = vertexNormal;
    vec3 surfaceToLight = light.position - vertexPos;

    float brightness = light.power *  dot(normal, surfaceToLight)/ (length(normal) * length(surfaceToLight) * length(surfaceToLight));
    brightness = clamp(brightness, 0, 1);
    fragColor = vec4((brightness) * light.colIntensities * exColour, 1.0);

}
