package tools;

public abstract class FPSCounter {
    private static int updates = 0;
    private static int frames = 0;
    private static int updateLoops = 0;
    private static String statistics = "Just a moment...";

    public static void incrementUpdates() {
        updates++;
    }

    public static void incrementFrames() {
        frames++;
    }

    public static void incrementUpdateLoops() {
        updateLoops++;
    }

    public static void writeStatistics() {
        statistics =  "Updates: " + updates + ", " +
                "FPS: " + frames + ", " +
                "UpdLoops: " + updateLoops;
        updates = 0;
        frames = 0;
        updateLoops = 0;
    }

    public static String getStatistics() {
        return statistics;
    }
}
