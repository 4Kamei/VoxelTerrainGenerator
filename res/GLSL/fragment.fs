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

uniform int voxelLight[5832]; //TODO: pass from vertex shader instead

uniform AreaLight light;

out vec4 fragColor;

void main()
{
    vec3 pos = localVertPos + localNormal + vec3(0.5);
    int index = int(pos.x) + int(pos.y) * 18 + int(pos.z) * 324;
    vec3 lighting = vec3(15, 0, 0);

    if (index < 5832) {
        lighting = vec3(voxelLight[index]);
    }

    if((int(pos.y) == 0 && int(pos.x) == 0) || (int(pos.y) == 0 && int(pos.z) == 0) || (int(pos.z) == 0 && int(pos.x) == 0)) {
        lighting = vec3(15, 0, 15);
    }

    if(mod(pos.x, 1) > 0.98 || mod(pos.y, 1) > 0.98 || mod(pos.z, 1) > 0.98) {
        lighting = vec3(0, 0, 15);
    }

    if (index < 0 || index >= 5832) {
        lighting = vec3(0, 15, 0);
    }

    fragColor = vec4((1.0/15 * lighting), 1.0);

}
