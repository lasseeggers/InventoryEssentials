package net.blay09.mods.inventoryessentials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Comparator;
import java.util.Objects;

public enum SortOrder implements StringRepresentable {
    ALPHABETIC("alphabetic", Comparator
            .comparing((ItemStack itemStack) -> itemStack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
            .thenComparing(itemStack -> itemStack.isEnchanted() ? 0 : 1)
            .thenComparingInt(ItemStack::getDamageValue)
            .thenComparing(itemStack -> Objects.toString(itemStack.getComponents(), ""))),

    ITEM_ID("itemId",
            Comparator.comparing((ItemStack itemStack) -> itemStack.getItem().builtInRegistryHolder().key().toString())
                    .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
                    .thenComparing(itemStack -> itemStack.isEnchanted() ? 0 : 1)
                    .thenComparingInt(ItemStack::getDamageValue)
                    .thenComparing(itemStack -> Objects.toString(itemStack.getComponents(), ""))),

    CREATIVE("creative", (ItemStack a, ItemStack b) -> {
        if (a.isEmpty() && b.isEmpty())
            return 0;
        if (a.isEmpty())
            return 1;
        if (b.isEmpty())
            return -1;

        // Get creative tab display items - convert to list for indexOf support
        java.util.Collection<ItemStack> displayItemsCollection = CreativeModeTabs.searchTab().getDisplayItems();
        java.util.List<ItemStack> displayItems;
        if (displayItemsCollection instanceof java.util.List) {
            displayItems = (java.util.List<ItemStack>) displayItemsCollection;
        } else {
            displayItems = new java.util.ArrayList<>(displayItemsCollection);
        }

        // Find index using a predicate that matches by item type
        int indexA = -1;
        int indexB = -1;
        for (int i = 0; i < displayItems.size(); i++) {
            ItemStack displayItem = displayItems.get(i);
            if (indexA == -1 && ItemStack.isSameItem(a, displayItem)) {
                indexA = i;
            }
            if (indexB == -1 && ItemStack.isSameItem(b, displayItem)) {
                indexB = i;
            }
            if (indexA != -1 && indexB != -1) {
                break;
            }
        }

        // If not found with exact match, try matching just by item type (for modded/enchanted items)
        if (indexA == -1) {
            for (int i = 0; i < displayItems.size(); i++) {
                if (displayItems.get(i).getItem() == a.getItem()) {
                    indexA = i;
                    break;
                }
            }
        }
        if (indexB == -1) {
            for (int i = 0; i < displayItems.size(); i++) {
                if (displayItems.get(i).getItem() == b.getItem()) {
                    indexB = i;
                    break;
                }
            }
        }

        // Items not in creative menu go to the end
        if (indexA == -1)
            indexA = Integer.MAX_VALUE;
        if (indexB == -1)
            indexB = Integer.MAX_VALUE;

        int cmp = Integer.compare(indexA, indexB);
        if (cmp != 0)
            return cmp;

        // Secondary sort by display name (alphabetic) for items at same position
        cmp = a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString());
        if (cmp != 0)
            return cmp;

        // Tertiary sort by count (descending), enchantment, damage, components
        cmp = Integer.compare(b.getCount(), a.getCount());
        if (cmp != 0)
            return cmp;

        cmp = Boolean.compare(a.isEnchanted(), b.isEnchanted());
        if (cmp != 0)
            return -cmp;

        cmp = Integer.compare(a.getDamageValue(), b.getDamageValue());
        if (cmp != 0)
            return cmp;

        return Objects.toString(a.getComponents(), "").compareTo(Objects.toString(b.getComponents(), ""));
    }),

    RARITY("rarity", (ItemStack a, ItemStack b) -> {
        if (a.isEmpty() && b.isEmpty())
            return 0;
        if (a.isEmpty())
            return 1;
        if (b.isEmpty())
            return -1;

        // Sort by rarity (epic > rare > uncommon > common)
        Rarity rarityA = a.getRarity();
        Rarity rarityB = b.getRarity();
        int cmp = Integer.compare(rarityB.ordinal(), rarityA.ordinal());
        if (cmp != 0)
            return cmp;

        // Secondary sort by name alphabetically
        cmp = a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString());
        if (cmp != 0)
            return cmp;

        // Tertiary sort by count (descending), enchantment, damage, components
        cmp = Integer.compare(b.getCount(), a.getCount());
        if (cmp != 0)
            return cmp;

        cmp = Boolean.compare(a.isEnchanted(), b.isEnchanted());
        if (cmp != 0)
            return -cmp;

        cmp = Integer.compare(a.getDamageValue(), b.getDamageValue());
        if (cmp != 0)
            return cmp;

        return Objects.toString(a.getComponents(), "").compareTo(Objects.toString(b.getComponents(), ""));
    }),

    CATEGORY("category", (ItemStack a, ItemStack b) -> {
        if (a.isEmpty() && b.isEmpty())
            return 0;
        if (a.isEmpty())
            return 1;
        if (b.isEmpty())
            return -1;

        // Find which creative tab each item belongs to
        String categoryA = "zzz_unknown";
        String categoryB = "zzz_unknown";

        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.contains(a)) {
                categoryA = tab.getDisplayName().getString();
                break;
            }
        }

        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.contains(b)) {
                categoryB = tab.getDisplayName().getString();
                break;
            }
        }

        int cmp = categoryA.compareToIgnoreCase(categoryB);
        if (cmp != 0)
            return cmp;

    // Secondary sort by count (descending), enchantment, damage, components
        cmp = Integer.compare(b.getCount(), a.getCount());
        if (cmp != 0)
            return cmp;

        cmp = Boolean.compare(a.isEnchanted(), b.isEnchanted());
        if (cmp != 0)
            return -cmp;

        cmp = Integer.compare(a.getDamageValue(), b.getDamageValue());
        if (cmp != 0)
            return cmp;

        return Objects.toString(a.getComponents(), "").compareTo(Objects.toString(b.getComponents(), ""));
    }),

    MAX_STACK_SIZE("maxStackSize", Comparator
            .comparingInt((ItemStack itemStack) -> itemStack.isEmpty() ? -1 : itemStack.getMaxStackSize())
            .reversed()
            .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
            .thenComparing(itemStack -> itemStack.isEnchanted() ? 0 : 1)
            .thenComparingInt(ItemStack::getDamageValue)
            .thenComparing(itemStack -> Objects.toString(itemStack.getComponents(), "")));

    private final String name;
    private final Comparator<ItemStack> comparator;

    SortOrder(String name, Comparator<ItemStack> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String getName() {
        return name;
    }

    public Comparator<ItemStack> getComparator() {
        return comparator;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
