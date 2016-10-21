package ak.physSim.map.generator;

/**
 * Created by Aleksander on 16/09/2016.
 */
public class TerrainGenerator {
    /**
     * Generator variables
     */

    private float scale = (float) 2; //Meters per pixel

    private float cutOff = 0.10f;

    long nSeed1, nSeed2, nSeed4;

    private float[][] radialMap, noise1, noise2, noise4;

    private float[][] moistureMap, elevationMap;

    private int width, height;

    public TerrainGenerator(long nSeed1) {

        moistureMap = new float[width][height];
        nSeed2 = nSeed1 - 1;
        nSeed4 = nSeed1 - 2;
        radialMap = generateRadialMap(width, height);
        SimplexNoise.seed(nSeed1);
        noise1 = generateNoiseMap(width, height, 1);
        SimplexNoise.seed(nSeed2);
        noise2 = generateNoiseMap(width, height, 2);
        SimplexNoise.seed(nSeed4);
        noise4 = generateNoiseMap(width, height, 4);

        elevationMap = blendMaps(width, height, cutOff, radialMap, noise1, noise2, noise4);

        //TODO: Calculate moisture and heat maps
    }

    private float[][] blendMaps(int width, int height, float cutOff, float[][] radial, float[][]... maps) {
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        if (maps.length == 0)
            return null;
        if (maps.length == 1)
            return maps[0];

        float count = (float) Math.pow(maps.length, 2);
        float[][] finalMap = new float[width][height];
        for (float[][] map : maps) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    finalMap[x][y] += count * map[x][y];
                }
            }
            count /= 2;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                finalMap[x][y] /= (float) Math.pow(maps.length + 1, 2);
                finalMap[x][y] *= radialMap[y][x];
                if (finalMap[x][y] < cutOff)
                    finalMap[x][y] = cutOff;
                if (finalMap[x][y] < min)
                    min = finalMap[x][y];
                if (finalMap[x][y] > max)
                    max = finalMap[x][y];
            }
        }

        return finalMap;
    }

    private float[][] generateNoiseMap(int width, int height, float scale) {
        float[][] noiseMap = new float[width][height];
        for (int x = 0; x < noiseMap.length; x++) {
            for (int y = 0; y < noiseMap[x].length; y++) {
                noiseMap[x][y] = (float) getNoise(x * scale, y * scale);
            }
        }
        return noiseMap;
    }

    private float[][] generateRadialMap(int width, int height) {
        float[][] radial = new float[width][height];
        double maxD = (float) (width / Math.sqrt(2)); // c^2 = 2 *(a / 2) * (b / 2) = ab/2
        for (int x = 0; x < radial.length; x++) {
            for (int y = 0; y < radial[x].length; y++) {
                radial[x][y] = (float) Math.pow(1 - distance(x, y, width / 2, height / 2) / maxD, 2);
            }
        }
        return radial;
    }

    private double distance(int x, int y, int x2, int y2) {
        float dx = x - x2;
        float dy = y - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getNoise(float x, float y) {
        float mult = 1 / 1000f;
        return (0.5 + SimplexNoise.noise(mult * x, mult * y)) / 2;
    }

    public float getElevation(float x, float z) {
        if (x < 0 || z < 0 || x > width || z > height)
            return 0;
        int posX = Math.round(x);
        int posZ = Math.round(z);
        return elevationMap[posX][posZ];
    }
}