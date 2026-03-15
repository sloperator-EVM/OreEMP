package com.oreemp;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OreSimManager {
    private final MinecraftClient client;
    private final Map<Long, Map<OreType, Set<BlockPos>>> chunkOres = new ConcurrentHashMap<>();
    private final Long2ObjectMap<String> scanned = new Long2ObjectOpenHashMap<>();
    private boolean running;
    private Long seed;
    private long lastDebugMs;

    public OreSimManager(MinecraftClient client) {
        this.client = client;
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
        scanned.clear();
        scanLoadedChunks();
        send("OreEMP started");
    }

    public void stop() {
        running = false;
        chunkOres.clear();
        scanned.clear();
        send("OreEMP stopped");
    }

    public void onChunkLoad(int chunkX, int chunkZ) {
        if (!running || seed == null) {
            return;
        }
        scanChunk(chunkX, chunkZ);
    }

    public Map<Long, Map<OreType, Set<BlockPos>>> getChunkOres() {
        return chunkOres;
    }

    private void scanLoadedChunks() {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        int vd = client.options.getViewDistance().getValue();
        ChunkPos center = player.getChunkPos();
        int total = 0;
        int scannedChunks = 0;
        for (int x = center.x - vd; x <= center.x + vd; x++) {
            for (int z = center.z - vd; z <= center.z + vd; z++) {
                total++;
                if (world.getChunkManager().isChunkLoaded(x, z)) {
                    scanChunk(x, z);
                    scannedChunks++;
                }
            }
        }
        send("Scanned " + scannedChunks + "/" + total + " chunks around you");
    }

    private void scanChunk(int chunkX, int chunkZ) {
        long key = ChunkPos.toLong(chunkX, chunkZ);
        if (chunkOres.containsKey(key)) {
            return;
        }
        ClientWorld world = client.world;
        if (world == null || seed == null) {
            return;
        }
        Map<OreType, Set<BlockPos>> simulated = new ConcurrentHashMap<>();
        Random random = new Random(mixSeed(seed, chunkX, chunkZ));
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (OreType ore : OreType.OVERWORLD) {
            Set<BlockPos> placed = new HashSet<>();
            random.setSeed(mixSeed(seed + ore.index(), chunkX, chunkZ) ^ ((long) ore.step() << 32));
            for (int i = 0; i < ore.count(); i++) {
                if (ore.rarity() != 1f && random.nextFloat() >= 1f / ore.rarity()) {
                    continue;
                }
                int x = baseX + random.nextInt(16);
                int z = baseZ + random.nextInt(16);
                int y = ore.minY() + random.nextInt(Math.max(1, ore.maxY() - ore.minY()));
                BlockPos origin = new BlockPos(x, y, z);
                if (ore.scattered()) {
                    placed.addAll(generateHidden(world, random, origin, ore.size()));
                } else {
                    placed.addAll(generateNormal(world, random, origin, ore.size(), ore.discardOnAir()));
                }
            }
            if (!placed.isEmpty()) {
                simulated.put(ore, placed);
            }
        }
        chunkOres.put(key, simulated);
        scanned.put(key, "ok");
        long now = System.currentTimeMillis();
        if (now - lastDebugMs > 1000) {
            send("Scanning chunk " + chunkX + "," + chunkZ + " status: " + scanned.size() + " scanned");
            lastDebugMs = now;
        }
    }

    private long mixSeed(long worldSeed, int chunkX, int chunkZ) {
        long x = (long) chunkX * 341873128712L;
        long z = (long) chunkZ * 132897987541L;
        return worldSeed ^ x ^ z;
    }

    private List<BlockPos> generateNormal(ClientWorld world, Random random, BlockPos blockPos, int veinSize, float discardOnAir) {
        float f = random.nextFloat() * 3.1415927F;
        float g = (float) veinSize / 8.0F;
        int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
        double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
        double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
        double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
        double l = (blockPos.getY() + random.nextInt(3) - 2);
        double m = (blockPos.getY() + random.nextInt(3) - 2);
        int n = blockPos.getX() - MathHelper.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - MathHelper.ceil(g) - i;
        int q = 2 * (MathHelper.ceil(g) + i);
        int r = 2 * (2 + i);
        return generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
    }

    private List<BlockPos> generateVeinPart(ClientWorld world, Random random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir) {
        BitSet bitSet = new BitSet(size * i * size);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        double[] ds = new double[veinSize * 4];
        ArrayList<BlockPos> poses = new ArrayList<>();
        for (int n = 0; n < veinSize; ++n) {
            float f = (float) n / (float) veinSize;
            double p = MathHelper.lerp(f, startX, endX);
            double q = MathHelper.lerp(f, startY, endY);
            double r = MathHelper.lerp(f, startZ, endZ);
            double s = random.nextDouble() * (double) veinSize / 16.0D;
            double m = ((double) (MathHelper.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
            ds[n * 4] = p;
            ds[n * 4 + 1] = q;
            ds[n * 4 + 2] = r;
            ds[n * 4 + 3] = m;
        }
        for (int n = 0; n < veinSize; ++n) {
            double u = ds[n * 4 + 3];
            if (u < 0.0D) {
                continue;
            }
            double v = ds[n * 4];
            double w = ds[n * 4 + 1];
            double aa = ds[n * 4 + 2];
            int ab = Math.max(MathHelper.floor(v - u), x);
            int ac = Math.max(MathHelper.floor(w - u), y);
            int ad = Math.max(MathHelper.floor(aa - u), z);
            int ae = Math.max(MathHelper.floor(v + u), ab);
            int af = Math.max(MathHelper.floor(w + u), ac);
            int ag = Math.max(MathHelper.floor(aa + u), ad);
            for (int ah = ab; ah <= ae; ++ah) {
                double ai = ((double) ah + 0.5D - v) / u;
                if (ai * ai >= 1.0D) {
                    continue;
                }
                for (int aj = ac; aj <= af; ++aj) {
                    double ak = ((double) aj + 0.5D - w) / u;
                    if (ai * ai + ak * ak >= 1.0D) {
                        continue;
                    }
                    for (int al = ad; al <= ag; ++al) {
                        double am = ((double) al + 0.5D - aa) / u;
                        if (ai * ai + ak * ak + am * am >= 1.0D) {
                            continue;
                        }
                        int an = ah - x + (aj - y) * size + (al - z) * size * i;
                        if (bitSet.get(an)) {
                            continue;
                        }
                        bitSet.set(an);
                        mutable.set(ah, aj, al);
                        if (aj < world.getBottomY() || aj >= world.getTopY()) {
                            continue;
                        }
                        if (!world.getBlockState(mutable).isOpaque()) {
                            continue;
                        }
                        if (shouldPlace(world, mutable, discardOnAir, random)) {
                            poses.add(mutable.toImmutable());
                        }
                    }
                }
            }
        }
        return poses;
    }

    private boolean shouldPlace(ClientWorld world, BlockPos orePos, float discardOnAir, Random random) {
        if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            BlockState state = world.getBlockState(orePos.offset(direction));
            if (!state.isOpaque() && discardOnAir != 1F) {
                return false;
            }
        }
        return true;
    }

    private List<BlockPos> generateHidden(ClientWorld world, Random random, BlockPos blockPos, int size) {
        ArrayList<BlockPos> poses = new ArrayList<>();
        int i = random.nextInt(size + 1);
        for (int j = 0; j < i; ++j) {
            int spread = Math.min(j, 7);
            int x = randomCoord(random, spread) + blockPos.getX();
            int y = randomCoord(random, spread) + blockPos.getY();
            int z = randomCoord(random, spread) + blockPos.getZ();
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.getBlockState(pos).isOpaque()) {
                continue;
            }
            if (shouldPlace(world, pos, 1F, random)) {
                poses.add(pos);
            }
        }
        return poses;
    }

    private int randomCoord(Random random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
    }

    private void send(String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[OreEMP] " + message), false);
        }
    }
}
