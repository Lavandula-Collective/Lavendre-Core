package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

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
            registerEquipRules(api);
            registerHeldItemLook(api);
        } catch (ReflectiveOperationException | LinkageError e) {
            LavendreCore.LOGGER.warn("Could not register Tool Slot rendering with Punchy.", e);
        }
    }

    private static void registerEquipRules(Class<?> api) throws ReflectiveOperationException {
        try {
            Class<?> equipRules = Class.forName("punchy.api.PunchyApi$EquipRules");
            Object rules = callback(equipRules, (proxy, method, args) -> {
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
                return null;
            });
            api.getMethod("equipRules", String.class, equipRules).invoke(null, LavendreCore.MOD_ID, rules);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // Older Punchy builds may lack this hook.
        }
    }

    private static void registerHeldItemLook(Class<?> api) throws ReflectiveOperationException {
        try {
            Class<?> heldItemLook = Class.forName("punchy.api.PunchyApi$HeldItemLook");
            Object look = callback(heldItemLook, (proxy, method, args) -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || args[0] != client.player
                        || (client.player.getMainArm() == Arm.LEFT) != (boolean) args[1]) {
                    return null;
                }
                // Punchy can keep drawing its cached item until the mining animation ends.
                ItemStack active = ActiveToolSlot.get(client);
                if (active.isEmpty() || args[2] instanceof ItemStack drawn && ItemStack.areEqual(active, drawn)) {
                    return null;
                }
                return active;
            });
            api.getMethod("heldItemLook", String.class, heldItemLook).invoke(null, LavendreCore.MOD_ID, look);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // Older Punchy builds still use the standard held-item queries.
        }
    }

    private static Object callback(Class<?> type, InvocationHandler handler) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> switch (method.getName()) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "Lavendre Core Tool Slot " + type.getSimpleName();
            default -> handler.invoke(proxy, method, args);
        });
    }
}
