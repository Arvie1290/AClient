package com.ave.addon.modules;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class DisconnectButton extends Module {
    public enum KickType {
        Kick,
        Hurt,
        Disconnect
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<KickType> kickType = sgGeneral.add(new EnumSetting.Builder<KickType>()
        .name("kick-type")
        .description("How the client disconnects.")
        .defaultValue(KickType.Disconnect)
        .build()
    );

    private final Setting<Boolean> customKickMsgToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-kick-msg-toggle")
        .description("Enable custom disconnect message.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> customKickMsg = sgGeneral.add(new StringSetting.Builder()
        .name("custom-kick-msg")
        .description("Custom message. Use %type_disconnect for the type.")
        .defaultValue("§7[ §cAVE§7 ]: %type_disconnect")
        .visible(customKickMsgToggle::get)
        .build()
    );

    public DisconnectButton(Category category) {
        super(category, "disconnect-button", "Lets you disconnect instantly");
    }

    @Override
    public void onActivate() {
        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Ganti client.level menjadi client.world
        if (client.world == null || client.getNetworkHandler() == null) {
            toggle();
            return;
        }

        // 2. Ganti tipe Component menjadi Text
        Text reason;
        if (customKickMsgToggle.get()) {
            String formattedMsg = customKickMsg.get().replace("%type_disconnect", kickType.get().name());
            reason = Text.literal(formattedMsg); // Ganti ke Text.literal
        } else {
            reason = Text.literal("Disconnected via Disconnect Button."); // Ganti ke Text.literal
        }

        // Tip: Di Fabric/Yarn 1.21.11, disarankan pakai client.getNetworkHandler() daripada client.getConnection()
        switch (kickType.get()) {
            case Kick:
                client.execute(() -> {
                    if (client.getNetworkHandler() != null) {
                        client.getNetworkHandler().getConnection().disconnect(reason);
                    }
                });
                break;

            case Hurt:
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().getConnection().disconnect(reason);
                }
                client.setScreen(new DisconnectedScreen(new TitleScreen(), Text.literal("Disconnected"), reason));
                break;

            case Disconnect:
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().getConnection().disconnect(reason);
                }
                client.execute(() -> client.setScreen(new DisconnectedScreen(new TitleScreen(), Text.literal("Disconnected"), reason)));
                break;
        }

        toggle();
    }
}
