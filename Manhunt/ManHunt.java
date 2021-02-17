package intelli.manhunt;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ManHunt extends JavaPlugin implements Listener, CommandExecutor{
	
	ChatColor yellow = ChatColor.YELLOW;
	ChatColor green = ChatColor.GREEN;
	ChatColor red = ChatColor.RED;
	
	public Player hunted;
	public boolean started = false;
	public Location lodestone;
	public BukkitTask autoUpdater;
	public HashMap<Environment, Location> lastloc = new HashMap<Environment, Location>();
	
	//By @Intelli -> https://github.com/IntelliDigitalOPS
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		System.out.println(yellow+"Loaded ManHunt by @Intelli");
	}
	@Override
	public void onDisable() {
		
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!cmd.getName().equalsIgnoreCase("hunt"))return false;
		if(args.length!=1) {
			sender.sendMessage(red+"Invalid usage use /hunt [PlayerName/stop]");
			return false;
		}
		if(args[0].equalsIgnoreCase("stop")) {
			if(!started) {
				sender.sendMessage(red+"ManHunt is not started xD!");
				return false;
			}
			started = false;
			autoUpdater.cancel();
			autoUpdater = null;
			Bukkit.broadcastMessage(ChatColor.BOLD+""+red+"Cancelled ManHunt");
			return true;
		}
		if(started) {
			sender.sendMessage(red+"ManHunt is already started xD!");
			return false;
		}
		if(Bukkit.getPlayer(args[0])==null) {
			sender.sendMessage(red+"That player does not exist");
			return false;
		}
		if(!worldAndEnvChecks()) {
			sender.sendMessage(red+"All players must be in the same Overworld dimension to start Manhunt");
			return false;
		}
		hunted = Bukkit.getPlayer(args[0]);
		started = true;
		startAuto();
		for(Player p:Bukkit.getOnlinePlayers()) {
			p.getInventory().clear();
			if(p==hunted)continue;
			giveCompass(p);
		}
		Bukkit.broadcastMessage(ChatColor.BOLD+""+green+"Started ManHunt good luck!");
		return false;
	}
	private boolean worldAndEnvChecks() {
		World common = null;
		for(Player p:Bukkit.getOnlinePlayers()) {
			if(common==null)
				common=p.getWorld();
			if(p.getWorld().getEnvironment()!=Environment.NORMAL)return false;
			if(p.getWorld()!=common)return false;
		}
		return true;
	}
	private void startAuto() {
		autoUpdater = new BukkitRunnable() {
			@Override
			public void run() {
				for(Player p:Bukkit.getOnlinePlayers()) {
					if(p==hunted)continue;
					setTarget(p);
				}
			}
		}.runTaskTimer(this, 0L, 20L);
	}
	private void giveCompass(Player p) {
		p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
	}
	private void setLore(ItemStack i, String lore) {
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(lore);
		i.setItemMeta(meta);
	}
	private ItemStack getCompass(Player p) {
		ItemStack compass = null;
		for(ItemStack i:p.getInventory().getContents()) {
			if(!i.getType().name().toLowerCase().contains("compass"))continue;
			compass = i;
			break;
		}
		return compass;
	}
	private void netherTracker(Location loc, Player p) {
		if(lodestone!=null)
			lodestone.getBlock().setType(Material.AIR);
		loc.setY(200);
		loc.getBlock().setType(Material.LODESTONE);
		ItemStack i = getCompass(p);
		CompassMeta c = (CompassMeta) i.getItemMeta();
		c.setLodestone(loc);
		c.setLodestoneTracked(true);
		i.setItemMeta(c);
		this.lodestone=loc.clone();
	}
	private void removeNether(Player p) {
		for (ItemStack i:p.getInventory().getContents()) {
			if(!i.getType().name().toLowerCase().contains("compass"))continue;
			p.getInventory().remove(i);
			break;
		}
		giveCompass(p);
	}
	private void fireworks(Player p) {
		new BukkitRunnable() {
			int i=0;
			@Override
			public void run() {
				i++;
				p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
				if(i==5)this.cancel();
			}
		}.runTaskTimer(this, 0L, 20L);
	}
	private void setTarget(Player p) {
		if(p.getWorld().getEnvironment()!=hunted.getWorld().getEnvironment()) {
			if(p.getWorld().getEnvironment()==Environment.NORMAL) {
				if(((CompassMeta)getCompass(p).getItemMeta()).isLodestoneTracked())
					removeNether(p);
				p.setCompassTarget(lastloc.get(p.getWorld().getEnvironment()));
				setLore(getCompass(p), ChatColor.LIGHT_PURPLE+"Pointing to "+hunted.getName()+"'s last location in your world");
				return;
			}
			if(!lastloc.containsKey(p.getWorld().getEnvironment())) {
				setLore(getCompass(p),ChatColor.LIGHT_PURPLE+"Speedrunner has not entered your dimension!");
				return;
			}
			netherTracker(lastloc.get(p.getWorld().getEnvironment()), p);
			setLore(getCompass(p), ChatColor.LIGHT_PURPLE+"Pointing to "+hunted.getName()+"'s last location in your world");
			return;
		}
		if(p.getWorld().getEnvironment()==Environment.NORMAL) {
			if(((CompassMeta)getCompass(p).getItemMeta()).isLodestoneTracked())
				removeNether(p);
			p.setCompassTarget(hunted.getLocation());
			setLore(getCompass(p), ChatColor.LIGHT_PURPLE+"Pointing to "+hunted.getName()+"'s current location");
			return;
		}
		netherTracker(hunted.getLocation(), p);
		setLore(getCompass(p), ChatColor.LIGHT_PURPLE+"Pointing to "+hunted.getName()+"'s current location (Nether Tracking)");
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent pde) {
		if(!started)return;
		if(((Player)pde.getEntity())==hunted) {
			started = false;
			autoUpdater.cancel();
			autoUpdater = null;
			Bukkit.broadcastMessage(green+"The hunters have won");
			for(Player p:Bukkit.getOnlinePlayers()) {
				if(p==hunted)continue;
				p.sendTitle(green+"The hunters have won", "The speedrunner was killed", 20, 40, 20);
				fireworks(p);
			}
			hunted = null;
			return;
		}
		for(ItemStack i:pde.getDrops())
			if(i.getType()==Material.COMPASS)
				i.setAmount(0);
	}
	@EventHandler
	public void onDrop(PlayerDropItemEvent pdie) {
		if(!started)return;
		if(pdie.getItemDrop().getItemStack().getType().name().toLowerCase().contains("compass")) {
			pdie.setCancelled(true);
			pdie.getPlayer().sendMessage(red+"You cannnot drop that");
		}
	}
	@EventHandler
	public void onPortal(PlayerPortalEvent ppe) {
		if(!started)return;
		if(ppe.getPlayer()!=hunted)return;
		if(lastloc.containsKey(ppe.getFrom().getWorld().getEnvironment())) {
			lastloc.replace(ppe.getFrom().getWorld().getEnvironment(), ppe.getFrom());
			return;
		}
		lastloc.put(ppe.getFrom().getWorld().getEnvironment(), ppe.getFrom());
	}
	@EventHandler
	public void onRespawn(PlayerRespawnEvent pre) {
		if(!started)return;
		giveCompass(pre.getPlayer());
	}
}
