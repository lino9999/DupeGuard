package com.Lino.dupeGuard.model;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import java.util.Objects;

@Getter
public class CheckItem {
    private final ItemStack itemStack;
    private final int cachedHash;

    public CheckItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
        this.cachedHash = calculateHash();
    }

    private int calculateHash() {
        if (itemStack.hasItemMeta()) {
            return Objects.hash(itemStack.getType(), itemStack.getItemMeta());
        }
        return Objects.hash(itemStack.getType());
    }

    public boolean matches(ItemStack other) {
        if (other == null) return false;
        return itemStack.isSimilar(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckItem)) return false;
        CheckItem that = (CheckItem) o;
        return matches(that.itemStack);
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }
}