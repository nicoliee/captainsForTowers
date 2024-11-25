package org.nicolie.captainsfortowers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrivadoCommand implements CommandExecutor {
        private boolean privadoActivado = false;
        private String mundoPrivado = "";
    
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
    
                // Verificar si se ha pasado un argumento
                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "Uso incorrecto. Usa /privado on o /privado off.");
                    return true;
                }
    
                String argumento = args[0].toLowerCase();
    
                if (argumento.equals("on")) {
                    // Activar modo privado
                    if (privadoActivado) {
                        player.sendMessage(ChatColor.YELLOW + "El modo privado ya está activado.");
                    } else {
                        privadoActivado = true;
                        mundoPrivado = player.getWorld().getName(); // Guardar el mundo donde se ejecutó el comando
                        player.sendMessage(ChatColor.GREEN + "Modo privado activado en el mundo " + mundoPrivado + ".");
                    }
                } else if (argumento.equals("off")) {
                    // Desactivar modo privado
                    if (!privadoActivado) {
                        player.sendMessage(ChatColor.YELLOW + "El modo privado ya está desactivado.");
                    } else {
                        privadoActivado = false;
                        mundoPrivado = ""; // Limpiar el mundo al desactivar el modo privado
                        player.sendMessage(ChatColor.RED + "Modo privado desactivado.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Uso incorrecto. Usa /privado on o /privado off.");
                }
    
                return true;
            }
            return false;
        }
}