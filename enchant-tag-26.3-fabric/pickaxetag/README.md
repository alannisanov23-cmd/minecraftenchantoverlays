# Enchant Tag (Fabric, Minecraft 26.3)

Client-side mod. Dropped **tools and armor** that carry enchantments get a floating label above them listing
up to 3 enchantments, e.g. `Silk Touch, Efficiency V, Unbreaking III +1`.

- **Silk Touch** is aqua, **Fortune** is gold, everything else is gray. Those two are always listed first.
- Covers pickaxes, axes, shovels, hoes, swords, and all helmets/chestplates/leggings/boots.
- Unenchanted items get no label.

## Compatibility with Enchantment Outlines
Enchantment Outlines is a resource pack that changes how enchanted items are drawn. This mod does not touch item
models, shaders, or the glint - it only uses vanilla's name-tag rendering on the dropped item entity - so the two
stack without conflict. Both are client-only.

## Build
Requires JDK 25.

    gradle wrapper --gradle-version 9.6.0   # first time only
    ./gradlew build        # jar in build/libs/ (use the one WITHOUT -sources)
    ./gradlew runClient    # test in a dev instance

## Install
Jar into `.minecraft/mods` with Fabric Loader >= 0.19.5 and Fabric API 0.161.0+26.3 on Minecraft 26.3.

## Tweak
`MAX_SHOWN` and `isToolOrArmor()` in `PickaxeTagClient.java` control how many enchantments show and which items count.
