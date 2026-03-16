package com.oreemp;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public record OreType(Block block, int color) {
    public static final OreType[] OVERWORLD = new OreType[] {
        new OreType(Blocks.COAL_ORE, 0x111111),
        new OreType(Blocks.DEEPSLATE_COAL_ORE, 0x222222),
        new OreType(Blocks.IRON_ORE, 0xD8AF93),
        new OreType(Blocks.DEEPSLATE_IRON_ORE, 0x8B6E5A),
        new OreType(Blocks.COPPER_ORE, 0xCB6D51),
        new OreType(Blocks.DEEPSLATE_COPPER_ORE, 0x9A4F3B),
        new OreType(Blocks.GOLD_ORE, 0xE6D966),
        new OreType(Blocks.DEEPSLATE_GOLD_ORE, 0xB79A3E),
        new OreType(Blocks.REDSTONE_ORE, 0xB80F0A),
        new OreType(Blocks.DEEPSLATE_REDSTONE_ORE, 0x8D0B07),
        new OreType(Blocks.LAPIS_ORE, 0x3552CC),
        new OreType(Blocks.DEEPSLATE_LAPIS_ORE, 0x24398E),
        new OreType(Blocks.DIAMOND_ORE, 0x45D7D8),
        new OreType(Blocks.DEEPSLATE_DIAMOND_ORE, 0x2E8E8F),
        new OreType(Blocks.EMERALD_ORE, 0x1FBE57),
        new OreType(Blocks.DEEPSLATE_EMERALD_ORE, 0x16853D)
    };
}
