package dev.pickaxetag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Client-only. For every dropped enchanted tool or armor piece, sets the (client-side) custom name of the item
 * entity to a short enchantment summary and makes it visible, so vanilla draws a floating label above it.
 * Silk Touch (aqua) and Fortune (gold) are listed first.
 *
 * Deliberately touches NO item models, shaders, glint or render pipeline, so it layers cleanly on top of the
 * "Enchantment Outlines" resource pack (and any other pack that restyles enchanted items).
 */
public class PickaxeTagClient implements ClientModInitializer {

    /** Max enchantments named in the label; the rest collapse into "+N". */
    private static final int MAX_SHOWN = 3;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(PickaxeTagClient::onTick);
    }

    private static void onTick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null) return;

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ItemEntity itemEntity) {
                tag(itemEntity);
            }
        }
    }

    private static boolean isToolOrArmor(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.HEAD_ARMOR)
                || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR)
                || stack.is(ItemTags.FOOT_ARMOR);
    }

    private static void tag(ItemEntity entity) {
        // Already labelled (by us) or named by something else: leave it alone.
        if (entity.getCustomName() != null) return;

        ItemStack stack = entity.getItem();
        if (!isToolOrArmor(stack)) return;

        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) return;

        List<Holder<Enchantment>> list = new ArrayList<>(enchants.keySet());
        list.sort(Comparator.comparingInt(PickaxeTagClient::priority));

        MutableComponent label = Component.empty();
        int shown = 0;
        for (Holder<Enchantment> holder : list) {
            if (shown >= MAX_SHOWN) break;
            if (shown > 0) label.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            label.append(describe(holder, enchants.getLevel(holder)));
            shown++;
        }
        int hidden = list.size() - shown;
        if (hidden > 0) label.append(Component.literal(" +" + hidden).withStyle(ChatFormatting.DARK_GRAY));

        entity.setCustomName(label);
        entity.setCustomNameVisible(true);
    }

    /** Silk Touch first, then Fortune, then everything else. */
    private static int priority(Holder<Enchantment> holder) {
        if (holder.is(Enchantments.SILK_TOUCH)) return 0;
        if (holder.is(Enchantments.FORTUNE)) return 1;
        return 2;
    }

    private static Component describe(Holder<Enchantment> holder, int level) {
        Enchantment enchantment = holder.value();
        MutableComponent name = enchantment.description().copy();

        if (holder.is(Enchantments.SILK_TOUCH)) {
            name.withStyle(ChatFormatting.AQUA);
        } else if (holder.is(Enchantments.FORTUNE)) {
            name.withStyle(ChatFormatting.GOLD);
        } else {
            name.withStyle(ChatFormatting.GRAY);
        }

        if (level > 1 || enchantment.getMaxLevel() > 1) {
            name.append(" ");
            if (level >= 1 && level <= 10) {
                name.append(Component.translatable("enchantment.level." + level));
            } else {
                name.append(Integer.toString(level));
            }
        }
        return name;
    }
}
