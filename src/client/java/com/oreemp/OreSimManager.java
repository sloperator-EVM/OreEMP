package com.oreemp;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OreSimManager {
    private static final int CHUNKS_PER_TICK = 1;
    private final MinecraftClient client;
    private final Map<Long, Map<OreType, Set<BlockPos>>> chunkOres = new ConcurrentHashMap<>();
    private final Queue<ChunkPos> scanQueue = new ArrayDeque<>();
    private final Set<Long> queuedChunks = new HashSet<>();
    private final Map<Block, OreType> oreByBlock = new HashMap<>();
    private boolean running;
    private Long seed;
    private long lastDebugMs;
    private int scannedCount;

    public OreSimManager(MinecraftClient client) {
        this.client = client;
        for (OreType ore : OreType.OVERWORLD) {
            oreByBlock.put(ore.block(), ore);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void start() {
        if (seed == null) {
            send("Set a seed first with /oreemp seed <value>");
            return;
        }
        running = true;
        chunkOres.clear();
        scanQueue.clear();
        queuedChunks.clear();
        scannedCount = 0;
        queueLoadedChunks();
        send("OreEMP started");
    }

    public void stop() {
        running = false;
        chunkOres.clear();
        scanQueue.clear();
        queuedChunks.clear();
        scannedCount = 0;
        send("OreEMP stopped");
    }

    public void tick() {
        if (!running) {
            return;
        }
        ClientWorld world = client.world;
        if (world == null) {
            return;
        }
        int processed = 0;
        while (processed < CHUNKS_PER_TICK && !scanQueue.isEmpty()) {
            ChunkPos chunkPos = scanQueue.poll();
            if (chunkPos == null) {
                break;
            }
            WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                scanChunk(chunk);
                processed++;
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastDebugMs > 1200 && !scanQueue.isEmpty()) {
            send("Scanning chunk queue: done=" + scannedCount + " left=" + scanQueue.size());
            lastDebugMs = now;
        }
    }

    public void onChunkLoad(int chunkX, int chunkZ) {
        if (!running) {
            return;
        }
        enqueue(chunkX, chunkZ);
    }

    public Map<Long, Map<OreType, Set<BlockPos>>> getChunkOres() {
        return chunkOres;
    }

    private void queueLoadedChunks() {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        int range = client.options.getViewDistance().getValue();
        ChunkPos center = player.getChunkPos();
        for (int x = center.x - range; x <= center.x + range; x++) {
            for (int z = center.z - range; z <= center.z + range; z++) {
                if (world.getChunkManager().isChunkLoaded(x, z)) {
                    enqueue(x, z);
                }
            }
        }
        send("Queued " + scanQueue.size() + " loaded chunks");
    }

    private void enqueue(int chunkX, int chunkZ) {
        long key = ChunkPos.toLong(chunkX, chunkZ);
        if (queuedChunks.add(key)) {
            scanQueue.add(new ChunkPos(chunkX, chunkZ));
        }
    }

    private void scanChunk(WorldChunk chunk) {
        ClientWorld world = client.world;
        if (world == null) {
            return;
        }
        int minY = world.getBottomY();
        int maxY = minY + world.getHeight();
        int minX = chunk.getPos().getStartX();
        int minZ = chunk.getPos().getStartZ();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Map<OreType, Set<BlockPos>> ores = new HashMap<>();

        for (int y = minY; y < maxY; y++) {
            for (int lx = 0; lx < 16; lx++) {
                int x = minX + lx;
                for (int lz = 0; lz < 16; lz++) {
                    int z = minZ + lz;
                    mutable.set(x, y, z);
                    BlockState state = chunk.getBlockState(mutable);
                    OreType ore = oreByBlock.get(state.getBlock());
                    if (ore == null) {
                        continue;
                    }
                    ores.computeIfAbsent(ore, ignored -> new HashSet<>()).add(mutable.toImmutable());
                }
            }
        }

        chunkOres.put(chunk.getPos().toLong(), ores);
        scannedCount++;
    }

    private void send(String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[OreEMP] " + message), false);
        }
    }
}
