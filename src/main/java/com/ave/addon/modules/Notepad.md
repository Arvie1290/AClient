private final Setting<List<Block>> overworldFinder = sgGeneral.add(new BlockListSetting.Builder()
.name("overworld-finder")
.description("Unnatural blocks to search for in the Overworld.")
.defaultValue(List.of(
// Workstations
Blocks.CRAFTING_TABLE,
Blocks.FURNACE,
Blocks.BLAST_FURNACE,
Blocks.SMOKER,
Blocks.STONECUTTER,
Blocks.GRINDSTONE,
Blocks.SMITHING_TABLE,
Blocks.CARTOGRAPHY_TABLE,
Blocks.FLETCHING_TABLE,
Blocks.LOOM,
Blocks.CRAFTER,

            // Utility
            Blocks.ENCHANTING_TABLE,
            Blocks.BREWING_STAND,
            Blocks.BEACON,
            Blocks.LODESTONE,
            Blocks.RESPAWN_ANCHOR,
            Blocks.CONDUIT,
            Blocks.END_ROD,

            // Redstone
            Blocks.REDSTONE_BLOCK,
            Blocks.REDSTONE_TORCH,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.OBSERVER,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.TARGET,
            Blocks.NOTE_BLOCK,
            Blocks.JUKEBOX,
            Blocks.REDSTONE_LAMP,

            // Metal
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,

            // Lighting
            Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE,
            Blocks.LANTERN,
            Blocks.SOUL_LANTERN,
            Blocks.SEA_LANTERN,

            // Doors
            Blocks.IRON_DOOR,
            Blocks.IRON_TRAPDOOR,

            // Misc
            Blocks.CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.POWDER_SNOW_CAULDRON,
            Blocks.COMPOSTER,
            Blocks.LECTERN,
            Blocks.DECORATED_POT,

            // Valuable
            Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.NETHERITE_BLOCK,

            // Beds
            Blocks.WHITE_BED,
            Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED,
            Blocks.LIGHT_BLUE_BED,
            Blocks.YELLOW_BED,
            Blocks.LIME_BED,
            Blocks.PINK_BED,
            Blocks.GRAY_BED,
            Blocks.LIGHT_GRAY_BED,
            Blocks.CYAN_BED,
            Blocks.PURPLE_BED,
            Blocks.BLUE_BED,
            Blocks.BROWN_BED,
            Blocks.GREEN_BED,
            Blocks.RED_BED,
            Blocks.BLACK_BED
        ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> netherFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("nether-finder")
        .description("Unnatural blocks to search for in the Nether.")
        .defaultValue(List.of(
            // Impossible in Nether
            Blocks.WATER,

            // Workstations
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.STONECUTTER,
            Blocks.GRINDSTONE,
            Blocks.SMITHING_TABLE,
            Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE,
            Blocks.LOOM,
            Blocks.CRAFTER,

            // Utility
            Blocks.ENCHANTING_TABLE,
            Blocks.BREWING_STAND,
            Blocks.BEACON,
            Blocks.LODESTONE,
            Blocks.RESPAWN_ANCHOR,
            Blocks.CONDUIT,

            // Redstone
            Blocks.REDSTONE_BLOCK,
            Blocks.REDSTONE_TORCH,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.OBSERVER,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.TARGET,
            Blocks.NOTE_BLOCK,
            Blocks.JUKEBOX,
            Blocks.REDSTONE_LAMP,

            // Metal
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,

            // Doors & Trapdoors
            Blocks.IRON_DOOR,
            Blocks.IRON_TRAPDOOR,

            // Misc
            Blocks.COMPOSTER,
            Blocks.LECTERN,
            Blocks.CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.POWDER_SNOW_CAULDRON,
            Blocks.DECORATED_POT,

            // Valuable
            Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.NETHERITE_BLOCK,

            // Beds
            Blocks.WHITE_BED,
            Blocks.ORANGE_BED,
            Blocks.MAGENTA_BED,
            Blocks.LIGHT_BLUE_BED,
            Blocks.YELLOW_BED,
            Blocks.LIME_BED,
            Blocks.PINK_BED,
            Blocks.GRAY_BED,
            Blocks.LIGHT_GRAY_BED,
            Blocks.CYAN_BED,
            Blocks.PURPLE_BED,
            Blocks.BLUE_BED,
            Blocks.BROWN_BED,
            Blocks.GREEN_BED,
            Blocks.RED_BED,
            Blocks.BLACK_BED
        ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> endFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("the-end-finder")
        .description("Unnatural blocks to search for in The End.")
            .defaultValue(List.of(
                // Workstations
                Blocks.CRAFTING_TABLE,
                Blocks.FURNACE,
                Blocks.BLAST_FURNACE,
                Blocks.SMOKER,
                Blocks.STONECUTTER,
                Blocks.GRINDSTONE,
                Blocks.SMITHING_TABLE,
                Blocks.CARTOGRAPHY_TABLE,
                Blocks.FLETCHING_TABLE,
                Blocks.LOOM,
                Blocks.CRAFTER,

                // Utility
                Blocks.ENCHANTING_TABLE,
                Blocks.BREWING_STAND,
                Blocks.BEACON,
                Blocks.LODESTONE,
                Blocks.RESPAWN_ANCHOR,
                Blocks.CONDUIT,

                // Redstone
                Blocks.REDSTONE_BLOCK,
                Blocks.REDSTONE_TORCH,
                Blocks.REPEATER,
                Blocks.COMPARATOR,
                Blocks.OBSERVER,
                Blocks.DISPENSER,
                Blocks.DROPPER,
                Blocks.PISTON,
                Blocks.STICKY_PISTON,
                Blocks.DAYLIGHT_DETECTOR,
                Blocks.TARGET,
                Blocks.NOTE_BLOCK,
                Blocks.JUKEBOX,
                Blocks.CRAFTER,

                // Metal
                Blocks.ANVIL,
                Blocks.CHIPPED_ANVIL,
                Blocks.DAMAGED_ANVIL,

                // Doors & Trapdoors
                Blocks.OAK_DOOR,
                Blocks.IRON_DOOR,
                Blocks.OAK_TRAPDOOR,
                Blocks.IRON_TRAPDOOR,

                // Lighting
                Blocks.LANTERN,
                Blocks.SOUL_LANTERN,
                Blocks.SEA_LANTERN,
                Blocks.REDSTONE_LAMP,

                // Misc
                Blocks.COMPOSTER,
                Blocks.LECTERN,
                Blocks.CAULDRON,
                Blocks.WATER_CAULDRON,
                Blocks.LAVA_CAULDRON,
                Blocks.POWDER_SNOW_CAULDRON,
                Blocks.DECORATED_POT,

                // Valuable
                Blocks.DIAMOND_BLOCK,
                Blocks.EMERALD_BLOCK,
                Blocks.IRON_BLOCK,
                Blocks.GOLD_BLOCK,
                Blocks.NETHERITE_BLOCK,

                // Beds
                Blocks.WHITE_BED,
                Blocks.ORANGE_BED,
                Blocks.MAGENTA_BED,
                Blocks.LIGHT_BLUE_BED,
                Blocks.YELLOW_BED,
                Blocks.LIME_BED,
                Blocks.PINK_BED,
                Blocks.GRAY_BED,
                Blocks.LIGHT_GRAY_BED,
                Blocks.CYAN_BED,
                Blocks.PURPLE_BED,
                Blocks.BLUE_BED,
                Blocks.BROWN_BED,
                Blocks.GREEN_BED,
                Blocks.RED_BED,
                Blocks.BLACK_BED
            ))
        .visible(manualThreeDim::get)
        .build()
    );

    private final Setting<List<Block>> allDimFinder = sgGeneral.add(new BlockListSetting.Builder()
        .name("all-dimension-finder")
        .description("Blocks to search for across all dimensions.")
        .defaultValue(List.of(
            // Workstations
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.BLAST_FURNACE,
            Blocks.SMOKER,
            Blocks.STONECUTTER,
            Blocks.GRINDSTONE,
            Blocks.SMITHING_TABLE,
            Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE,
            Blocks.LOOM,

            // Utility
            Blocks.ENCHANTING_TABLE,
            Blocks.BREWING_STAND,
            Blocks.BEACON,
            Blocks.LODESTONE,
            Blocks.RESPAWN_ANCHOR,

            // Redstone
            Blocks.REDSTONE_BLOCK,
            Blocks.OBSERVER,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.REPEATER,
            Blocks.COMPARATOR,

            // Metal
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,

            // Valuable
            Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.NETHERITE_BLOCK,

            // Misc
            Blocks.CONDUIT
        ))
        .visible(() -> !manualThreeDim.get())
        .build()
    );
