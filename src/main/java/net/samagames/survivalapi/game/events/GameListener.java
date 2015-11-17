package net.samagames.survivalapi.game.events;

import net.samagames.api.games.Status;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.SurvivalGameLoop;
import net.samagames.survivalapi.game.WorldLoader;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.tools.GameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class GameListener implements Listener
{
    private SurvivalGame game;
    private Random random;

    public GameListener(SurvivalGame game)
    {
        this.game = game;
        this.random = new Random();
    }

    /**
     * Save the last damager of a damaged player
     *
     * @param event Event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player damaged = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (damager instanceof Player)
            {
                if (!this.game.isPvPActivated() || (this.game instanceof SurvivalTeamGame && ((SurvivalTeamGame) this.game).getPlayerTeam(damager.getUniqueId()).hasPlayer(damaged.getUniqueId())))
                {
                    event.setCancelled(true);
                    return;
                }

                damaged.setMetadata("lastDamager", new FixedMetadataValue(this.game.getPlugin(), damager));

                if (((Player) damager).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
                    event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
            }
            else if (damager instanceof Projectile)
            {
                Projectile arrow = (Projectile) damager;

                if (arrow.getShooter() instanceof Player)
                {
                    Player shooter = (Player) arrow.getShooter();

                    if (!this.game.isPvPActivated() || (this.game instanceof SurvivalTeamGame && ((SurvivalTeamGame<SurvivalGameLoop>) this.game).getPlayerTeam(shooter.getUniqueId()).hasPlayer(damaged.getUniqueId())))
                    {
                        event.setCancelled(true);
                        return;
                    }

                    damaged.setMetadata("lastDamager", new FixedMetadataValue(this.game.getPlugin(), shooter));

                    if (shooter.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
                        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                }
            }
        }
    }

    /**
     * Increase the Renegartion boost when a golden apple is eaten
     *
     * @param event Event
     */
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event)
    {
        if (event.getItem().getType() == Material.GOLDEN_APPLE)
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
    }

    /**
     * Block Minecraft utilization
     *
     * @param event Event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == (Material.MINECART))
        {
            event.getPlayer().sendMessage(ChatColor.RED + "L'utilisation de Minecart est bloqué.");
            event.setCancelled(true);
        }
    }

    /**
     * Handle player death
     *
     * @param event Event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        if (this.game.hasPlayer(event.getEntity()) && !this.game.isSpectator(event.getEntity()))
        {
            this.game.stumpPlayer(event.getEntity(), false);

            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));

            if (event.getEntity().getKiller() != null)
            {
                event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1));
                event.setDeathMessage("");
            }
            else
            {
                event.setDeathMessage(this.game.getCoherenceMachine().getGameTag() + " " + event.getDeathMessage());
            }

            GameUtils.broadcastSound(Sound.WITHER_SPAWN);
        }
    }

    /**
     * Disable Guardian spawn (Mining Fatique effect)
     *
     * @param event Event
     */
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if (event.getEntityType() == EntityType.GUARDIAN)
            event.setCancelled(true);
    }

    /**
     * Cancel damages if the game doesn't activate them
     *
     * @param event Event
     */
    @EventHandler
    public void onDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player && !this.game.isDamagesActivated())
            event.setCancelled(true);
    }

    /**
     * Keep the player's food level if the game isn't started
     *
     * @param event Event
     */
    @EventHandler
    public void onLoseFood(FoodLevelChangeEvent event)
    {
        event.setCancelled(this.game.getStatus() != Status.IN_GAME || (this.game.hasPlayer((Player) event.getEntity()) && !this.game.isSpectator((Player) event.getEntity())));
    }

    /**
     * Handle Towers
     *
     * @param event Event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (this.game.isPvPActivated() && event.getBlockPlaced().getY() > WorldLoader.getHighestNaturalBlockAt(event.getBlockPlaced().getX(), event.getBlockPlaced().getZ()) + 15)
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.RED + "Tours" + ChatColor.DARK_RED + "] " + ChatColor.RED + "Les Tours sont interdites.");
        }
    }

    /**
     * Disable lava buckets if the PvP isn't activated
     *
     * @param event Event
     */
    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {
        if (event.getBucket().equals(Material.LAVA_BUCKET) && !this.game.isPvPActivated())
        {
            event.getPlayer().sendMessage(ChatColor.RED + "Le PVP est désactivé, l'utilisation de sources de lave est interdite.");
            event.getPlayer().getWorld().getBlockAt(event.getBlockClicked().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ())).setType(Material.AIR);
            event.getPlayer().getItemInHand().setType(Material.LAVA_BUCKET);

            event.setCancelled(true);
        }
    }

    /**
     * Control sign contents
     *
     * @param event Event
     */
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        for (int i = 0; i < 4; i++)
            if (event.getLine(i).matches("^[a-zA-Z0-9ÀÁÂÄÇÈÉÊËÌÍÎÏÒÓÔÖÙÚÛÜàáâäçèéêëîïôöûü &]*$"))
                if (event.getLine(i).length() > 20)
                    event.setCancelled(true);
    }
}