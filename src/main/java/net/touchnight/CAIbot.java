package net.touchnight;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

import java.io.IOException;

public final class CAIbot extends JavaPlugin {
    public static final CAIbot INSTANCE = new CAIbot();

    private CAIbot() {
        super(new JvmPluginDescriptionBuilder("net.touchnight.caibot", "0.1.0")
                .name("CAIbot")
                .info("连接qq机器人和cai角色")
                .author("TouchNight")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");
        try {
            GlobalEventChannel.INSTANCE.registerListenerHost(new CAIListener());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}