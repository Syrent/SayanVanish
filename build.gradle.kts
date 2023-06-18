import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.URL
import java.util.*
import java.time.Instant
import java.util.concurrent.Executors

plugins {
    kotlin("jvm") version "1.8.22"
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("org.screamingsandals.nms-mapper") version "1.4.5"
    id("xyz.jpenilla.run-paper") version "2.1.0"
}

group = "ir.syrent.velocityvanish"
version = "${project.version}"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.maven.apache.org/maven2/")

    // Velocity-API / PaperLib / Folia
    maven("https://repo.papermc.io/repository/maven-public/")

    // AdventureAPI/MiniMessage
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }

    // ProtocolLib
    maven("https://repo.dmulloy2.net/repository/public/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // EssentialsX
    maven("https://repo.essentialsx.net/releases/")

    // Update checker
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")

    // DiscordSRV
    maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("xyz.jpenilla:squaremap-api:1.1.14")
    compileOnly("net.essentialsx:EssentialsX:2.20.0")
    compileOnly("org.spongepowered:configurate-yaml:4.0.0")
    compileOnly("com.discordsrv:discordsrv:1.26.2")

    // SayanChat 2.8.1
    // ProCosmetics
    // ServerListPlus 3.5.0 SNAPSHOT
    // NexEngine and SunLight
    compileOnly(fileTree("libs"))

    implementation("io.papermc:paperlib:1.0.8")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // gson is not exist in legacy minecraft version
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.cryptomorin:XSeries:9.4.0") { isTransitive = false }

    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")

    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("org.spigotmc:spigot:1.20.1-R0.1-SNAPSHOT:remapped-mojang")
}

val extraDependencies = emptyMap<String, String>()

