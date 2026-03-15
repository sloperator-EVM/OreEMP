package com.oreemp;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public record OreType(Block block, int color, int count, int size, int minY, int maxY, float discardOnAir, float rarity, int index, int step, boolean scattered) {
    public static final OreType[] OVERWORLD = new OreType[] {
        new OreType(Blocks.COAL_ORE, 0x111111, 30, 17, 136, 320, 0f, 1f, 0, 6, false),
        new OreType(Blocks.DEEPSLATE_COAL_ORE, 0x222222, 20, 17, 0, 192, 0f, 1f, 1, 6, false),
        new OreType(Blocks.IRON_ORE, 0xD8AF93, 90, 9, 80, 384, 0f, 1f, 2, 6, false),
        new OreType(Blocks.DEEPSLATE_IRON_ORE, 0x8B6E5A, 10, 4, -64, 72, 0f, 1f, 3, 6, false),
        new OreType(Blocks.COPPER_ORE, 0xCB6D51, 16, 10, -16, 112, 0f, 1f, 4, 6, false),
        new OreType(Blocks.DEEPSLATE_COPPER_ORE, 0x9A4F3B, 16, 10, -64, 16, 0f, 1f, 5, 6, false),
        new OreType(Blocks.GOLD_ORE, 0xE6D966, 4, 9, -64, 32, 0f, 1f, 6, 6, false),
        new OreType(Blocks.DEEPSLATE_GOLD_ORE, 0xB79A3E, 4, 9, -64, 0, 0f, 1f, 7, 6, false),
        new OreType(Blocks.REDSTONE_ORE, 0xB80F0A, 8, 8, -64, 16, 0f, 1f, 8, 6, false),
        new OreType(Blocks.DEEPSLATE_REDSTONE_ORE, 0x8D0B07, 8, 8, -64, 0, 0f, 1f, 9, 6, false),
        new OreType(Blocks.LAPIS_ORE, 0x3552CC, 2, 7, -64, 64, 0f, 1f, 10, 6, false),
        new OreType(Blocks.DEEPSLATE_LAPIS_ORE, 0x24398E, 4, 7, -64, 32, 0f, 1f, 11, 6, false),
        new OreType(Blocks.DIAMOND_ORE, 0x45D7D8, 7, 8, -64, 16, 0f, 1f, 12, 6, false),
        new OreType(Blocks.DEEPSLATE_DIAMOND_ORE, 0x2E8E8F, 7, 8, -64, 0, 0f, 1f, 13, 6, false),
        new OreType(Blocks.EMERALD_ORE, 0x1FBE57, 100, 4, -16, 320, 0f, 1f, 14, 6, true),
        new OreType(Blocks.DEEPSLATE_EMERALD_ORE, 0x16853D, 100, 4, -64, 32, 0f, 1f, 15, 6, true)
    };
}
