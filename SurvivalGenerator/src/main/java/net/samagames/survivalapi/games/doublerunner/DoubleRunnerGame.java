package net.samagames.survivalapi.games.doublerunner;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.samagames.survivalapi.SurvivalGenerator;
import net.samagames.survivalapi.games.AbstractGame;
import net.samagames.survivalapi.gen.OrePopulator;
import net.samagames.survivalapi.gen.OreRemoverPopulator;
import org.bukkit.Material;
import org.bukkit.World;

public class DoubleRunnerGame extends AbstractGame
{
    public DoubleRunnerGame(SurvivalGenerator plugin)
    {
        super(plugin);
    }

    @Override
    public void preInit()
    {
        this.plugin.addBiomeToRemove(BiomeBase.ICE_PLAINS);
        this.plugin.addBiomeToRemove(BiomeBase.ICE_MOUNTAINS);
        this.plugin.addBiomeToRemove(BiomeBase.JUNGLE);
        this.plugin.addBiomeToRemove(BiomeBase.JUNGLE_HILLS);
        this.plugin.addBiomeToRemove(BiomeBase.JUNGLE_EDGE);
    }

    @Override
    public void init(World world)
    {
        OrePopulator orePopulator = new OrePopulator();
        OreRemoverPopulator oreRemoverPopulator = new OreRemoverPopulator();

        orePopulator.addRule(new OrePopulator.Rule(Material.DIAMOND_ORE, 4, 0, 64, 8));
        orePopulator.addRule(new OrePopulator.Rule(Material.IRON_ORE, 2, 0, 64, 15));
        orePopulator.addRule(new OrePopulator.Rule(Material.OBSIDIAN, 4, 0, 64, 10));
        orePopulator.addRule(new OrePopulator.Rule(Material.QUARTZ_BLOCK, 3, 0, 64, 6));

        oreRemoverPopulator.removeOre(Material.GOLD_ORE);
        oreRemoverPopulator.removeOre(Material.LAPIS_ORE);

        world.getPopulators().add(orePopulator);
        world.getPopulators().add(oreRemoverPopulator);
    }
}
