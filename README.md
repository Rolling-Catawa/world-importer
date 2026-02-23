# World Importer

该模组旨在将本地世界的 MCA 区域文件上传并粘贴到服务器的 Minecraft Mod。

客户端解析 MCA 文件，逐区块提取 NBT 数据上传到服务端，服务端实时替换区块（方块、生物群系、方块实体），无需重启。导入完成后自动重算光照并同步给所有玩家。

支持 Fabric / Forge / NeoForge，兼容 Minecraft 1.19 ~ 1.20.6。

！注意：该模组将会直接将MCA中的所有区块覆盖到服务器中！请不要在有建筑的范围内粘贴！粘贴前建议备份！

## 版本

| 目录 | MC 版本 | 加载器 | Architectury | Java |
|------|---------|--------|-------------|------|
| `versions/1.19-1.19.2/` | 1.19 ~ 1.19.2 | Fabric + Forge | 6.x | 17 |
| `versions/1.19.3/` | 1.19.3 | Fabric + Forge | 7.x | 17 |
| `versions/1.19.4/` | 1.19.4 | Fabric + Forge | 8.x | 17 |
| `versions/1.20-1.20.1/` | 1.20 ~ 1.20.1 | Fabric + Forge | 9.x | 17 |
| `versions/1.20.2-1.20.4/` | 1.20.2 ~ 1.20.4 | Fabric + Forge | 11.x | 17 |
| `versions/1.20.5-1.20.6/` | 1.20.5 ~ 1.20.6 | Fabric + NeoForge | 12.x | 21 |

## 使用流程

1. 客户端和服务端都安装 Mod
2. 进入服务器，站到目标位置执行 `/wi setpos` 设置粘贴原点
3. 执行 `/wi select <路径>` 选择本地世界的 `region` 文件夹（或按 `K` 打开配置界面填写）
4. 执行 `/wi start` 开始上传（或在配置界面点击"开始上传"）
5. 等待导入完成（HUD 显示进度条和 TPS）
6. 导入结束后自动更新光照并同步区块

## 命令

| 命令 | 说明 |
|------|------|
| `/wi setpos` | 将当前站立位置设为粘贴原点（源世界中心点对应粘贴原点） |
| `/wi select <路径>` | 选择本地 region 文件夹路径 |
| `/wi start` | 开始上传已选择的 region 文件夹 |
| `/wi status` | 查看粘贴原点和导入进度 |
| `/wi cancel` | 取消正在进行的导入 |

所有命令需要 OP 权限（等级 2）。

## 配置

按 **K** 键打开配置界面（Fabric 端也可通过 Mod Menu 打开），可调整：

| 选项 | 默认值 | 说明 |
|------|--------|------|
| 上传间隔 (tick) | 5 | 每个区块上传之间的间隔，值越大服务器压力越小 |
| 分包大小 (KB) | 30 | 单个网络包的最大体积 |
| 光照更新速率 (chunk/tick) | 5 | 每 tick 处理多少个区块的光照更新 |
| 自动调速 | 开 | 根据服务器 TPS 自动调整上传间隔 |

自动调速开启时：
- TPS ≥ 18：使用配置的上传间隔
- TPS ≤ 15：自动减速到最大间隔（40 tick）
- 中间线性插值

配置文件保存在 `.minecraft/config/world_importer.json`。

## HUD

导入过程中屏幕顶部显示：

- **导入中** — 绿色进度条 + 已完成/总数
- **光照更新** — 青色进度条
- **同步区块** — 紫色进度条
- 进度条右侧显示服务器 TPS（绿/黄/红）

## 构建

```bash
# 构建全部版本、全部加载器
./gradlew build

# 只构建 Fabric 全版本
./gradlew build -Ploader=fabric

# 只构建 Forge/NeoForge 全版本
./gradlew build -Ploader=forge

# 只构建某个 MC 版本
./gradlew build -Pver=1201

# 组合：只构建 1.20.4 的 Fabric
./gradlew build -Pver=4 -Ploader=fabric

# 启动客户端（默认 1.20.1 fabric）
./gradlew runClient

# 启动客户端：指定版本和加载器
./gradlew runClient -Pver=4 -Ploader=forge

# 启动服务端
./gradlew runServer -Pver=1 -Ploader=fabric

# 收集所有 jar 到 build/libs/
./gradlew collectJars

# 清理全部
./gradlew cleanAll
```

> **提示**：`-Pver` 支持简写，无需输入完整版本号。`-Pver=1` 等价于 `-Pver=1201`，即 MC 1.20.1。

| 别名 | 简写 | 对应版本 |
|------|------|---------|
| `1192` | `2` | 1.19 ~ 1.19.2 |
| `1193` | `3` | 1.19.3 |
| `1194` | `4` | 1.19.4 |
| `1201` | `1` | 1.20 ~ 1.20.1 |
| `1204` | `4` | 1.20.2 ~ 1.20.4 |
| `1206` | `6` | 1.20.5 ~ 1.20.6 |

`-Ploader` 可选值：`fabric`、`forge`

## 技术细节

- 客户端解析 MCA 二进制格式（4096 字节头部 → sector 偏移 → 逐 chunk 提取压缩 NBT）
- 服务端解压 NBT → 解析 `sections` 中的 `PalettedContainer`（方块状态 + 生物群系）→ 替换整个 section → 重算高度图 → 加载方块实体
- 光照更新使用区块NBT覆盖法 → 清除原光照数据并重新计算
- 流量控制：服务端处理完一个 chunk 后才通知客户端发下一个，配合 TPS 自动调速
- 最后删除客户端原区块渲染缓存并下发Chunks到客户端

## 依赖

- [Architectury API](https://github.com/architectury/architectury-api)
- [Mod Menu](https://github.com/TerraformersMC/ModMenu)（Fabric 端可选）

## 许可

MIT License © rolling_cat
