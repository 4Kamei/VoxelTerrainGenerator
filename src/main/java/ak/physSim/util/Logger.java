package ak.physSim.util;

import java.io.PrintStream;
import java.util.Date;

/**
 * Created by Aleksander on 27/10/2015.
 */
public class Logger {
    private static PrintStream out =  System.out;
    private static String name = Reference.GAME_NAME;
    private static boolean printlog = true;

    public enum LogLevel {
        ALL (1, "All"),
        DEBUG (2, "DEBUG"),
        ERROR (3, "ERROR");

        public int id;
        public String name;

        LogLevel(int id, String name){
            this.id = id;
            this.name = name;
        }
    }

    public static void toggleLog(){
        printlog = !printlog;
    }
    public static void log(LogLevel level, String mess){
        String[] messages = mess.split("\n");
        for (String message : messages) {
            Date now = new Date();
            String s = String.format("[%tD %tT][" + name + "][" + level.name + "] : " + message, now, now);
            System.out.println(s);
        }
    }
}
