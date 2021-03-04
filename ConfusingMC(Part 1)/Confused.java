package intelli.confusingmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

public class Confused extends JavaPlugin implements Listener, CommandExecutor, TabCompleter{
	
	ChatColor green = ChatColor.GREEN;
	ChatColor red = ChatColor.RED;
	ChatColor yellow = ChatColor.YELLOW;
	
	public boolean started = false;
	public HashMap<Player, BlockFace> lastface = new HashMap<Player, BlockFace>();
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		this.getCommand("confusing").setTabCompleter(this);
		System.out.println(green+"Started COnfusingMC by @Intelli");
	}
	@Override
	public void onDisable() {
		System.out.println(red+"Stopped COnfusingMC by @Intelli");
	}
	private static final String[] COMMANDS = {"start", "stop"};//Change to match args
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		List<String> completions = new ArrayList<>();
		StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        Collections.sort(completions);
        return completions;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Server server = Bukkit.getServer();
		if(!cmd.getName().equalsIgnoreCase("confusing"))return false;
		if(args.length!=1) {
			invalid(sender);
			return false;
		}
		switch(args[0]) {
		case "start":
			if(started) {
				sender.sendMessage(ChatColor.RED+"Already started xD !");
				return false;
			}
			started = true;
			server.broadcastMessage(ChatColor.GREEN+"Started COnfusingMC by @Intelli");
			break;
		case "stop":
			if(!started) {
				sender.sendMessage(ChatColor.RED+"Not already started xD !");
				return false;
			}
			started = false;
			server.broadcastMessage(ChatColor.RED+"Stopped COnfusingMC by @Intelli");
			break;
		default:
			invalid(sender);
			return false;
		}
		return true;
	}
	private void invalid(CommandSender s) {
		s.sendMessage(red+"Invalid Usage use /confusing [start/stop]");
	}
	@EventHandler
	public void faceGetter(PlayerInteractEvent pie) {
		if(!started)return;
		if(pie.getAction()!=Action.LEFT_CLICK_BLOCK)return;
		if(!lastface.containsKey(pie.getPlayer()))
			lastface.put(pie.getPlayer(), pie.getBlockFace());
		else
			lastface.replace(pie.getPlayer(), pie.getBlockFace());
	}
	@EventHandler
	public void onPlace(BlockPlaceEvent bp) {
		if(!started)return;
		bp.getBlockAgainst().breakNaturally();
		bp.getBlock().setType(Material.AIR);
	}
	@EventHandler
	public void onBreak(BlockBreakEvent bb) {
		if(!started)return;
		ItemStack stack = bb.getPlayer().getInventory().getItemInOffHand();
		if(stack.getType()==null)bb.setCancelled(true);
		if(stack.getType()==Material.AIR)bb.setCancelled(true);
		if(!stack.getType().isBlock())bb.setCancelled(true);
		bb.setCancelled(true);
		bb.getBlock().getRelative(lastface.get(bb.getPlayer())).setType(stack.getType());
		stack.setAmount(stack.getAmount()-1);
	}
}
