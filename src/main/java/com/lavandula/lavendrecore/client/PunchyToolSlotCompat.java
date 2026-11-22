package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

final class PunchyToolSlotCompat {
    private PunchyToolSlotCompat() {
    }

    static void initialize() {
        if (!FabricLoader.getInstance().isModLoaded("punchy")) {
            return;
        }
        try {
            Class<?> api = Class.forName("punchy.api.PunchyApi");
            Class<?> equipRules = Class.forName("punchy.api.PunchyApi$EquipRules");
            Object rules = Proxy.newProxyInstance(equipRules.getClassLoader(), new Class<?>[]{equipRules}, (proxy, method, args) -> {
                if (method.getName().equals("skip") && Boolean.TRUE.equals(args[2]) && args[1] instanceof ItemStack next) {
                    ItemStack active = ActiveToolSlot.get(MinecraftClient.getInstance());
                    // Automatic selection and the tool action happen together, so let the action use the hand immediately.
                    if (!active.isEmpty() && ItemStack.areItemsEqual(active, next)) {
                        return true;
                    }
                }
                if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, args);
                }
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "Lavendre Core Tool Slot equip rules";
                    default -> throw new UnsupportedOperationException(method.toString());
                };
            });
            api.getMethod("equipRules", String.class, equipRules).invoke(null, LavendreCore.MOD_ID, rules);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // Older Punchy builds still use the standard held-item queries.
        } catch (ReflectiveOperationException | LinkageError e) {
            LavendreCore.LOGGER.warn("Could not register Tool Slot equip rules with Punchy.", e);
        }
    }
}
