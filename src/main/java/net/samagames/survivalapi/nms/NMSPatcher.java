package net.samagames.survivalapi.nms;

import net.minecraft.server.v1_9_R1.*;
import net.samagames.survivalapi.SurvivalPlugin;
import net.samagames.survivalapi.nms.potions.PotionAttackDamageNerf;
import net.samagames.survivalapi.nms.stack.CustomPotion;
import net.samagames.survivalapi.nms.stack.CustomSoup;
import net.samagames.tools.Reflection;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * NMSPatcher class
 *
 * Copyright (c) for SamaGames
 * All right reserved
 */
public class NMSPatcher
{
    private final Logger logger;

    /**
     * Constructor
     *
     * @param plugin Parent plugin
     */
    public NMSPatcher(SurvivalPlugin plugin)
    {
        this.logger = plugin.getLogger();
    }

    /**
     * Modify the Strength potion to do less damages
     *
     * @throws ReflectiveOperationException
     */
    public void patchPotions() throws ReflectiveOperationException
    {
        Reflection.setFinalStatic(PotionEffectType.class.getDeclaredField("acceptingNew"), true);

        Field byIdField = Reflection.getField(PotionEffectType.class, true, "byId");
        Field byNameField = Reflection.getField(PotionEffectType.class, true, "byName");
        ((Map) byNameField.get(null)).remove("increase_damage");
        ((PotionEffectType[]) byIdField.get(null))[5] = null;

        this.logger.info("Patching Strength Potion (130% => 43.3%, 260% => 86.6%)");
        Reflection.setFinalStatic(MobEffectList.class.getDeclaredField("INCREASE_DAMAGE"), new PotionAttackDamageNerf());
        this.logger.info("Potions patched");
    }

    /**
     * Replace certain ItemStack to our customs
     */
    public void patchStackable()
    {
        this.logger.info("Patching Potion and Soup to change their stack size...");

        try
        {
            Method register = Item.class.getDeclaredMethod("a", int.class, String.class, Item.class);
            register.setAccessible(true);

            Item potion = new CustomPotion();
            Item soup = new CustomSoup();

            register.invoke(null, 373, "potion", potion);
            register.invoke(null, 282, "mushroom_stew", soup);

            Reflection.setFinalStatic(Items.class.getDeclaredField("POTION"), potion);
            Reflection.setFinalStatic(Items.class.getDeclaredField("MUSHROOM_STEW"), soup);
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Modify the spawn rate of the animals
     *
     * @throws ReflectiveOperationException
     */
    public void patchAnimals() throws ReflectiveOperationException
    {
        Field defaultMobField = BiomeBase.class.getDeclaredField("v");
        defaultMobField.setAccessible(true);

        ArrayList<BiomeBase.BiomeMeta> mobs = new ArrayList<>();

        mobs.add(new BiomeBase.BiomeMeta(EntitySheep.class, 15, 10, 10));
        mobs.add(new BiomeBase.BiomeMeta(EntityRabbit.class, 4, 3, 5));
        mobs.add(new BiomeBase.BiomeMeta(EntityPig.class, 15, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityChicken.class, 20, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityCow.class, 20, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityWolf.class, 5, 5, 10));

        for (MinecraftKey biomeKey : BiomeBase.REGISTRY_ID.keySet())
            defaultMobField.set(BiomeBase.REGISTRY_ID.get(biomeKey), mobs);
    }

    /**
     * Add more reeds in a chunk of a given biome
     *
     * @throws ReflectiveOperationException
     */
    public void patchReeds() throws ReflectiveOperationException
    {
        for (MinecraftKey biomeKey : BiomeBase.REGISTRY_ID.keySet())
        {
            BiomeBase biome = BiomeBase.REGISTRY_ID.get(biomeKey);
            Reflection.setValue(biome.t, BiomeDecorator.class, true, "E", (int) Reflection.getValue(biome.t, BiomeDecorator.class, true, "E") * 2);
        }
    }
}
