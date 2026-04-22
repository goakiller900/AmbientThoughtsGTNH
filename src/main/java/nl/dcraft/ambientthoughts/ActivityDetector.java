package nl.dcraft.ambientthoughts;

import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class ActivityDetector {

    public PlayerContext detect(EntityPlayerMP player) {
        String playerName = player.getCommandSenderName();
        String biomeName = detectBiomeName(player);
        String dimensionName = detectDimensionName(player);
        String machineName = detectNearbyMachineName(player);
        String heldItemName = detectHeldItemName(player);
        String activityName = detectBestActivity(player, machineName);

        return new PlayerContext(playerName, biomeName, dimensionName, activityName, machineName, heldItemName);
    }

    private String detectBiomeName(EntityPlayerMP player) {
        int x = MathHelper.floor_double(player.posX);
        int z = MathHelper.floor_double(player.posZ);

        BiomeGenBase biome = player.worldObj.getBiomeGenForCoords(x, z);
        if (biome == null || biome.biomeName == null
            || biome.biomeName.trim()
                .isEmpty()) {
            return "somewhere strange";
        }

        return biome.biomeName;
    }

    private String detectDimensionName(EntityPlayerMP player) {
        if (player.dimension == -1) {
            return "the Nether";
        }
        if (player.dimension == 0) {
            return "the Overworld";
        }
        if (player.dimension == 1) {
            return "the End";
        }

        if (player.worldObj != null && player.worldObj.provider != null) {
            String name = player.worldObj.provider.getDimensionName();
            if (name != null && !name.trim()
                .isEmpty()) {
                return name;
            }
        }

        return "another dimension";
    }

    private String detectHeldItemName(EntityPlayerMP player) {
        ItemStack heldStack = player.getCurrentEquippedItem();
        if (heldStack == null) {
            return "empty hands";
        }

        String displayName = heldStack.getDisplayName();
        if (displayName == null || displayName.trim()
            .isEmpty()) {
            return "something odd";
        }

        return displayName;
    }

    private String detectBestActivity(EntityPlayerMP player, String machineName) {
        ItemStack heldStack = player.getCurrentEquippedItem();
        Item heldItem = heldStack != null ? heldStack.getItem() : null;

        boolean moving = isMoving(player);
        boolean hostileNearby = hasNearbyHostiles(player);
        boolean farmingNearby = isNearFarming(player);
        boolean chestNearby = isNearChest(player);
        boolean underground = player.posY < 45.0D;
        boolean machineNearby = machineName != null && !"nearby machinery".equalsIgnoreCase(machineName);
        boolean waterNearby = isNearWater(player);
        boolean damaged = player.getHealth() < player.getMaxHealth();

        int combatScore = 0;
        int miningScore = 0;
        int buildingScore = 0;
        int farmingScore = 0;
        int chestScore = 0;
        int machineScore = 0;
        int exploringScore = 0;
        int fishingScore = 0;
        int idleScore = 0;

        if (heldItem instanceof ItemSword || heldItem instanceof ItemBow) {
            combatScore += 4;
        }
        if (hostileNearby) {
            combatScore += 5;
        }
        if (damaged) {
            combatScore += 2;
        }

        if (heldItem instanceof ItemPickaxe) {
            miningScore += 5;
        }
        if (underground) {
            miningScore += 3;
        }
        if (isNearStoneOrOre(player)) {
            miningScore += 3;
        }
        if (!moving && underground) {
            miningScore += 1;
        }

        if (heldItem instanceof ItemBlock) {
            buildingScore += 5;
        }
        if (!moving && heldItem instanceof ItemBlock) {
            buildingScore += 2;
        }
        if (machineNearby && heldItem instanceof ItemBlock) {
            buildingScore += 1;
        }

        if (heldItem instanceof ItemHoe) {
            farmingScore += 5;
        }
        if (farmingNearby) {
            farmingScore += 4;
        }
        if (!moving && farmingNearby) {
            farmingScore += 1;
        }

        if (chestNearby && !moving) {
            chestScore += 5;
        }
        if (chestNearby && heldItem == null) {
            chestScore += 2;
        }

        if (machineNearby && !moving) {
            machineScore += 5;
        }
        if (machineNearby && !(heldItem instanceof ItemBlock)) {
            machineScore += 1;
        }

        if (heldItem instanceof ItemFishingRod) {
            fishingScore += 6;
        }
        if (heldItem instanceof ItemFishingRod && waterNearby) {
            fishingScore += 3;
        }

        if (moving) {
            exploringScore += 4;
        }
        if (moving && !hostileNearby && !underground) {
            exploringScore += 2;
        }
        if (moving && !farmingNearby && !chestNearby && !machineNearby) {
            exploringScore += 2;
        }

        if (!moving) {
            idleScore += 2;
        }
        if (!moving && heldItem == null) {
            idleScore += 1;
        }

        int bestScore = -1;
        String bestActivity = "standing around mysteriously";

        if (fishingScore > bestScore) {
            bestScore = fishingScore;
            bestActivity = "fishing";
        }

        if (combatScore > bestScore) {
            bestScore = combatScore;
            bestActivity = "fighting for your life";
        }

        if (farmingScore > bestScore) {
            bestScore = farmingScore;
            bestActivity = "farming";
        }

        if (miningScore > bestScore) {
            bestScore = miningScore;
            bestActivity = "mining";
        }

        if (buildingScore > bestScore) {
            bestScore = buildingScore;
            bestActivity = "building";
        }

        if (chestScore > bestScore) {
            bestScore = chestScore;
            bestActivity = "organizing chests";
        }

        if (machineScore > bestScore) {
            bestScore = machineScore;
            bestActivity = "working on " + machineName.toLowerCase();
        }

        if (exploringScore > bestScore) {
            bestScore = exploringScore;
            bestActivity = "exploring";
        }

        if (idleScore > bestScore) {
            bestActivity = "standing around mysteriously";
        }

        return bestActivity;
    }

    private boolean isMoving(EntityPlayerMP player) {
        double horizontalMotion = Math.abs(player.motionX) + Math.abs(player.motionZ);
        return horizontalMotion > 0.08D;
    }

    private boolean hasNearbyHostiles(EntityPlayerMP player) {
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
            player.posX - 8.0D,
            player.posY - 4.0D,
            player.posZ - 8.0D,
            player.posX + 8.0D,
            player.posY + 4.0D,
            player.posZ + 8.0D);

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, area);

        for (EntityLivingBase entity : entities) {
            if (entity == null || entity == player) {
                continue;
            }

            if (entity instanceof IMob) {
                return true;
            }
        }

        return false;
    }

    private boolean isNearFarming(EntityPlayerMP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.posY);
        int pz = MathHelper.floor_double(player.posZ);

        for (int x = px - 4; x <= px + 4; x++) {
            for (int y = py - 2; y <= py + 2; y++) {
                for (int z = pz - 4; z <= pz + 4; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block == Blocks.farmland || block == Blocks.wheat
                        || block == Blocks.carrots
                        || block == Blocks.potatoes
                        || block == Blocks.nether_wart
                        || block == Blocks.reeds
                        || block == Blocks.cactus
                        || block == Blocks.melon_block
                        || block == Blocks.pumpkin) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isNearChest(EntityPlayerMP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.posY);
        int pz = MathHelper.floor_double(player.posZ);

        for (int x = px - 4; x <= px + 4; x++) {
            for (int y = py - 2; y <= py + 2; y++) {
                for (int z = pz - 4; z <= pz + 4; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.ender_chest) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isNearWater(EntityPlayerMP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.posY);
        int pz = MathHelper.floor_double(player.posZ);

        for (int x = px - 4; x <= px + 4; x++) {
            for (int y = py - 2; y <= py + 2; y++) {
                for (int z = pz - 4; z <= pz + 4; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null && block.getMaterial() == Material.water) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isNearStoneOrOre(EntityPlayerMP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.posY);
        int pz = MathHelper.floor_double(player.posZ);

        for (int x = px - 3; x <= px + 3; x++) {
            for (int y = py - 3; y <= py + 3; y++) {
                for (int z = pz - 3; z <= pz + 3; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }

                    if (block == Blocks.stone || block == Blocks.cobblestone
                        || block == Blocks.coal_ore
                        || block == Blocks.iron_ore
                        || block == Blocks.gold_ore
                        || block == Blocks.redstone_ore
                        || block == Blocks.lit_redstone_ore
                        || block == Blocks.lapis_ore
                        || block == Blocks.diamond_ore
                        || block == Blocks.emerald_ore
                        || block == Blocks.quartz_ore) {
                        return true;
                    }

                    String localizedName = block.getLocalizedName();
                    if (localizedName != null && localizedName.toLowerCase(Locale.ROOT)
                        .contains("ore")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String detectNearbyMachineName(EntityPlayerMP player) {
        World world = player.worldObj;
        int px = MathHelper.floor_double(player.posX);
        int py = MathHelper.floor_double(player.posY);
        int pz = MathHelper.floor_double(player.posZ);

        double bestDistanceSq = Double.MAX_VALUE;
        String bestName = "nearby machinery";

        for (int x = px - 4; x <= px + 4; x++) {
            for (int y = py - 2; y <= py + 2; y++) {
                for (int z = pz - 4; z <= pz + 4; z++) {
                    TileEntity tileEntity = world.getTileEntity(x, y, z);
                    if (tileEntity == null) {
                        continue;
                    }

                    String displayName = getTileEntityDisplayName(tileEntity);
                    if (!isLikelyMachine(tileEntity, displayName)) {
                        continue;
                    }

                    double dx = (x + 0.5D) - player.posX;
                    double dy = (y + 0.5D) - player.posY;
                    double dz = (z + 0.5D) - player.posZ;
                    double distanceSq = dx * dx + dy * dy + dz * dz;

                    if (distanceSq < bestDistanceSq) {
                        bestDistanceSq = distanceSq;
                        bestName = displayName;
                    }
                }
            }
        }

        return bestName;
    }

    private boolean isLikelyMachine(TileEntity tileEntity, String displayName) {
        if (displayName == null || displayName.trim()
            .isEmpty()) {
            return false;
        }

        String lowerName = displayName.toLowerCase(Locale.ROOT);
        String className = tileEntity.getClass()
            .getName()
            .toLowerCase(Locale.ROOT);
        String simpleName = tileEntity.getClass()
            .getSimpleName()
            .toLowerCase(Locale.ROOT);

        if (containsAny(
            lowerName,
            "chest",
            "trapped chest",
            "ender chest",
            "hopper",
            "dropper",
            "dispenser",
            "sign",
            "bed")) {
            return false;
        }

        if (containsAny(simpleName, "chest", "hopper", "dropper", "dispenser", "sign", "bed")) {
            return false;
        }

        if (containsAny(
            lowerName,
            "machine",
            "generator",
            "macerator",
            "compressor",
            "lathe",
            "wiremill",
            "bender",
            "centrifuge",
            "electrolyzer",
            "mixer",
            "chemical",
            "boiler",
            "turbine",
            "reactor",
            "assembler",
            "blast furnace",
            "electric furnace",
            "furnace",
            "battery",
            "charger",
            "hatch",
            "bus",
            "controller",
            "oven",
            "forge hammer")) {
            return true;
        }

        if (containsAny(
            className,
            "gregtech",
            "machine",
            "generator",
            "macerator",
            "compressor",
            "lathe",
            "wiremill",
            "bender",
            "centrifuge",
            "electrolyzer",
            "mixer",
            "chemical",
            "boiler",
            "turbine",
            "reactor",
            "assembler",
            "furnace",
            "hatch",
            "bus",
            "controller")) {
            return true;
        }

        if (containsAny(
            simpleName,
            "machine",
            "generator",
            "macerator",
            "compressor",
            "lathe",
            "wiremill",
            "bender",
            "centrifuge",
            "electrolyzer",
            "mixer",
            "chemical",
            "boiler",
            "turbine",
            "reactor",
            "assembler",
            "furnace",
            "hatch",
            "bus",
            "controller")) {
            return true;
        }

        return false;
    }

    private boolean containsAny(String haystack, String... needles) {
        if (haystack == null) {
            return false;
        }

        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    private String getTileEntityDisplayName(TileEntity tileEntity) {
        Block block = tileEntity.getBlockType();
        if (block != null) {
            String localizedName = block.getLocalizedName();
            if (localizedName != null && !localizedName.trim()
                .isEmpty() && !localizedName.startsWith("tile.") && !localizedName.endsWith(".name")) {
                return localizedName;
            }
        }

        String simpleName = tileEntity.getClass()
            .getSimpleName();
        if (simpleName == null || simpleName.trim()
            .isEmpty()) {
            return "nearby machinery";
        }

        simpleName = simpleName.replace("TileEntity", "");
        if (simpleName.isEmpty()) {
            return "nearby machinery";
        }

        return splitCamelCase(simpleName);
    }

    private String splitCamelCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1 $2")
            .trim();
    }
}
