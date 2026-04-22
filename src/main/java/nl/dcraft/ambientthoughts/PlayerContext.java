package nl.dcraft.ambientthoughts;

public class PlayerContext {

    private final String playerName;
    private final String biomeName;
    private final String dimensionName;
    private final String activityName;
    private final String machineName;
    private final String heldItemName;

    public PlayerContext(String playerName, String biomeName, String dimensionName, String activityName,
        String machineName, String heldItemName) {
        this.playerName = playerName;
        this.biomeName = biomeName;
        this.dimensionName = dimensionName;
        this.activityName = activityName;
        this.machineName = machineName;
        this.heldItemName = heldItemName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getBiomeName() {
        return biomeName;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getHeldItemName() {
        return heldItemName;
    }
}
