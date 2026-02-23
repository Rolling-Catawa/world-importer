package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.WorldImporter;
import cn.jstxjf_.world_importer.data.ImportSession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class ServerChunkPlacer {
    public static void placeChunk(ServerPlayer player, ImportSession session, int srcChunkX, int srcChunkZ, int compressionType, byte[] compressedData) {
        ServerLevel level = player.serverLevel();
        int offsetChunkX = session.getOriginX() >> 4;
        int offsetChunkZ = session.getOriginZ() >> 4;
        int targetChunkX = srcChunkX + offsetChunkX;
        int targetChunkZ = srcChunkZ + offsetChunkZ;
        try {
            CompoundTag chunkNbt = decompress(compressedData, compressionType);
            applyFullChunk(level, chunkNbt, targetChunkX, targetChunkZ);
            session.addPlacedChunk(new ChunkPos(targetChunkX, targetChunkZ));
        } catch (Exception e) {
            WorldImporter.LOGGER.error("Failed chunk ({}, {}): {}", targetChunkX, targetChunkZ, e.getMessage());
        }
    }

    private static CompoundTag decompress(byte[] data, int compressionType) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis;
        if (compressionType == 1) {
            dis = new DataInputStream(new GZIPInputStream(bais));
        } else if (compressionType == 2) {
            dis = new DataInputStream(new InflaterInputStream(bais));
        } else {
            dis = new DataInputStream(bais);
        }
        return NbtIo.read(dis);
    }

    @SuppressWarnings("unchecked")
    private static void applyFullChunk(ServerLevel level, CompoundTag chunkNbt, int targetChunkX, int targetChunkZ) {
        ChunkPos targetPos = new ChunkPos(targetChunkX, targetChunkZ);
        LevelChunk targetChunk = level.getChunk(targetChunkX, targetChunkZ);
        LevelChunkSection[] sections = targetChunk.getSections();
        int minSectionY = level.getMinSection();
        int baseX = targetPos.getMinBlockX();
        int baseZ = targetPos.getMinBlockZ();

        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> defaultBiome = biomeRegistry.getHolderOrThrow(Biomes.PLAINS);

        for (BlockPos pos : new HashSet<>(targetChunk.getBlockEntities().keySet())) {
            targetChunk.removeBlockEntity(pos);
        }

        for (int idx = 0; idx < sections.length; idx++) {
            sections[idx] = new LevelChunkSection(
                    new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES),
                    new PalettedContainer<>(biomeRegistry.asHolderIdMap(), defaultBiome, PalettedContainer.Strategy.SECTION_BIOMES)
            );
        }

        ThreadedLevelLightEngine lightEngine = (ThreadedLevelLightEngine) level.getLightEngine();

        ListTag sectionsTag = chunkNbt.getList("sections", 10);
        if (sectionsTag != null) {
            for (int i = 0; i < sectionsTag.size(); i++) {
                CompoundTag sectionTag = sectionsTag.getCompound(i);
                int sectionY = sectionTag.getByte("Y");
                int sectionIndex = sectionY - minSectionY;
                if (sectionIndex < 0 || sectionIndex >= sections.length) continue;

                PalettedContainer<BlockState> blockStates = parseBlockStates(sectionTag);
                PalettedContainer<Holder<Biome>> biomes = parseBiomes(sectionTag, biomeRegistry, defaultBiome);
                sections[sectionIndex] = new LevelChunkSection(blockStates, biomes);

                var secPos = net.minecraft.core.SectionPos.of(targetChunkX, sectionY, targetChunkZ);
                if (sectionTag.contains("BlockLight", 7)) {
                    byte[] bl = sectionTag.getByteArray("BlockLight");
                    if (bl.length == 2048) {
                        lightEngine.queueSectionData(LightLayer.BLOCK, secPos, new DataLayer(bl.clone()));
                    }
                }
                if (sectionTag.contains("SkyLight", 7)) {
                    byte[] sl = sectionTag.getByteArray("SkyLight");
                    if (sl.length == 2048) {
                        lightEngine.queueSectionData(LightLayer.SKY, secPos, new DataLayer(sl.clone()));
                    }
                }
            }
        }

        lightEngine.setLightEnabled(targetPos, true);
        lightEngine.retainData(targetPos, true);

        Heightmap.primeHeightmaps(targetChunk,
                EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.WORLD_SURFACE,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR));

        if (chunkNbt.contains("block_entities", 9)) {
            ListTag beList = chunkNbt.getList("block_entities", 10);
            for (int i = 0; i < beList.size(); i++) {
                try {
                    CompoundTag beTag = beList.getCompound(i);
                    int localX = beTag.getInt("x") & 15;
                    int localZ = beTag.getInt("z") & 15;
                    int y = beTag.getInt("y");
                    beTag.putInt("x", baseX + localX);
                    beTag.putInt("z", baseZ + localZ);
                    BlockPos bePos = new BlockPos(baseX + localX, y, baseZ + localZ);
                    BlockState stateAt = targetChunk.getBlockState(bePos);
                    if (stateAt.hasBlockEntity()) {
                        BlockEntity be = BlockEntity.loadStatic(bePos, stateAt, beTag);
                        if (be != null) targetChunk.setBlockEntity(be);
                    }
                } catch (Exception ignored) {}
            }
        }

        targetChunk.setUnsaved(true);
    }

    private static PalettedContainer<BlockState> parseBlockStates(CompoundTag sectionTag) {
        if (!sectionTag.contains("block_states")) {
            return new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        }
        try {
            return PalettedContainer.codecRW(
                    Block.BLOCK_STATE_REGISTRY, BlockState.CODEC,
                    PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
            ).parse(net.minecraft.nbt.NbtOps.INSTANCE, sectionTag.getCompound("block_states"))
                    .getOrThrow(false, s -> {});
        } catch (Exception e) {
            return new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        }
    }

    @SuppressWarnings("unchecked")
    private static PalettedContainer<Holder<Biome>> parseBiomes(CompoundTag sectionTag, Registry<Biome> registry, Holder<Biome> defaultBiome) {
        if (!sectionTag.contains("biomes")) {
            return new PalettedContainer<>(registry.asHolderIdMap(), defaultBiome, PalettedContainer.Strategy.SECTION_BIOMES);
        }
        try {
            var codec = PalettedContainer.codecRO(
                    registry.asHolderIdMap(), registry.holderByNameCodec(),
                    PalettedContainer.Strategy.SECTION_BIOMES, defaultBiome
            );
            PalettedContainerRO<Holder<Biome>> parsed = codec.parse(
                    net.minecraft.nbt.NbtOps.INSTANCE, sectionTag.getCompound("biomes")
            ).getOrThrow(false, s -> {});
            if (parsed instanceof PalettedContainer<Holder<Biome>> pc) return pc;
        } catch (Exception ignored) {}
        return new PalettedContainer<>(registry.asHolderIdMap(), defaultBiome, PalettedContainer.Strategy.SECTION_BIOMES);
    }
}
