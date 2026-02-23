# World Importer

<div align="right">   <strong>English</strong> | <a href="README.md">简体中文</a> </div>

A Minecraft mod designed to upload and paste local world MCA region files to a server.

The client parses MCA files, extracts NBT data chunk by chunk, and uploads it to the server. The server replaces chunks (blocks, biomes, block entities) in real-time without requiring a restart. After the import is complete, it automatically recalculates lighting and synchronizes the chunks with all players.

Supports Fabric / Forge / NeoForge, compatible with Minecraft 1.19 ~ 1.20.6.

**! WARNING:** This mod will directly overwrite all chunks from the MCA file onto the server! Do not paste in areas with existing builds! It is highly recommended to make a backup before pasting!

## Versions

| Directory                 | MC Version      | Loader            | Architectury | Java |
| ------------------------- | --------------- | ----------------- | ------------ | ---- |
| `versions/1.19-1.19.2/`   | 1.19 ~ 1.19.2   | Fabric + Forge    | 6.x          | 17   |
| `versions/1.19.3/`        | 1.19.3          | Fabric + Forge    | 7.x          | 17   |
| `versions/1.19.4/`        | 1.19.4          | Fabric + Forge    | 8.x          | 17   |
| `versions/1.20-1.20.1/`   | 1.20 ~ 1.20.1   | Fabric + Forge    | 9.x          | 17   |
| `versions/1.20.2-1.20.4/` | 1.20.2 ~ 1.20.4 | Fabric + Forge    | 11.x         | 17   |
| `versions/1.20.5-1.20.6/` | 1.20.5 ~ 1.20.6 | Fabric + NeoForge | 12.x         | 21   |

## Usage

1. Install the mod on both the client and the server.
2. Join the server, stand at the target location, and execute `/wi setpos` to set the paste origin.
3. Execute `/wi select <path>` to select the local world's `region` folder (or press `K` to open the configuration GUI and fill it in).
4. Execute `/wi start` to begin the upload (or click "Start Upload" in the configuration GUI).
5. Wait for the import to complete (the HUD will display a progress bar and TPS).
6. After the import finishes, lighting will automatically update and chunks will sync.

## Commands

| Command             | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| `/wi setpos`        | Sets the current standing position as the paste origin (the center point of the source world will correspond to this paste origin). |
| `/wi select <path>` | Selects the path to the local region folder.                 |
| `/wi start`         | Starts uploading the selected region folder.                 |
| `/wi status`        | Checks the paste origin and import progress.                 |
| `/wi cancel`        | Cancels the ongoing import.                                  |

All commands require OP permissions (Level 2).

## Configuration

Press the **K** key to open the configuration GUI (also accessible via Mod Menu on Fabric). You can adjust:

| Option                          | Default | Description                                                  |
| ------------------------------- | ------- | ------------------------------------------------------------ |
| Upload Interval (ticks)         | 5       | The interval between each chunk upload; higher values reduce server load. |
| Packet Size (KB)                | 30      | The maximum size of a single network packet.                 |
| Light Update Rate (chunks/tick) | 5       | How many chunks' lighting updates are processed per tick.    |
| Auto-throttle                   | On      | Automatically adjusts the upload interval based on the server's TPS. |

When Auto-throttle is enabled:
- TPS ≥ 18: Uses the configured upload interval.
- TPS ≤ 15: Automatically throttles down to the maximum interval (40 ticks).
- Linear interpolation is used for values in between.

The configuration file is saved at `.minecraft/config/world_importer.json`.

## HUD

During the import process, the top of the screen displays:

- **Importing** — Green progress bar + Completed / Total.
- **Lighting Update** — Cyan progress bar.
- **Syncing Chunks** — Purple progress bar.
- The right side of the progress bar displays the server TPS (Green/Yellow/Red).

## Building

```bash
# Build all versions and all loaders
./gradlew build

# Build only all Fabric versions
./gradlew build -Ploader=fabric

# Build only all Forge/NeoForge versions
./gradlew build -Ploader=forge

# Build only a specific MC version
./gradlew build -Pver=1201

# Combination: Build only 1.20.4 for Fabric
./gradlew build -Pver=4 -Ploader=fabric

# Run client (default is 1.20.1 Fabric)
./gradlew runClient

# Run client: Specify version and loader
./gradlew runClient -Pver=4 -Ploader=forge

# Run server
./gradlew runServer -Pver=1 -Ploader=fabric

# Collect all jars into release/
./gradlew collectJars

# Clean everything
./gradlew cleanAll
```

| **Alias** | **Corresponding Version** |
| --------- | ------------------------- |
| `1192`    | 1.19 ~ 1.19.2             |
| `1193`    | 1.19.3                    |
| `1194`    | 1.19.4                    |
| `1201`    | 1.20 ~ 1.20.1             |
| `1204`    | 1.20.2 ~ 1.20.4           |
| `1206`    | 1.20.5 ~ 1.20.6           |

Supported `-Ploader` values: `fabric`, `forge`

## Technical Details

- The client parses the MCA binary format (4096-byte header → sector offsets → extracts compressed NBT chunk by chunk).
- The server decompresses the NBT → parses the `PalettedContainer` (block states + biomes) in the `sections` → replaces the entire section → recalculates heightmaps → loads block entities.
- Light updates use the chunk NBT overwrite method → clears original lighting data and recalculates it.
- Traffic control: The server only notifies the client to send the next chunk after finishing the previous one, combined with TPS auto-throttling.
- Finally, it deletes the client's original chunk render cache and sends the updated chunks to the client.

## Dependencies

- [Architectury API](https://github.com/architectury/architectury-api)
- [Mod Menu](https://github.com/TerraformersMC/ModMenu) (Optional for Fabric)

## License

MIT License © rolling_cat