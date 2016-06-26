package ak.physSim.util;

import java.io.*;

/**
 * Created by Aleksander on 22/06/2016.
 */
public class Utils {
    public static String loadResource(String source) throws FileNotFoundException {
        StringBuilder b = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(new File(source)));
        reader.lines().forEachOrdered(line -> b.append(line).append("\n"));
        return b.toString();
    }
}
