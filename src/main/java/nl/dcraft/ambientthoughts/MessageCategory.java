package nl.dcraft.ambientthoughts;

public enum MessageCategory {

    WHOLESOME("wholesome"),
    SHOWER_THOUGHTS("shower_thoughts"),
    CHAOTIC("chaotic"),
    CREEPY("creepy"),
    GTNH_WHOLESOME("gtnh_wholesome"),
    GTNH_SHOWER_THOUGHTS("gtnh_shower_thoughts"),
    GTNH_CHAOTIC("gtnh_chaotic");

    private final String jsonKey;

    MessageCategory(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return jsonKey;
    }
}
