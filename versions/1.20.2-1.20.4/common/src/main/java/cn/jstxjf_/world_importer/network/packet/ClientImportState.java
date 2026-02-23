package cn.jstxjf_.world_importer.network.packet;

public class ClientImportState {
    private static int completed = 0;
    private static int total = 0;
    private static String status = "idle";
    private static double serverTps = 20.0;

    public static void updateStatus(int completed, int total, String status, double tps) {
        ClientImportState.completed = completed;
        ClientImportState.total = total;
        ClientImportState.status = status;
        ClientImportState.serverTps = tps;
    }

    public static int getCompleted() { return completed; }
    public static int getTotal() { return total; }
    public static String getStatus() { return status; }
    public static double getServerTps() { return serverTps; }

    public static boolean isImporting() { return "importing".equals(status); }
    public static boolean isDone() { return "done".equals(status); }

    public static int getProgressPercent() {
        if (total == 0) return 0;
        return (completed * 100) / total;
    }

    public static void reset() {
        completed = 0;
        total = 0;
        status = "idle";
        serverTps = 20.0;
    }
}
