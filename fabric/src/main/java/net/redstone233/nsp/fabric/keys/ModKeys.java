package net.redstone233.nsp.fabric.keys;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.redstone233.nsp.OneShotMod;
import org.lwjgl.glfw.GLFW;

public class ModKeys {
    public static KeyBinding OPEN_CONFIG_KEY = new KeyBinding(
            "key.nsp.open_config_key",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.nsp"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_KEY);

        OneShotMod.LOGGER.info("注册按键绑定成功");
    }

    public static boolean isOpenConfigKey() {
        return OPEN_CONFIG_KEY.isPressed();
    }
}
