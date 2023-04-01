package ir.syrent.velocityvanish.velocity.vruom.configuration;

import ir.syrent.velocityvanish.velocity.vruom.VRUoMPlugin;
import ir.syrent.velocityvanish.velocity.vruom.utils.ResourceUtils;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

public class ConfigurateYamlConfig {

    private final String fileName;
    private YamlConfigurationLoader loader;

    public CommentedConfigurationNode root;

    public ConfigurateYamlConfig(String fileName) {
        this.fileName = fileName;
    }

    public void create() {
        File copyFile = new File(VRUoMPlugin.getDataDirectory().toFile(), fileName);
        if (!copyFile.exists()) {
            copyFile.getParentFile().mkdirs();
            ResourceUtils.copyResource(fileName, copyFile);
        }

        loader = YamlConfigurationLoader.builder().file(copyFile).build();
    }

    public void load() {
        if (loader == null) {
            create();
        }

        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }
}