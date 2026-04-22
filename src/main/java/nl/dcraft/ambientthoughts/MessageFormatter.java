package nl.dcraft.ambientthoughts;

public class MessageFormatter {

    public String format(String template, PlayerContext context) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        String formatted = template;

        formatted = formatted.replace("{player}", safe(context.getPlayerName(), "player"));
        formatted = formatted.replace("{biome}", safe(context.getBiomeName(), "somewhere"));
        formatted = formatted.replace("{dimension}", safe(context.getDimensionName(), "somewhere strange"));
        formatted = formatted.replace("{activity}", safe(context.getActivityName(), "surviving"));
        formatted = formatted.replace("{machine}", safe(context.getMachineName(), "nearby machinery"));
        formatted = formatted.replace("{item}", safe(context.getHeldItemName(), "something"));

        return formatted;
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim()
            .isEmpty()) {
            return fallback;
        }
        return value;
    }
}
