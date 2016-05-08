package net.samagames.survivalapi.modules.gameplay;

import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.SurvivalPlugin;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.survivalapi.modules.AbstractSurvivalModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

/**
 * PersonalBlocksModule class
 *
 * Copyright (c) for SamaGames
 * All right reserved
 */
public class PersonalBlocksModule extends AbstractSurvivalModule
{
    private final Map<Location, UUID> blocksOwner;
    private final List<Material> privateBlocks;
    private SurvivalGame game;

    /**
     * Constructor
     *
     * @param plugin Parent plugin
     * @param api API instance
     * @param moduleConfiguration Module configuration
     */
    public PersonalBlocksModule(SurvivalPlugin plugin, SurvivalAPI api, Map<String, Object> moduleConfiguration)
    {
        super(plugin, api, moduleConfiguration);

        this.blocksOwner = new HashMap<>();
        this.privateBlocks = new ArrayList<>();

        this.privateBlocks.add(Material.WORKBENCH);
        this.privateBlocks.add(Material.FURNACE);
        this.privateBlocks.add(Material.BURNING_FURNACE);
        this.privateBlocks.add(Material.ENCHANTMENT_TABLE);
        this.privateBlocks.add(Material.ANVIL);
        this.privateBlocks.add(Material.BREWING_STAND);
    }

    @Override
    public void onGameStart(SurvivalGame game)
    {
        this.game = game;
    }

    /**
     * Stocking private blocks
     *
     * @param event Event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (this.privateBlocks.contains(event.getBlockPlaced().getType()))
            this.blocksOwner.put(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId());
    }

    /**
     * Private block breaking handling
     *
     * @param event Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.blocksOwner.containsKey(event.getBlock().getLocation()) && this.blocksOwner.get(event.getBlock().getLocation()) != event.getPlayer().getUniqueId())
        {
            UUID id = this.blocksOwner.get(event.getBlock().getLocation());

            if (id == null || id.equals(event.getPlayer().getUniqueId()) || (this.game instanceof SurvivalTeamGame && ((SurvivalTeamGame) this.game).getPlayerTeam(id) == ((SurvivalTeamGame) this.game).getPlayerTeam(event.getPlayer().getUniqueId())))
            {
                this.blocksOwner.remove(event.getBlock().getLocation());
            }
            else if (!this.game.isPvPActivated())
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Ce bloc appartient à " + Bukkit.getOfflinePlayer(this.blocksOwner.get(event.getBlock().getLocation())).getName()  + ". Vous ne pouvez pas le casser actuellement !");
                event.setCancelled(true);
            }
        }
    }
}
