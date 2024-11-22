package org.nicolie.captainsfortowers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Listener;

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

    }
    public static class CapitanesCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (command.getName().equalsIgnoreCase("capitanes")) {
    
                // Verificar que el sender sea un jugador
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
                    return true;
                }
                Player player = (Player) sender;
                // Verificar permisos
                if (!(player.hasPermission("towers.admin") || player.isOp())) {
                    player.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                    return true;
                }
                // Verificar cantidad de argumentos
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Debes especificar al menos dos jugadores.");
                    return true;
                }
                List<String> players = Arrays.asList(args); // Obtener lista de jugadores
                String worldName = player.getWorld().getName(); // Obtener mundo del jugador
    
                // Ruta al archivo gameSettings.yml
                File gameSettingsFile = new File(Bukkit.getServer().getPluginManager()
                        .getPlugin("AmazingTowers") // Nombre del plugin
                        .getDataFolder() + "/instances/" + worldName + "/gameSettings.yml"); // Ruta al archivo
    
                // Verificar existencia del archivo
                if (!gameSettingsFile.exists()) {
                    player.sendMessage(ChatColor.RED + "No se pudo añadir los capitanes.");
                    return true;
                }
    
                YamlConfiguration config = YamlConfiguration.loadConfiguration(gameSettingsFile);
                config.set("possibleCaptains.activated", "true");
                config.set("possibleCaptains.players", players);
    
                try {
                    config.save(gameSettingsFile);
                    player.sendMessage(ChatColor.GREEN + "¡Capitanes actualizados correctamente!");
    
                    // Ejecutar comando para recargar configuración
                    String reloadCommand = String.format("tt reloadconfig game_settings %s", worldName);
                    player.performCommand(reloadCommand);
    
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED + "Hubo un error al guardar los capitanes.");
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    }
    
    public static class ListaCommand implements CommandExecutor {
        @Override
        // si el comando ingresado es /lista
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (command.getName().equalsIgnoreCase("lista")) {
                // si el jugador tiene el permiso "towers.admin" o es OP
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String worldName = player.getWorld().getName();

                    // lista de jugadores en los equipos
                    List<String> rojo = new ArrayList<>();
                    List<String> azul = new ArrayList<>();
                    List<String> espectadores = new ArrayList<>();

                    // recorre todos los jugadores en línea pero solo los que están en el mundo actual
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!onlinePlayer.getWorld().getName().equals(worldName)) {
                            continue;
                        }
                        // determina el equipo del jugador
                        String equipo = determinarEquipoPorArmadura(onlinePlayer);

                        // agrega el jugador a la lista de su equipo
                        if (equipo.equals("ROJO")) {
                            rojo.add(onlinePlayer.getName());
                        } else if (equipo.equals("AZUL")) {
                            azul.add(onlinePlayer.getName());
                        } else {
                            espectadores.add(onlinePlayer.getName());
                        }
                    }
                    
                    // crea un mensaje con la lista de jugadores en los equipos
                    StringBuilder mensaje = new StringBuilder();
                    mensaje.append("§7Teams:\n");
                    mensaje.append(formatearMensajeEquipo("§4 Red", rojo, "§4", "§8", "§f"));
                    mensaje.append(formatearMensajeEquipo("§9 Blue", azul, "§9", "§8", "§f"));
                    mensaje.append(formatearMensajeEquipo("§b Observers", espectadores, "§b", "§8", "§f"));
                    // envía el mensaje al jugador que ejecutó el comando
                    sender.sendMessage(mensaje.toString());
                    return true;
                } else {
                    sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
                    return false;
                }
            }
            return false;
        }
        // determina el equipo de un jugador basado en su armadura
        private String determinarEquipoPorArmadura(Player player) {
            boolean tieneRojo = false;
            boolean tieneAzul = false;
            
            // recorre todas las piezas de armadura del jugador
            for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                if (armorPiece != null && (armorPiece.getType() == Material.LEATHER_HELMET
                        || armorPiece.getType() == Material.LEATHER_CHESTPLATE
                        || armorPiece.getType() == Material.LEATHER_LEGGINGS
                        || armorPiece.getType() == Material.LEATHER_BOOTS)) {
                    
                    // si la armadura es de cuero, obtiene el color
                    if (armorPiece.getItemMeta() instanceof LeatherArmorMeta) {
                        LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
                        Color color = meta.getColor();
                        // verifica si el color es rojo o azul
                        if (esColorRojo(color)) {
                            tieneRojo = true;
                        } else if (esColorAzul(color)) {
                            tieneAzul = true;
                        }
                    }
                }
            }
            // determina el equipo del jugador
            if (tieneRojo && !tieneAzul) {
                return "ROJO";
            } else if (tieneAzul && !tieneRojo) {
                return "AZUL";
            } else {
                return "ESPECTADOR";
            }
        }
        // verifica si un color es rojo
        private boolean esColorRojo(Color color) {
            return color.getRed() > 200 && color.getGreen() < 100 && color.getBlue() < 100;
        }
        // verifica si un color es azul
        private boolean esColorAzul(Color color) {
            return color.getBlue() > 200 && color.getGreen() < 100 && color.getRed() < 100;
        }
        // formatea un mensaje con la lista de jugadores en un equipo
        private String formatearMensajeEquipo(String titulo, List<String> jugadores, String color, String separador, String colorNum) {
            StringBuilder resultado = new StringBuilder();
            resultado.append(titulo).append(": ").append(colorNum).append(jugadores.size()).append("\n");
            if (!jugadores.isEmpty()) {
                for (int i = 0; i < jugadores.size(); i++) {
                    // si solo hay un jugador, se agrega el nombre del jugador
                    resultado.append(color).append(jugadores.get(i));
                    if (i == jugadores.size() - 2) {
                        // si hay dos jugadores, se agrega " and " entre los nombres
                        resultado.append(separador).append(" and ");
                    } else if (i < jugadores.size() - 1) {
                        // si hay más de dos jugadores, se agrega ", " entre los nombres
                        resultado.append(separador).append(", ");
                    }
                }
                resultado.append("\n");
            }
            return resultado.toString();
        }
    }
    public class PrivadoCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (command.getName().equalsIgnoreCase("privado")) {
                // Verificar permisos
                if (!(sender.hasPermission("towers.admin") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                    return true;
                }
                // Verificar si el sender es un jugador
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
                    return true;
                }
                Player player = (Player) sender;

                // Activar o desactivar el modo privado
                privadoActivado = !privadoActivado;

                if (privadoActivado) {
                    mundoPrivado = player.getWorld().getName(); // Guardar el mundo donde se ejecutó el comando
                    player.sendMessage(ChatColor.GREEN + "Modo privado activado en el mundo " + mundoPrivado + ".");
                } else {
                    mundoPrivado = ""; // Limpiar el mundo al desactivar el modo privado
                    player.sendMessage(ChatColor.RED + "Modo privado desactivado.");
                }

                return true;
            }
            return false;
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

