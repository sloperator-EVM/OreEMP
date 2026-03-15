package com.oreemp;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Set;

public class OreEmpClient implements ClientModInitializer {
    private OreSimManager manager;

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        manager = new OreSimManager(client);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = ClientCommandManager.literal("oreemp")
                .then(ClientCommandManager.literal("start").executes(ctx -> {
                    manager.start();
                    return 1;
                }))
                .then(ClientCommandManager.literal("stop").executes(ctx -> {
                    manager.stop();
                    return 1;
                }))
                .then(ClientCommandManager.literal("seed")
                    .then(ClientCommandManager.argument("value", LongArgumentType.longArg()).executes(ctx -> {
                        long value = LongArgumentType.getLong(ctx, "value");
                        manager.setSeed(value);
                        if (client.player != null) {
                            client.player.sendMessage(Text.literal("[OreEMP] Seed set to " + value), false);
                        }
                        return 1;
                    })));
            dispatcher.register(root);
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> manager.onChunkLoad(chunk.getPos().x, chunk.getPos().z));
        WorldRenderEvents.LAST.register(this::renderOres);
    }

    private void renderOres(WorldRenderContext context) {
        if (!manager.isRunning()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || context.camera() == null || context.matrixStack() == null || context.consumers() == null) {
            return;
        }
        ChunkPos playerChunk = client.player.getChunkPos();
        int range = client.options.getViewDistance().getValue();
        Vec3d camPos = context.camera().getPos();
        VertexConsumerProvider consumers = context.consumers();

        for (Map.Entry<Long, Map<OreType, Set<BlockPos>>> entry : manager.getChunkOres().entrySet()) {
            ChunkPos pos = new ChunkPos(entry.getKey());
            if (Math.abs(pos.x - playerChunk.x) > range || Math.abs(pos.z - playerChunk.z) > range) {
                continue;
            }
            for (Map.Entry<OreType, Set<BlockPos>> oreEntry : entry.getValue().entrySet()) {
                OreType ore = oreEntry.getKey();
                float r = ((ore.color() >> 16) & 255) / 255f;
                float g = ((ore.color() >> 8) & 255) / 255f;
                float b = (ore.color() & 255) / 255f;
                for (BlockPos orePos : oreEntry.getValue()) {
                    if (client.world.getBlockState(orePos).isAir()) {
                        continue;
                    }
                    Box box = new Box(orePos).offset(-camPos.x, -camPos.y, -camPos.z);
                    DebugRenderer.drawBox(context.matrixStack(), consumers, box, r, g, b, 0.85f);
                }
            }
        }
    }
}
