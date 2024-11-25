package org.nicolie.captainsfortowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.nicolie.captainsfortowers.commands.CapitanesCommand;
import org.nicolie.captainsfortowers.commands.ListaCommand;
import org.nicolie.captainsfortowers.commands.PrivadoCommand;

public final class CaptainsForTowers extends JavaPlugin implements Listener {
    private boolean privadoActivado = false; // Variable para saber si el modo privado está activado
    private String mundoPrivado = ""; // Variable para guardar el mundo donde se activó el modo privado

    @Override
    public void onEnable() {
        // Registrar los comandos
        this.getCommand("capitanes").setExecutor(new CapitanesCommand());
        this.getCommand("lista").setExecutor(new ListaCommand());
        this.getCommand("privado").setExecutor(new PrivadoCommand());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        // Reinicio inicial de variables
        privadoActivado = false;
        mundoPrivado = "";
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Verificar si el mundo cargado es el mundo con modo privado activado
        if (privadoActivado && event.getWorld().getName().equals(mundoPrivado)) {
            privadoActivado = false; // Desactivar modo privado
            mundoPrivado = ""; // Limpiar el mundo
        }
    }
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        // Verificar si el jugador cambió a un mundo con el modo privado activado
        if (privadoActivado && player.getWorld().getName().equals(mundoPrivado)) {
            // Usar BukkitRunnable para esperar medio segundo antes de ejecutar las acciones
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    // Verificar si el jugador no tiene armadura y ponerlo en modo espectador
                    if (!tieneArmadura(player)) {
                        player.getInventory().clear();
                        player.setGameMode(GameMode.SPECTATOR);
                        player.sendMessage(ChatColor.RED + "Partida iniciada con capitanes, no puedes entrar.");
                        player.performCommand("lista");

                    }
                }
            }, 10L); // 10L equivale a medio segundo (20 ticks por segundo)
        }
    }
    // Verifica si el jugador tiene armadura equipada
    private boolean tieneArmadura(Player player) {
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null && (armorPiece.getType() == Material.LEATHER_HELMET
                    || armorPiece.getType() == Material.LEATHER_CHESTPLATE
                    || armorPiece.getType() == Material.LEATHER_LEGGINGS
                    || armorPiece.getType() == Material.LEATHER_BOOTS)) {
                return true;
            }
        }
        return false;
    }
}