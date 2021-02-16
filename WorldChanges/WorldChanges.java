package intelli.worldchanges;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WorldChanges extends JavaPlugin implements CommandExecutor, TabCompleter{
	
	Random rand = new Random();
	World cnormal = null;
	World cnether = null;
	World cend = null;
	boolean started = false;
	BukkitTask timings = null;
	
	@Override
	public void onEnable() {
		rand = new Random(Bukkit.getWorlds().get(0).getSeed());
		this.getCommand("wc").setTabCompleter(this);
		System.out.println(ChatColor.YELLOW+"Loaded WorldChanges by @Intelli");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!cmd.getName().equalsIgnoreCase("wc"))return false;
		if(args.length!=1) {
			sender.sendMessage(ChatColor.RED+"Invalid usage use /cw [start/stop]");
			return false;
		}
		if(args[0].equalsIgnoreCase("start")) {
			if(started) {
				sender.sendMessage(ChatColor.RED+"Already started");
				return false;
			}
			timingsManager();
			started = true;
			Bukkit.broadcastMessage(ChatColor.GREEN+"Started WorldChanges swapping in 5 minutes");
		}else if(args[0].equalsIgnoreCase("stop")) {
			if(!started) {
				sender.sendMessage(ChatColor.RED+"Not already started");
				return false;
			}
			timings.cancel();
			timings = null;
			started = false;
			Bukkit.broadcastMessage(ChatColor.GREEN+"Stopped WorldChanges!");
		}else {
			
		}
		return false;
	}
	private void timingsManager() {
		timings = new BukkitRunnable() {
			int i = 0;
			@Override
			public void run() {
				i++;
				if(i==280)//Change this to amount of seconds -20
					generateNew();
				else if(i==300) {//Change this to amount of seconds
					changeWorld();
					i = 0;
				}
			}
		}.runTaskTimer(this, 0L, 20);
	}
	private String randomStringGen(int length) {
		String candidates = 
				"BYINTELLIACDFGHJKMOPQRSUVWXZ1234567890abcdefghijklmnopqrstuvwxyz1234567890";
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<length;i++)
			builder.append(candidates.charAt(rand.nextInt(candidates.length())));
		return builder.toString();
	}
	private World newWorld(Environment env) {
		WorldCreator wc = new WorldCreator(randomStringGen(5));
		wc.generateStructures(true);
		wc.type(WorldType.NORMAL);
		wc.environment(env);
		return Bukkit.createWorld(wc);
	}
	private World worldFromEnv(Environment env) {
		switch(env) {
		case NETHER:return cnether;
		case NORMAL:return cnormal;
		case THE_END:return cend;
		default: return null; //This will never happen
		}
	}
	private void generateNew() {
		cnormal = newWorld(Environment.NORMAL);
		cnether = newWorld(Environment.NETHER);
		cend = newWorld(Environment.END);
		System.out.println(ChatColor.YELLOW+"Generated 3 new worlds");
	}
	private void changeWorld() {
		Bukkit.broadcastMessage(ChatColor.YELLOW+"Swapping...");
		for(Player p:Bukkit.getOnlinePlayers()) {
			World current = p.getWorld();
			p.teleport(new Location(
					worldFromEnv(current.getEnvironment()),
					p.getLocation().getX(),
					p.getLocation().getY(),
					p.getLocation().getZ()
					));
		}
	}
	
	@EventHandler
	public void onPortal(PlayerPortalEvent ppe) {
		ppe.setCanCreatePortal(true);
		ppe.setTo(new Location(
					worldFromEnv(ppe.getTo().getWorld().getEnvironment()),
					ppe.getTo().getX(),
					ppe.getTo().getY(),
					ppe.getTo().getZ()
					));
	}
}