tasks {
    runServer {
        minecraftVersion("1.20.1")
        serverJar(file("run/paper-1.20.1-20.jar"))
    }

    kotlin {
        jvmToolchain(17)
    }

    jar {
        enabled = false
    }

    val relocate = task<ConfigureShadowRelocation>("relocateShadowJar") {
        println(actions)
        target = shadowJar.get()
        prefix = "ir.syrent.velocityvanish"
    }


    shadowJar {
        dependsOn(relocate)
        archiveClassifier.set("")
        exclude("META-INF/**")
        minimize()

        destinationDirectory.set(file("run/plugins"))
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    val extraDeps = register("downloadExtraDependencies") {
        val libsDir = File("libs")
        libsDir.mkdirs()
        val ex = Executors.newCachedThreadPool()
        for (entry in extraDependencies) {
            val file = File(libsDir, entry.key)
            if (file.exists())
                continue
            ex.submit {
                println("Downloading ${entry.key} from ${entry.value}")
                URL(entry.value).openStream().use { s -> file.outputStream().use { it.write(s.readBytes()) } }
                println("Successfully downloaded ${entry.key} to ${file.path}")
            }
        }
        ex.shutdown()
        ex.awaitTermination(10, TimeUnit.SECONDS)
    }

    build {
        dependsOn(extraDeps)
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { outputs -> outputs })
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)

/* First add a new source set. Don't use your main source set for generated stuff. */
sourceSets.main.get().java.srcDirs("src/generated/java", "src/main/java")

/* All other things will be set inside the nmsGen method, */
nmsGen {
    basePackage = "ir.syrent.nms.accessors" // All generated classes will be in this package.
    sourceSet = "src/generated/java" // All generated classes will be part of this source set.
    minMinecraftVersion = "1.8"

    /* This means that the folder will be cleared before generation.
     *
     * If this value is false, old and no longer used classes won't be removed.
     */
    isCleanOnRebuild = true

    val ClientboundPlayerInfoPacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket")
    val ClientboundPlayerInfoPacketAction = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket\$Action")
    val ClientboundPlayerInfoPacketPlayerUpdate = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket\$PlayerUpdate")
    val ClientboundPlayerInfoUpdatePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket") //1.19.3 and above
    val ClientboundPlayerInfoRemovePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket") //1.19.3 and above
    val ClientboundPlayerInfoUpdatePacketAction = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Action") //1.19.3 and above
    val ClientboundAddEntityPacket = reqClass("net.minecraft.network.protocol.game.ClientboundAddEntityPacket")
    val ClientboundAddPlayerPacket = reqClass("net.minecraft.network.protocol.game.ClientboundAddPlayerPacket")
    val ClientboundRotateHeadPacket = reqClass("net.minecraft.network.protocol.game.ClientboundRotateHeadPacket")
    val ClientboundRemoveEntitiesPacket = reqClass("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket")
    val ClientboundMoveEntityPacketRot = reqClass("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket\$Rot")
    val ClientboundMoveEntityPacketPos = reqClass("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket\$Pos")
    val ClientboundMoveEntityPacketPosRot = reqClass("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket\$PosRot")
    val ClientboundAnimatePacket = reqClass("net.minecraft.network.protocol.game.ClientboundAnimatePacket")
    val ClientboundBlockBreakAckPacket = reqClass("net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket")
    val ClientboundSetEntityDataPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket")
    val ClientboundSetEquipmentPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket")
    val ClientboundTeleportEntityPacket = reqClass("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket")
    val ClientboundSetEntityMotionPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket")
    val ClientboundTakeItemEntityPacket = reqClass("net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket")
    val ClientboundBlockEventPacket = reqClass("net.minecraft.network.protocol.game.ClientboundBlockEventPacket")
    val ClientboundSetPassengersPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetPassengersPacket")
    val ClientboundBlockDestructionPacket = reqClass("net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket")
    val ClientboundUpdateAdvancementsPacket = reqClass("net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket")
    val ClientboundLevelChunkPacketData = reqClass("net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData") //1.18 and above
    val ClientboundLevelChunkWithLightPacket = reqClass("net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket") //1.18 and above
    val ClientboundLevelChunkPacket = reqClass("net.minecraft.network.protocol.game.ClientboundLevelChunkPacket") //1.17 and below
    val ClientboundLightUpdatePacket = reqClass("net.minecraft.network.protocol.game.ClientboundLightUpdatePacket")
    val ClientboundOpenScreenPacket = reqClass("net.minecraft.network.protocol.game.ClientboundOpenScreenPacket")
    val ClientboundRespawnPacket = reqClass("net.minecraft.network.protocol.game.ClientboundRespawnPacket")
    val ClientboundEntityEventPacket = reqClass("net.minecraft.network.protocol.game.ClientboundEntityEventPacket")
    val ClientboundChatPacket = reqClass("net.minecraft.network.protocol.game.ClientboundChatPacket")
    val ClientboundSetPlayerTeamPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket")
    val ClientboundSetPlayerTeamPacketParameters = reqClass("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket\$Parameters")
    val ClientboundSetDisplayChatPreviewPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetDisplayChatPreviewPacket")
    val ClientboundChatPreviewPacket = reqClass("net.minecraft.network.protocol.game.ClientboundChatPreviewPacket")
    val ClientboundPlayerChatPacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerChatPacket")
    val ClientboundSystemChatPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket")
    val ClientboundKeepAlivePacket = reqClass("net.minecraft.network.protocol.game.ClientboundKeepAlivePacket")
    val ClientboundSetCameraPacket = reqClass("net.minecraft.network.protocol.game.ClientboundSetCameraPacket")
    val ClientboundContainerSetContentPacket = reqClass("net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket")

    val ServerboundPlayerActionPacket = reqClass("net.minecraft.network.protocol.game.ServerboundPlayerActionPacket")
    val ServerboundPlayerActionPacketAction = reqClass("net.minecraft.network.protocol.game.ServerboundPlayerActionPacket\$Action")
    val ServerboundInteractPacket = reqClass("net.minecraft.network.protocol.game.ServerboundInteractPacket")
    val ServerboundInteractPacketAction = reqClass("net.minecraft.network.protocol.game.ServerboundInteractPacket\$Action")
    val ServerboundInteractPacketActionType = reqClass("net.minecraft.network.protocol.game.ServerboundInteractPacket\$ActionType")
    val ServerboundInteractPacketActionInteractAt = reqClass("net.minecraft.network.protocol.game.ServerboundInteractPacket\$InteractionAtLocationAction")
    val ServerboundInteractPacketActionInteract = reqClass("net.minecraft.network.protocol.game.ServerboundInteractPacket\$InteractionAction")
    val ServerboundChatPreviewPacket = reqClass("net.minecraft.network.protocol.game.ServerboundChatPreviewPacket")
    val ServerboundKeepAlivePacket = reqClass("net.minecraft.network.protocol.game.ServerboundKeepAlivePacket")
    val ServerboundClientInformationPacket = reqClass("net.minecraft.network.protocol.game.ServerboundClientInformationPacket")

    val ServerPlayer = reqClass("net.minecraft.server.level.ServerPlayer")
    val Player = reqClass("net.minecraft.world.entity.player.Player")
    val ServerLevel = reqClass("net.minecraft.server.level.ServerLevel")
    val ServerLevelAccessor = reqClass("net.minecraft.world.level.ServerLevelAccessor")
    val ServerPlayerGameMode = reqClass("net.minecraft.server.level.ServerPlayerGameMode")
    val Level = reqClass("net.minecraft.world.level.Level")
    val LevelWriter = reqClass("net.minecraft.world.level.LevelWriter")
    val LevelChunk = reqClass("net.minecraft.world.level.chunk.LevelChunk")
    val ChunkAccess = reqClass("net.minecraft.world.level.chunk.ChunkAccess")
    val LevelLightEngine = reqClass("net.minecraft.world.level.lighting.LevelLightEngine")
    val Packet = reqClass("net.minecraft.network.protocol.Packet")
    val ServerGamePacketListenerImpl = reqClass("net.minecraft.server.network.ServerGamePacketListenerImpl")
    val Connection = reqClass("net.minecraft.network.Connection")
    val MinecraftServer = reqClass("net.minecraft.server.MinecraftServer")
    val GameType = reqClass("net.minecraft.world.level.GameType")
    val MobSpawnType = reqClass("net.minecraft.world.entity.MobSpawnType")
    val Pose = reqClass("net.minecraft.world.entity.Pose")
    val Vec3 = reqClass("net.minecraft.world.phys.Vec3")
    val Vec3i = reqClass("net.minecraft.core.Vec3i")
    val Rotations = reqClass("net.minecraft.core.Rotations")
    val Mob = reqClass("net.minecraft.world.entity.Mob")
    val Entity = reqClass("net.minecraft.world.entity.Entity")
    val LivingEntity = reqClass("net.minecraft.world.entity.LivingEntity")
    val BlockEntity = reqClass("net.minecraft.world.level.block.entity.BlockEntity")
    val SpawnerBlockEntity = reqClass("net.minecraft.world.level.block.entity.SpawnerBlockEntity")
    val BaseSpawner = reqClass("net.minecraft.world.level.BaseSpawner")
    val SpawnData = reqClass("net.minecraft.world.level.SpawnData")
    val EntityType = reqClass("net.minecraft.world.entity.EntityType")
    val EquipmentSlot = reqClass("net.minecraft.world.entity.EquipmentSlot")
    val InteractionHand = reqClass("net.minecraft.world.InteractionHand")
    val BlockPos = reqClass("net.minecraft.core.BlockPos")
    val ChunkPos = reqClass("net.minecraft.world.level.ChunkPos")
    val Direction = reqClass("net.minecraft.core.Direction")
    val BlockState = reqClass("net.minecraft.world.level.block.state.BlockState")
    val BlockBehaviour = reqClass("net.minecraft.world.level.block.state.BlockBehaviour")
    val BlockStateBase = reqClass("net.minecraft.world.level.block.state.BlockBehaviour\$BlockStateBase")
    val Blocks = reqClass("net.minecraft.world.level.block.Blocks")
    val Block = reqClass("net.minecraft.world.level.block.Block")
    val Component = reqClass("net.minecraft.network.chat.Component")
    val ComponentSerializer = reqClass("net.minecraft.network.chat.Component\$Serializer")
    val Item = reqClass("net.minecraft.world.item.Item")
    val ItemStack = reqClass("net.minecraft.world.item.ItemStack")
    val Potion = reqClass("net.minecraft.world.item.alchemy.Potion")
    val Potions = reqClass("net.minecraft.world.item.alchemy.Potions")
    val PotionUtils = reqClass("net.minecraft.world.item.alchemy.PotionUtils")
    val SynchedEntityData = reqClass("net.minecraft.network.syncher.SynchedEntityData")
    val DataItem = reqClass("net.minecraft.network.syncher.SynchedEntityData\$DataItem")
    val Tag = reqClass("net.minecraft.nbt.Tag")
    val CompoundTag = reqClass("net.minecraft.nbt.CompoundTag")
    val ListTag = reqClass("net.minecraft.nbt.ListTag")
    val StringTag = reqClass("net.minecraft.nbt.StringTag")
    val TagParser = reqClass("net.minecraft.nbt.TagParser")
    val EntityDataSerializer = reqClass("net.minecraft.network.syncher.EntityDataSerializer")
    val EntityDataSerializers = reqClass("net.minecraft.network.syncher.EntityDataSerializers")
    val EntityDataAccessor = reqClass("net.minecraft.network.syncher.EntityDataAccessor")
    val ResourceLocation = reqClass("net.minecraft.resources.ResourceLocation")
    val ResourceKey = reqClass("net.minecraft.resources.ResourceKey")
    val Advancement = reqClass("net.minecraft.advancements.Advancement")
    val AdvancementBuilder = reqClass("net.minecraft.advancements.Advancement\$Builder")
    val AdvancementProgress = reqClass("net.minecraft.advancements.AdvancementProgress")
    val ServerAdvancementManager = reqClass("net.minecraft.server.ServerAdvancementManager")
    val FrameType = reqClass("net.minecraft.advancements.FrameType")
    val DeserializationContext = reqClass("net.minecraft.advancements.critereon.DeserializationContext")
    val PredicateManager = reqClass("net.minecraft.world.level.storage.loot.PredicateManager")
    val GsonHelper = reqClass("net.minecraft.util.GsonHelper")
    val CreativeModeTab = reqClass("net.minecraft.world.item.CreativeModeTab")
    val AbstractContainerMenu = reqClass("net.minecraft.world.inventory.AbstractContainerMenu")
    val MenuType = reqClass("net.minecraft.world.inventory.MenuType")
    val DimensionType = reqClass("net.minecraft.world.level.dimension.DimensionType")
    val ParticleOptions = reqClass("net.minecraft.core.particles.ParticleOptions")
    val DifficultyInstance = reqClass("net.minecraft.world.DifficultyInstance")
    val SpawnGroupData = reqClass("net.minecraft.world.entity.SpawnGroupData")
    val ChatType = reqClass("net.minecraft.network.chat.ChatType")
    val VillagerData = reqClass("net.minecraft.world.entity.npc.VillagerData")
    val VillagerType = reqClass("net.minecraft.world.entity.npc.VillagerType")
    val VillagerProfession = reqClass("net.minecraft.world.entity.npc.VillagerProfession")
    val ChatFormatting = reqClass("net.minecraft.ChatFormatting")
    val BoatType = reqClass("net.minecraft.world.entity.vehicle.Boat\$Type")
    val Registry = reqClass("net.minecraft.core.Registry")
    val MappedRegistry = reqClass("net.minecraft.core.MappedRegistry")
    val WritableRegistry = reqClass("net.minecraft.core.WritableRegistry")
    val RegistryAccess = reqClass("net.minecraft.core.RegistryAccess")
    val BuiltinRegistries = reqClass("net.minecraft.data.BuiltinRegistries")
    val Holder = reqClass("net.minecraft.core.Holder")
    val Biome = reqClass("net.minecraft.world.level.biome.Biome")
    val BiomeBuilder = reqClass("net.minecraft.world.level.biome.Biome\$BiomeBuilder")
    val BiomeCategory = reqClass("net.minecraft.world.level.biome.Biome\$BiomeCategory")
    val BiomePrecipitation = reqClass("net.minecraft.world.level.biome.Biome\$Precipitation")
    val TemperatureModifier = reqClass("net.minecraft.world.level.biome.Biome\$TemperatureModifier")
    val BiomeGenerationSettings = reqClass("net.minecraft.world.level.biome.BiomeGenerationSettings")
    val BiomeSpecialEffects = reqClass("net.minecraft.world.level.biome.BiomeSpecialEffects")
    val BiomeSpecialEffectsBuilder = reqClass("net.minecraft.world.level.biome.BiomeSpecialEffects\$Builder")
    val BiomeSpecialEffectsGrassColorModifier = reqClass("net.minecraft.world.level.biome.BiomeSpecialEffects\$GrassColorModifier")
    val MobSpawnSettings = reqClass("net.minecraft.world.level.biome.MobSpawnSettings")
    val SoundEvent = reqClass("net.minecraft.sounds.SoundEvent")
    val SoundType = reqClass("net.minecraft.world.level.block.SoundType")
    val ChatSender = reqClass("net.minecraft.network.chat.ChatSender")
    val CryptSaltSignaturePair = reqClass("net.minecraft.util.Crypt\$SaltSignaturePair")
    val PlayerChatMessage = reqClass("net.minecraft.network.chat.PlayerChatMessage")
    val ProfilePublicKey = reqClass("net.minecraft.world.entity.player.ProfilePublicKey")
    val NonNullList = reqClass("net.minecraft.core.NonNullList")
    val MobEffectInstance = reqClass("net.minecraft.world.effect.MobEffectInstance")
    val MobEffect = reqClass("net.minecraft.world.effect.MobEffect")
    val Scoreboard = reqClass("net.minecraft.world.scores.Scoreboard")
    val PlayerTeam = reqClass("net.minecraft.world.scores.PlayerTeam")
    val Team = reqClass("net.minecraft.world.scores.Team")
    val CollisionRule = reqClass("net.minecraft.world.scores.Team\$CollisionRule")
    val Visibility = reqClass("net.minecraft.world.scores.Team\$Visibility")

    val CrossbowItem = reqClass("net.minecraft.world.item.CrossbowItem")

    val ArmorStand = reqClass("net.minecraft.world.entity.decoration.ArmorStand")
    val Arrow = reqClass("net.minecraft.world.entity.projectile.Arrow")
    val ThrownPotion = reqClass("net.minecraft.world.entity.projectile.ThrownPotion")
    val ThrownTrident = reqClass("net.minecraft.world.entity.projectile.ThrownTrident")
    val ThrowableItemProjectile = reqClass("net.minecraft.world.entity.projectile.ThrowableItemProjectile")
    val ItemEntity = reqClass("net.minecraft.world.entity.item.ItemEntity")
    val FallingBlockEntity = reqClass("net.minecraft.world.entity.item.FallingBlockEntity")
    val AreaEffectCloud = reqClass("net.minecraft.world.entity.AreaEffectCloud")
    val FishingHook = reqClass("net.minecraft.world.entity.projectile.FishingHook")
    val LegacyFishingHook = reqClass("spigot:EntityFishingHook")
    val FireworkRocketEntity = reqClass("net.minecraft.world.entity.projectile.FireworkRocketEntity")
    val LightningBolt = reqClass("net.minecraft.world.entity.LightningBolt")
    val SignBlockEntity = reqClass("net.minecraft.world.level.block.entity.SignBlockEntity")
    val Villager = reqClass("net.minecraft.world.entity.npc.Villager")
    val Boat = reqClass("net.minecraft.world.entity.vehicle.Boat")
    val Creeper = reqClass("net.minecraft.world.entity.monster.Creeper")

    ClientboundPlayerInfoPacket
        .reqConstructor(ClientboundPlayerInfoPacketAction, ServerPlayer.array())
        .reqField("action")
        .reqField("entries")
    ClientboundPlayerInfoPacketAction
        .reqEnumField("ADD_PLAYER")
        .reqEnumField("UPDATE_GAME_MODE")
        .reqEnumField("UPDATE_LATENCY")
        .reqEnumField("UPDATE_DISPLAY_NAME")
        .reqEnumField("REMOVE_PLAYER")
    ClientboundPlayerInfoUpdatePacket
        .reqConstructor(ClientboundPlayerInfoUpdatePacketAction, ServerPlayer)
        .reqMethod("createPlayerInitializing", Collection::class)
    ClientboundPlayerInfoRemovePacket
        .reqConstructor(List::class)
    ClientboundPlayerInfoUpdatePacketAction
        .reqEnumField("ADD_PLAYER")
        .reqEnumField("INITIALIZE_CHAT")
        .reqEnumField("UPDATE_GAME_MODE")
        .reqEnumField("UPDATE_LISTED")
        .reqEnumField("UPDATE_LATENCY")
        .reqEnumField("UPDATE_DISPLAY_NAME")
    ClientboundPlayerInfoPacketPlayerUpdate
        .reqConstructor("com.mojang.authlib.GameProfile", Int::class, GameType, Component)
        .reqMethod("getProfile")
        .reqMethod("getLatency")
        .reqMethod("getGameMode")
        .reqMethod("getDisplayName")
    ClientboundAddEntityPacket
        .reqConstructor(Int::class, UUID::class, Double::class, Double::class, Double::class, Float::class, Float::class, EntityType, Int::class, Vec3)
        .reqConstructor(Entity, Int::class)
        .reqConstructor(Entity)
        .reqMethod("getId")
        .reqMethod("getUUID")
        .reqMethod("getX")
        .reqMethod("getY")
        .reqMethod("getZ")
        .reqMethod("getXa")
        .reqMethod("getYa")
        .reqMethod("getZa")
        .reqMethod("getyRot")
        .reqMethod("getxRot")
        .reqMethod("getType")
        .reqMethod("getData")
    ClientboundAddPlayerPacket
        .reqConstructor(Player)
        .reqMethod("getEntityId")
        .reqMethod("getPlayerId")
        .reqMethod("getX")
        .reqMethod("getY")
        .reqMethod("getZ")
        .reqMethod("getyRot")
        .reqMethod("getxRot")
    ClientboundRotateHeadPacket
        .reqConstructor(Entity, Byte::class)
        .reqMethod("getEntity", Level)
        .reqMethod("getYHeadRot")
    ClientboundRemoveEntitiesPacket
        .reqConstructor(intArrayOf())
        .reqMethod("getEntityIds")
    ClientboundMoveEntityPacketRot
        .reqConstructor(Int::class, Byte::class, Byte::class, Boolean::class)
    ClientboundMoveEntityPacketPos
        .reqConstructor(Int::class, Short::class, Short::class, Short::class, Boolean::class)
        .reqConstructor(Int::class, Long::class, Long::class, Long::class, Boolean::class)
        .reqConstructor(Int::class, Byte::class, Byte::class, Byte::class, Boolean::class)
    ClientboundMoveEntityPacketPosRot
        .reqConstructor(Int::class, Short::class, Short::class, Short::class, Byte::class, Byte::class, Boolean::class)
        .reqConstructor(Int::class, Long::class, Long::class, Long::class, Byte::class, Byte::class, Boolean::class)
        .reqConstructor(Int::class, Byte::class, Byte::class, Byte::class, Byte::class, Byte::class, Boolean::class)
    ClientboundAnimatePacket
        .reqConstructor(Entity, Int::class)
        .reqMethod("getId")
        .reqMethod("getAction")
    ClientboundBlockBreakAckPacket
        .reqConstructor(BlockPos, BlockState, ServerboundPlayerActionPacketAction, Boolean::class)
    ClientboundSetEntityDataPacket
        .reqConstructor(Int::class, SynchedEntityData, Boolean::class)
        .reqConstructor(Int::class, List::class) //1.19.3 and higher
        .reqMethod("getId")
    ClientboundSetEquipmentPacket
        .reqConstructor(Int::class, List::class)
        .reqConstructor(Int::class, EquipmentSlot, ItemStack)
    ClientboundTeleportEntityPacket
        .reqConstructor(Entity)
        .reqField("id")
        .reqField("x")
        .reqField("y")
        .reqField("z")
        .reqField("yRot")
        .reqField("xRot")
        .reqField("onGround")
    ClientboundSetEntityMotionPacket
        .reqConstructor(Int::class, Vec3)
        .reqConstructor(Int::class, Double::class, Double::class, Double::class)
        .reqMethod("getId")
        .reqMethod("getXa")
        .reqMethod("getYa")
        .reqMethod("getZa")
    ClientboundTakeItemEntityPacket
        .reqConstructor(Int::class, Int::class, Int::class)
        .reqConstructor(Int::class, Int::class)
        .reqMethod("getItemId")
        .reqMethod("getPlayerId")
        .reqMethod("getAmount")
    ClientboundBlockEventPacket
        .reqConstructor(BlockPos, Block, Int::class, Int::class)
        .reqMethod("getPos")
        .reqMethod("getB0")
        .reqMethod("getB1")
        .reqMethod("getBlock")
    ClientboundSetPassengersPacket
        .reqConstructor(Entity)
        .reqMethod("getPassengers")
        .reqMethod("getVehicle")
        .reqField("vehicle")
        .reqField("passengers")
    ClientboundBlockDestructionPacket
        .reqConstructor(Int::class, BlockPos, Int::class)
        .reqMethod("getId")
        .reqMethod("getPos")
        .reqMethod("getProgress")
    ClientboundUpdateAdvancementsPacket
        .reqConstructor(Boolean::class, Collection::class, Set::class, Map::class)
        .reqMethod("getAdded")
        .reqMethod("getRemoved")
        .reqMethod("getProgress")
        .reqMethod("shouldReset")
    ClientboundLevelChunkPacketData
        .reqConstructor(LevelChunk)
    ClientboundLevelChunkWithLightPacket
        .reqConstructor(LevelChunk, LevelLightEngine, BitSet::class, BitSet::class, Boolean::class)
    ClientboundLevelChunkPacket
        .reqConstructor(LevelChunk)
        .reqConstructor(LevelChunk, Int::class)
    ClientboundLightUpdatePacket
        .reqConstructor(ChunkPos, LevelLightEngine, BitSet::class, BitSet::class, Boolean::class)
        .reqConstructor(ChunkPos, LevelLightEngine, Boolean::class)
    ClientboundOpenScreenPacket
        .reqConstructor(Int::class, MenuType, Component)
    ClientboundRespawnPacket
        .reqConstructor(DimensionType, ResourceKey, Long::class, GameType, GameType, Boolean::class, Boolean::class, Boolean::class)
    ClientboundEntityEventPacket
        .reqConstructor(Entity, Byte::class)
    ClientboundChatPacket
        .reqConstructor(Component, ChatType, UUID::class)
        .reqConstructor(Component, ChatType)
        .reqConstructor(Component, Byte::class)
        .reqMethod("getMessage")
        .reqMethod("getType")
        .reqMethod("getSender")
        .reqField("message")
    ClientboundSetPlayerTeamPacket
//        .reqConstructor(String, Int::class, Optional::class, Collection::class)
        .reqConstructor()
        .reqField("name")
        .reqField("displayName")
        .reqField("playerPrefix")
        .reqField("playerSuffix")
        .reqField("nametagVisibility")
        .reqField("collisionRule")
        .reqField("color")
        .reqField("players")
        .reqField("method")
        .reqField("options")
    ClientboundSetPlayerTeamPacketParameters
        .reqConstructor(PlayerTeam)
    ClientboundSetDisplayChatPreviewPacket
        .reqConstructor(Boolean::class)
        .reqMethod("enabled")
    ClientboundChatPreviewPacket
        .reqConstructor(Int::class, Component)
        .reqMethod("queryId")
        .reqMethod("preview")
    ClientboundPlayerChatPacket
//        .reqConstructor(Component, Optional::class, Int::class, ChatSender, Instant::class, CryptSaltSignaturePair)
        .reqMethod("getMessage")
        .reqField("signedContent")
        .reqField("unsignedContent")
        .reqField("typeId")
        .reqField("sender")
        .reqField("timeStamp")
        .reqField("saltSignature")
    ClientboundSystemChatPacket
        .reqMethod("content")
        .reqMethod("typeId")
    ClientboundSetCameraPacket
        .reqField("cameraId")
    ClientboundContainerSetContentPacket
        .reqConstructor(Int::class, Int::class, NonNullList, ItemStack)

    ServerboundPlayerActionPacket
        .reqMethod("getPos")
        .reqMethod("getDirection")
        .reqMethod("getAction")
    ServerboundPlayerActionPacketAction
        .reqEnumField("START_DESTROY_BLOCK")
        .reqEnumField("ABORT_DESTROY_BLOCK")
        .reqEnumField("STOP_DESTROY_BLOCK")
        .reqEnumField("DROP_ALL_ITEMS")
        .reqEnumField("DROP_ITEM")
        .reqEnumField("RELEASE_USE_ITEM")
        .reqEnumField("SWAP_ITEM_WITH_OFFHAND")
    ServerboundInteractPacket
        .reqField("entityId")
        .reqField("action")
        .reqField("usingSecondaryAction")
        .reqField("location")
        .reqField("hand")
    ServerboundInteractPacketAction
        .reqMethod("getType")
        .reqField("INTERACT")
        .reqField("ATTACK")
        .reqField("INTERACT_AT")
    ServerboundInteractPacketActionType
        .reqEnumField("INTERACT")
        .reqEnumField("ATTACK")
        .reqEnumField("INTERACT_AT")
    ServerboundInteractPacketActionInteractAt
        .reqField("hand")
        .reqField("location")
    ServerboundInteractPacketActionInteract
        .reqField("hand")

    ServerPlayer
        .reqConstructor(MinecraftServer, ServerLevel, "com.mojang.authlib.GameProfile")
        .reqConstructor(MinecraftServer, ServerLevel, "com.mojang.authlib.GameProfile", ServerPlayerGameMode)
        .reqConstructor(MinecraftServer, ServerLevel, "com.mojang.authlib.GameProfile", ProfilePublicKey)
        .reqMethod("setCamera", Entity)
        .reqMethod("refreshContainer", AbstractContainerMenu)
        .reqField("connection")
        .reqField("latency")
    ServerPlayerGameMode
        .reqConstructor(ServerLevel)
        .reqConstructor(Level)
    ServerboundChatPreviewPacket
        .reqMethod("queryId")
        .reqMethod("query")
    Level
        .reqMethod("getChunkAt", BlockPos)
        .reqMethod("getChunk", Int::class, Int::class)
        .reqMethod("obfuscated:c:1.8.8", BlockPos)
        .reqMethod("getBlockState", BlockPos)
        .reqMethod("getLightEngine")
        .reqMethod("dimension")
        .reqMethod("dimensionType")
        .reqMethod("dimensionTypeId")
        .reqMethod("getBlockEntity", BlockPos)
        .reqMethod("getCurrentDifficultyAt", BlockPos)
    LevelChunk
        .reqMethod("getBlockState", BlockPos)
        .reqMethod("getFluidState", BlockPos)
        .reqMethod("setBlockState", BlockPos, BlockState, Boolean::class)
        .reqMethod("getBlockEntityNbtForSaving", BlockPos)
        .reqMethod("getLevel")
    ChunkAccess
        .reqMethod("getPos")
    LevelLightEngine
        .reqMethod("checkBlock", BlockPos)
        .reqMethod("hasLightWork")
    Player
        .reqMethod("setEntityOnShoulder", CompoundTag)
        .reqMethod("setShoulderEntityRight", CompoundTag)
        .reqMethod("setShoulderEntityLeft", CompoundTag)
        .reqMethod("getGameProfile")
        .reqMethod("playSound", SoundEvent, Float::class, Float::class)
        .reqField("containerMenu")
        .reqField("DATA_PLAYER_MODE_CUSTOMISATION")
    ServerLevel
        .reqMethod("getSeed")
        .reqMethod("addFreshEntity", Entity)
    ServerGamePacketListenerImpl
        .reqMethod("send", Packet)
        .reqField("connection")
    Connection
        .reqMethod("disconnect", Component)
        .reqMethod("connectToServer", InetSocketAddress::class, Boolean::class)
        .reqMethod("connectToLocalServer", SocketAddress::class)
        .reqMethod("getAverageReceivedPackets")
        .reqMethod("getAverageSentPackets")
        .reqField("channel")
    MinecraftServer
        .reqMethod("registryAccess")
    GameType
        .reqEnumField("SURVIVAL")
        .reqEnumField("CREATIVE")
        .reqEnumField("ADVENTURE")
        .reqEnumField("SPECTATOR")
    MobSpawnType
        .reqEnumField("NATURAL")
        .reqEnumField("CHUNK_GENERATION")
        .reqEnumField("SPAWNER")
        .reqEnumField("STRUCTURE")
        .reqEnumField("BREEDING")
        .reqEnumField("MOB_SUMMONED")
        .reqEnumField("JOCKEY")
        .reqEnumField("EVENT")
        .reqEnumField("CONVERSION")
        .reqEnumField("REINFORCEMENT")
        .reqEnumField("TRIGGERED")
        .reqEnumField("BUCKET")
        .reqEnumField("SPAWN_EGG")
        .reqEnumField("COMMAND")
        .reqEnumField("DISPENSER")
        .reqEnumField("PATROL")
    Pose
        .reqEnumField("STANDING")
        .reqEnumField("FALL_FLYING")
        .reqEnumField("SLEEPING")
        .reqEnumField("SWIMMING")
        .reqEnumField("SPIN_ATTACK")
        .reqEnumField("CROUCHING")
        .reqEnumField("LONG_JUMPING")
        .reqEnumField("DYING")
    Vec3
        .reqConstructor(Double::class, Double::class, Double::class)
        .reqMethod("x")
        .reqMethod("y")
        .reqMethod("z")
    Vec3i
        .reqMethod("getX")
        .reqMethod("getY")
        .reqMethod("getZ")
    Mob
        .reqMethod("finalizeSpawn", ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag)
    Entity
        .reqConstructor(EntityType, Level)
        .reqMethod("getType")
        .reqMethod("getId")
        .reqMethod("setId", Int::class)
        .reqMethod("setPose", Pose)
        .reqMethod("hasPose", Pose)
        .reqMethod("isCrouching")
        .reqMethod("setPos", Double::class, Double::class, Double::class)
        .reqMethod("setRot", Float::class, Float::class)
        .reqMethod("setGlowingTag", Boolean::class) //1.17 and higher
        .reqMethod("hasGlowingTag")
        .reqMethod("setGlowing", Boolean::class) //1.16.5 and below
        .reqMethod("isGlowing")
        .reqMethod("setCustomName", Component)
        .reqMethod("getCustomName")
        .reqMethod("setCustomNameVisible", Boolean::class)
        .reqMethod("isCustomNameVisible")
        .reqMethod("setInvisible", Boolean::class)
        .reqMethod("isInvisible")
        .reqMethod("setInvulnerable", Boolean::class)
        .reqMethod("setIsInPowderSnow", Boolean::class)
        .reqMethod("setItemSlot", EquipmentSlot, ItemStack)
        .reqMethod("setNoGravity", Boolean::class)
        .reqMethod("isNoGravity")
        .reqMethod("setOnGround", Boolean::class)
        .reqMethod("isOnGround")
        .reqMethod("setSprinting", Boolean::class)
        .reqMethod("isSprinting")
        .reqMethod("setSwimming", Boolean::class)
        .reqMethod("isSwimming")
        .reqMethod("setTicksFrozen", Int::class) //1.17 and higher
        .reqMethod("getTicksFrozen") //1.17 and higher
        .reqMethod("setUUID", UUID::class)
        .reqMethod("getUUID")
        .reqMethod("getEntityData")
        .reqMethod("setSharedFlag", Int::class, Boolean::class)
        .reqMethod("getSharedFlag", Int::class)
        .reqMethod("moveTo", Double::class, Double::class, Double::class)
        .reqField("position")
        .reqField("spigot:locX:1.8.8")
        .reqField("spigot:locY:1.8.8")
        .reqField("spigot:locZ:1.8.8")
        .reqField("DATA_CUSTOM_NAME")
        .reqField("DATA_CUSTOM_NAME_VISIBLE")
        .reqField("DATA_SILENT")
        .reqField("DATA_NO_GRAVITY")
        .reqField("DATA_POSE")
        .reqField("DATA_TICKS_FROZEN")
    LivingEntity
        .reqMethod("setArrowCount", Int::class)
        .reqMethod("getArrowCount")
        .reqMethod("setSleepingPos", BlockPos)
        .reqMethod("getSleepingPos")
        .reqMethod("removeEffectParticles")
        .reqMethod("setStingerCount", Int::class)
        .reqMethod("getStingerCount")
        .reqMethod("triggerItemUseEffects", ItemStack, Int::class)
        .reqMethod("startUsingItem", InteractionHand)
        .reqMethod("stopUsingItem")
        .reqMethod("getUseItem")
        .reqMethod("getUseItemRemainingTicks")
        .reqMethod("setLivingEntityFlag", Int::class, Boolean::class)
        .reqField("useItem")
        .reqField("DATA_LIVING_ENTITY_FLAGS")
        .reqField("DATA_HEALTH_ID")
        .reqField("DATA_EFFECT_COLOR_ID")
        .reqField("DATA_EFFECT_AMBIENCE_ID")
        .reqField("DATA_ARROW_COUNT_ID")
        .reqField("DATA_STINGER_COUNT_ID")
        .reqField("SLEEPING_POS_ID")
    SpawnerBlockEntity
        .reqMethod("getSpawner")
    BaseSpawner
        .reqField("nextSpawnData")
    SpawnData
        .reqMethod("getEntityToSpawn")
//    EntityType
//        .reqMethod("loadEntityRecursive", CompoundTag, Level, Function::class)
//        .reqAllEnumFieldsOfVersion("1.19")
    EquipmentSlot
        .reqEnumField("MAINHAND")
        .reqEnumField("OFFHAND")
        .reqEnumField("FEET")
        .reqEnumField("LEGS")
        .reqEnumField("CHEST")
        .reqEnumField("HEAD")
    InteractionHand
        .reqEnumField("MAIN_HAND")
        .reqEnumField("OFF_HAND")
    BlockPos
        .reqConstructor(Int::class, Int::class, Int::class)
        .reqConstructor(Double::class, Double::class, Double::class)
        .reqConstructor(Vec3)
    ChunkPos
        .reqConstructor(Int::class, Int::class)
        .reqMethod("getMiddleBlockX")
        .reqMethod("getMiddleBlockZ")
        .reqMethod("getMinBlockX")
        .reqMethod("getMinBlockZ")
        .reqMethod("getMaxBlockX")
        .reqMethod("getMaxBlockZ")
        .reqMethod("getBlockX", Int::class)
        .reqMethod("getBlockZ", Int::class)
    Direction
        .reqMethod("getName")
        .reqEnumField("DOWN")
        .reqEnumField("UP")
        .reqEnumField("NORTH")
        .reqEnumField("SOUTH")
        .reqEnumField("WEST")
        .reqEnumField("EAST")
    BlockState
        .reqMethod("spigot:getBlock:1.12.2")
    BlockStateBase
        .reqMethod("getBlock")
    Rotations
        .reqConstructor(Float::class, Float::class, Float::class)
        .reqMethod("getX")
        .reqMethod("getY")
        .reqMethod("getZ")
        .reqMethod("getWrappedX")
        .reqMethod("getWrappedY")
        .reqMethod("getWrappedZ")
    Block
        .reqField("spigot:stepSound:1.8.8")
        .reqMethod("byItem", Item)
        .reqMethod("spigot:getById:1.8.8", Int::class)
        .reqMethod("getSoundType", BlockState)
        .reqMethod("defaultBlockState")
        .reqMethod("getId", BlockState)
    Component
        .reqMethod("getStyle")
        .reqMethod("getContents")
        .reqMethod("getString", Int::class)
        .reqMethod("getSiblings")
        .reqMethod("plainCopy")
        .reqMethod("copy")
//    ComponentSerializer
//        .reqMethod("fromJsonLenient", String)
    Item
        .reqMethod("getItemCategory")
    ItemStack
        .reqConstructor(CompoundTag)
        .reqField("EMPTY")
        .reqField("TAG_ENCH")
        .reqField("TAG_DISPLAY")
        .reqField("TAG_DISPLAY_NAME")
        .reqField("TAG_LORE")
        .reqField("TAG_DAMAGE")
        .reqField("TAG_COLOR")
        .reqField("TAG_UNBREAKABLE")
        .reqField("TAG_REPAIR_COST")
        .reqField("TAG_CAN_DESTROY_BLOCK_LIST")
        .reqField("TAG_CAN_PLACE_ON_BLOCK_LIST")
        .reqField("TAG_HIDE_FLAGS")
        .reqMethod("of", CompoundTag)
        .reqMethod("spigot:createStack:1.10.2", CompoundTag)
        .reqMethod("getTag")
        .reqMethod("getOrCreateTag")
        .reqMethod("setTag", CompoundTag)
        .reqMethod("getHoverName")
        .reqMethod("getDisplayName")
        .reqMethod("getItem")
        .reqMethod("save", CompoundTag)
    Potion
        .reqField("name")
    PotionUtils
        .reqMethod("getMobEffects", ItemStack)
        .reqMethod("getColor", ItemStack)
        .reqMethod("getPotion", ItemStack)
        .reqMethod("getPotion", CompoundTag)
        .reqMethod("setPotion", ItemStack, Potion)
    SynchedEntityData
        .reqConstructor(Entity)
        .reqField("itemsById")
        .reqMethod("packDirty") //1.19.3 and higher
        .reqMethod("getNonDefaultValues") //1.19.3 and higher
        .reqMethod("define", EntityDataAccessor, Object::class)
        .reqMethod("defineId", Class::class, EntityDataSerializer)
        .reqMethod("set", EntityDataAccessor, Object::class)
        .reqMethod("get", EntityDataAccessor)
        .reqMethod("obfuscated:a:1.8.8", Int::class, Object::class)
        .reqMethod("spigot:add:1.8.8", Int::class, Int::class)
        .reqMethod("spigot:watch:1.8.8", Int::class, Object::class)
    DataItem
        .reqConstructor(EntityDataAccessor, Object::class)
        .reqField("initialValue")
        .reqMethod("getAccessor")
        .reqMethod("setValue", Object::class)
        .reqMethod("getValue")
        .reqMethod("isSetToDefault")
        .reqMethod("value")
    Tag
        .reqField("OBJECT_HEADER")
        .reqField("ARRAY_HEADER")
        .reqField("OBJECT_REFERENCE")
        .reqField("STRING_SIZE")
        .reqField("TAG_END")
        .reqField("TAG_BYTE")
        .reqField("TAG_SHORT")
        .reqField("TAG_INT")
        .reqField("TAG_LONG")
        .reqField("TAG_FLOAT")
        .reqField("TAG_DOUBLE")
        .reqField("TAG_BYTE_ARRAY")
        .reqField("TAG_STRING")
        .reqField("TAG_LIST")
        .reqField("TAG_COMPOUND")
        .reqField("TAG_INT_ARRAY")
        .reqField("TAG_LONG_ARRAY")
        .reqField("TAG_ANY_NUMERIC")
        .reqField("MAX_DEPTH")
    CompoundTag
        .reqConstructor()
        .reqMethod("getAllKeys")
        .reqMethod("size")
        .reqMethod("put", String, Tag)
        .reqMethod("putString", String, String)
        .reqMethod("get", String)
        .reqMethod("getList", String, Int::class)
        .reqMethod("getString", String)
        .reqMethod("getCompound", String)
        .reqMethod("remove", String)
        .reqMethod("copy")
    ListTag
        .reqConstructor(List::class, Byte::class)
        .reqConstructor()
    StringTag
        .reqConstructor(String)
    TagParser
        .reqMethod("parseTag", String)
    EntityDataSerializer
        .reqMethod("createAccessor", Int::class)
    EntityDataSerializers
        .reqField("Byte")
        .reqField("INT")
        .reqField("FLOAT")
        .reqField("STRING")
        .reqField("COMPONENT")
        .reqField("OPTIONAL_COMPONENT")
        .reqField("ITEM_STACK")
        .reqField("BLOCK_STATE")
        .reqField("BOOLEAN")
        .reqField("PARTICLE")
        .reqField("ROTATIONS")
        .reqField("BLOCK_POS")
        .reqField("OPTIONAL_BLOCK_POS")
        .reqField("DIRECTION")
        .reqField("OPTIONAL_UUID")
        .reqField("COMPOUND_TAG")
        .reqField("VILLAGER_DATA")
        .reqField("OPTIONAL_UNSIGNED_INT")
        .reqField("POSE")
    EntityDataAccessor
        .reqMethod("getId")
    ResourceLocation
        .reqConstructor(String)
        .reqConstructor(String, String)
        .reqMethod("getPath")
        .reqMethod("getNamespace")
        .reqMethod("toString")
    ResourceKey
        .reqMethod("create", ResourceKey, ResourceLocation)
    Advancement
        .reqMethod("getDisplay")
        .reqMethod("getRewards")
        .reqMethod("getCriteria")
        .reqMethod("toString")
        .reqMethod("getId")
        .reqMethod("getRequirements")
        .reqMethod("getChatComponent")
    AdvancementBuilder
        .reqMethod("advancement")
        .reqMethod("parent", Advancement)
        .reqMethod("parent", ResourceLocation)
        .reqMethod("serializeToJson")
        .reqMethod("fromJson", "com.google.gson.JsonObject", DeserializationContext)
        .reqMethod("build", ResourceLocation)
    AdvancementProgress
        .reqConstructor()
        .reqMethod("update", Map::class, "java.lang.String[][]")
        .reqMethod("isDone")
        .reqMethod("grantProgress", String)
        .reqMethod("revokeProgress", String)
    ServerAdvancementManager
        .reqField("GSON")
    DeserializationContext
        .reqConstructor(ResourceLocation, PredicateManager)
    PredicateManager
        .reqConstructor()
    GsonHelper
        .reqMethod("fromJson", "com.google.gson.Gson", String, Class::class)
    CreativeModeTab
        .reqField("langId")
    AbstractContainerMenu
        .reqMethod("sendAllDataToRemote")
        .reqField("containerId")
    MenuType
        .reqEnumField("GENERIC_9x1")
        .reqEnumField("GENERIC_9x2")
        .reqEnumField("GENERIC_9x3")
        .reqEnumField("GENERIC_9x4")
        .reqEnumField("GENERIC_9x5")
        .reqEnumField("GENERIC_9x6")
    DimensionType
        .reqField("DEFAULT_OVERWORLD")
        .reqField("DEFAULT_NETHER")
        .reqField("DEFAULT_END")
    ChatType
        .reqEnumField("CHAT")
        .reqEnumField("SYSTEM")
        .reqEnumField("GAME_INFO")
    VillagerData
        .reqConstructor(VillagerType, VillagerProfession, Int::class)
        .reqMethod("getType")
        .reqMethod("getProfession")
    VillagerType
        .reqField("DESERT")
        .reqField("JUNGLE")
        .reqField("PLAINS")
        .reqField("SAVANNA")
        .reqField("SNOW")
        .reqField("SWAMP")
        .reqField("TAIGA")
    VillagerProfession
        .reqField("NONE")
        .reqField("ARMORER")
        .reqField("BUTCHER")
        .reqField("CARTOGRAPHER")
        .reqField("CLERIC")
        .reqField("FARMER")
        .reqField("FISHERMAN")
        .reqField("FLETCHER")
        .reqField("LEATHERWORKER")
        .reqField("LIBRARIAN")
        .reqField("MASON")
        .reqField("NITWIT")
        .reqField("SHEPHERD")
        .reqField("TOOLSMITH")
        .reqField("WEAPONSMITH")
    ChatFormatting
        .reqEnumField("BLACK")
        .reqEnumField("DARK_BLUE")
        .reqEnumField("DARK_GREEN")
        .reqEnumField("DARK_AQUA")
        .reqEnumField("DARK_RED")
        .reqEnumField("DARK_PURPLE")
        .reqEnumField("GOLD")
        .reqEnumField("GRAY")
        .reqEnumField("DARK_GRAY")
        .reqEnumField("BLUE")
        .reqEnumField("GREEN")
        .reqEnumField("AQUA")
        .reqEnumField("RED")
        .reqEnumField("LIGHT_PURPLE")
        .reqEnumField("YELLOW")
        .reqEnumField("WHITE")
        .reqEnumField("OBFUSCATED")
        .reqEnumField("BOLD")
        .reqEnumField("STRIKETHROUGH")
        .reqEnumField("UNDERLINE")
        .reqEnumField("ITALIC")
        .reqEnumField("RESET")
    BoatType
        .reqEnumField("OAK")
        .reqEnumField("SPRUCE")
        .reqEnumField("BIRCH")
        .reqEnumField("JUNGLE")
        .reqEnumField("ACACIA")
        .reqEnumField("DARK_OAK")
    Registry
        .reqField("BIOME_REGISTRY")
        .reqMethod("getOrThrow", ResourceKey)
        .reqMethod("get", ResourceKey)
        .reqMethod("get", ResourceLocation)
        .reqMethod("register", Registry, ResourceLocation, Object::class)
        .reqMethod("register", Registry, ResourceKey, Object::class)
    MappedRegistry
        .reqField("frozen")
    WritableRegistry
        .reqMethod("register", ResourceKey, Object::class, "com.mojang.serialization.Lifecycle")
    RegistryAccess
        .reqMethod("ownedRegistryOrThrow", ResourceKey)
    BuiltinRegistries
        .reqField("BIOME")
        .reqMethod("register", Registry, ResourceKey, Object::class)
    Holder
        .reqMethod("direct", Object::class)
    Biome
        .reqField("generationSettings")
        .reqField("mobSettings")
        .reqMethod("getPrecipitation")
        .reqMethod("getBiomeCategory")
        .reqMethod("getSpecialEffects")
    BiomeBuilder
        .reqConstructor()
        .reqMethod("from", Biome)
        .reqMethod("precipitation", BiomePrecipitation)
        .reqMethod("biomeCategory", BiomeCategory)
        .reqMethod("temperature", Float::class)
        .reqMethod("downfall", Float::class)
        .reqMethod("specialEffects", BiomeSpecialEffects)
        .reqMethod("mobSpawnSettings", MobSpawnSettings)
        .reqMethod("generationSettings", BiomeGenerationSettings)
        .reqMethod("temperatureAdjustment", TemperatureModifier)
        .reqMethod("build")
    TemperatureModifier
        .reqEnumField("NONE")
        .reqEnumField("FROZEN")
    BiomeSpecialEffects
        .reqMethod("getFogColor")
        .reqMethod("getWaterColor")
        .reqMethod("getWaterFogColor")
        .reqMethod("getSkyColor")
        .reqMethod("getFoliageColorOverride")
        .reqMethod("getGrassColorOverride")
        .reqMethod("getGrassColorModifier")
        .reqMethod("getAmbientParticleSettings")
        .reqMethod("getAmbientLoopSoundEvent")
        .reqMethod("getAmbientMoodSettings")
        .reqMethod("getAmbientAdditionsSettings")
        .reqMethod("getBackgroundMusic")
    BiomeSpecialEffectsBuilder
        .reqConstructor()
        .reqMethod("fogColor", Int::class)
        .reqMethod("waterColor", Int::class)
        .reqMethod("waterFogColor", Int::class)
        .reqMethod("skyColor", Int::class)
        .reqMethod("foliageColorOverride", Int::class)
        .reqMethod("grassColorModifier", BiomeSpecialEffectsGrassColorModifier)
        .reqMethod("grassColorOverride", Int::class)
        .reqMethod("build")
    BiomeSpecialEffectsGrassColorModifier
        .reqEnumField("NONE")
        .reqEnumField("DARK_FOREST")
        .reqEnumField("SWAMP")
    SoundEvent
        .reqMethod("getLocation")
    SoundType
        .reqField("breakSound")
        .reqField("stepSound")
        .reqField("placeSound")
        .reqField("hitSound")
        .reqField("fallSound")
    PlayerChatMessage
        .reqMethod("serverContent")
        .reqMethod("signedContent")
        .reqMethod("signature")
        .reqMethod("unsignedContent")
    NonNullList
        .reqConstructor(List::class, Object::class)
        .reqMethod("create")
        .reqMethod("withSize", Int::class, Object::class)
        .reqMethod("get", Int::class)
        .reqMethod("set", Int::class, Object::class)
        .reqMethod("add", Int::class, Object::class)
        .reqMethod("remove", Int::class)
        .reqMethod("size")
        .reqMethod("clear")
    MobEffectInstance
        .reqMethod("getEffect")
        .reqMethod("getDuration")
        .reqMethod("getAmplifier")
        .reqMethod("isAmbient")
        .reqMethod("isVisible")
        .reqMethod("showIcon")
    MobEffect
        .reqMethod("getDescriptionId")
        .reqMethod("getDisplayName")
        .reqMethod("getCategory")
        .reqMethod("getColor")
    PlayerTeam
        .reqConstructor(Scoreboard, String)
        .reqField("scoreboard")
        .reqField("name")
        .reqField("players")
        .reqField("displayName")
        .reqField("playerPrefix")
        .reqField("playerSuffix")
        .reqField("allowFriendlyFire")
        .reqField("seeFriendlyInvisibles")
        .reqField("nameTagVisibility")
        .reqField("deathMessageVisibility")
        .reqField("color")
        .reqField("collisionRule")
        .reqField("displayNameStyle")
    CollisionRule
        .reqEnumField("ALWAYS")
        .reqEnumField("NEVER")
        .reqEnumField("PUSH_OTHER_TEAMS")
        .reqEnumField("PUSH_OWN_TEAM")
    Visibility
        .reqEnumField("ALWAYS")
        .reqEnumField("NEVER")
        .reqEnumField("HIDE_FOR_OTHER_TEAMS")
        .reqEnumField("HIDE_FOR_OWN_TEAM")

    CrossbowItem
        .reqMethod("isCharged", ItemStack)
        .reqMethod("setCharged", ItemStack, Boolean::class)
        .reqMethod("getChargedProjectiles", ItemStack)
        .reqMethod("clearChargedProjectiles", ItemStack)
        .reqMethod("getChargeDuration", ItemStack)
        .reqMethod("getStartSound", Int::class)
        .reqMethod("getPowerForTime", Int::class, ItemStack)

    ArmorStand
        .reqConstructor(EntityType, Level)
        .reqMethod("setHeadPose", Rotations)
        .reqMethod("setBodyPose", Rotations)
        .reqMethod("setLeftArmPose", Rotations)
        .reqMethod("setRightArmPose", Rotations)
        .reqMethod("setLeftLegPose", Rotations)
        .reqMethod("setRightLegPose", Rotations)
        .reqMethod("setMarker", Boolean::class)
        .reqMethod("setNoBasePlate", Boolean::class)
        .reqMethod("setShowArms", Boolean::class)
        .reqMethod("setSmall", Boolean::class)
        .reqMethod("setYBodyRot", Float::class)
        .reqMethod("setYHeadRot", Float::class)
        .reqMethod("getHeadPose")
        .reqMethod("getBodyPose")
        .reqMethod("getLeftArmPose")
        .reqMethod("getRightArmPose")
        .reqMethod("getLeftLegPose")
        .reqMethod("getRightLegPose")
        .reqMethod("isMarker")
        .reqMethod("isNoBasePlate")
        .reqMethod("isShowArms")
        .reqMethod("isSmall")
        .reqMethod("spigot:setGravity:1.8.8", Boolean::class)
        .reqMethod("spigot:hasGravity:1.8.8")
    Arrow
        .reqConstructor(EntityType, Level)
        .reqMethod("setEffectsFromItem", ItemStack)
        .reqMethod("makeParticle", Int::class)
        .reqMethod("getColor")
        .reqMethod("setFixedColor", Int::class)
    ThrownPotion
        .reqConstructor(EntityType, Level)
        .reqConstructor(Level)
        .reqField("DATA_ITEM_STACK")
    ThrownTrident
        .reqConstructor(EntityType, Level)
        .reqConstructor(Level, LivingEntity, ItemStack)
        .reqField("tridentItem")
        .reqField("clientSideReturnTridentTickCount")
        .reqField("ID_LOYALTY")
        .reqField("ID_FOIL")
    ThrowableItemProjectile
        .reqMethod("setItem", ItemStack)
        .reqMethod("getItemRaw")
        .reqField("DATA_ITEM_STACK")
    ItemEntity
        .reqConstructor(Level, Double::class, Double::class, Double::class, ItemStack)
        .reqMethod("setItem", ItemStack)
        .reqMethod("getItem")
        .reqField("DATA_ITEM")
    FallingBlockEntity
        .reqConstructor(Level, Double::class, Double::class, Double::class, BlockState)
        .reqMethod("setStartPos", BlockPos)
        .reqMethod("getBlockState")
        .reqMethod("getAddEntityPacket")
        .reqField("blockState")
    AreaEffectCloud
        .reqConstructor(Level, Double::class, Double::class, Double::class)
        .reqMethod("setRadius", Float::class)
        .reqMethod("getRadius")
        .reqMethod("getColor")
        .reqMethod("setFixedColor", Int::class)
        .reqMethod("getParticle")
        .reqMethod("setParticle", ParticleOptions)
        .reqMethod("setWaiting", Boolean::class)
        .reqMethod("isWaiting")
        .reqMethod("getPotion")
    FishingHook
        .reqConstructor(EntityType, Level) //1.17
        .reqMethod("setOwner", Entity)
        .reqMethod("getAddEntityPacket")
        .reqField("DATA_HOOKED_ENTITY")
        .reqField("DATA_BITING")
    LegacyFishingHook
        .reqConstructor(Player, Level, Int::class, Int::class) //1.14 - 1.16
        .reqConstructor(Level, Player) //1.8 - 1.13
        .reqField("obfuscated:b:1.12.2")
    FireworkRocketEntity
        .reqConstructor(EntityType, Level)
        .reqMethod("hasExplosion")
        .reqMethod("isAttachedToEntity")
        .reqField("DATA_ID_FIREWORKS_ITEM")
        .reqField("DATA_ATTACHED_TO_TARGET")
        .reqField("DATA_SHOT_AT_ANGLE")
    LightningBolt
        .reqConstructor(EntityType, Level)
    SignBlockEntity
        .reqConstructor(BlockPos, BlockState)
        .reqMethod("getMessage", Int::class, Boolean::class)
        .reqMethod("setMessage", Int::class, Component, Component)
        .reqMethod("setMessage", Int::class, Component)
        .reqMethod("getUpdatePacket")
        .reqMethod("hasGlowingText")
        .reqMethod("setHasGlowingText", Boolean::class)
        .reqMethod("markUpdated")
        .reqField("messages")
    Villager
        .reqConstructor(EntityType, Level)
        .reqField("DATA_VILLAGER_DATA")
    Boat
        .reqConstructor(EntityType, Level)
        .reqMethod("setDamage", Float::class)
        .reqMethod("getDamage")
        .reqMethod("setHurtTime", Int::class)
        .reqMethod("getHurtTime")
        .reqMethod("setBubbleTime", Int::class)
        .reqMethod("getBubbleTime")
        .reqMethod("setHurtDir", Int::class)
        .reqMethod("getHurtDir")
        .reqMethod("setType", BoatType)
        .reqMethod("getBoatType")
        .reqField("DATA_ID_PADDLE_LEFT")
        .reqField("DATA_ID_PADDLE_RIGHT")
    Creeper
        .reqField("DATA_SWELL_DIR")
        .reqField("DATA_IS_POWERED")
        .reqField("DATA_IS_IGNITED")
}