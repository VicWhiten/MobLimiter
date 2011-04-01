package com.bukkit.vicwhiten.moblimiter;

import java.util.Arrays;
import java.util.List;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;



public class MobLimiter extends JavaPlugin
{

	private final MobLimiterEntityListener entityListener = new MobLimiterEntityListener(this);
	public List<String> mobBlacklist;
	public int mobMax;
	public Configuration config;
	public GroupManager gm;
	public Permissions perm;

	public void onDisable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName()+" version "+pdfFile.getVersion()+" is disabled!");
	}

	public void onEnable()
	{

		PluginManager pm = getServer().getPluginManager();
		
		 Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
	        if (p != null) {
	            if (!this.getServer().getPluginManager().isPluginEnabled(p)) {
	                this.getServer().getPluginManager().enablePlugin(p);
	            }
	            gm = (GroupManager) p;
	        } 
	        
	        p = this.getServer().getPluginManager().getPlugin("Permissions");
	        if (p != null) {
	            if (!this.getServer().getPluginManager().isPluginEnabled(p)) {
	                this.getServer().getPluginManager().enablePlugin(p);
	            }
	            perm = (Permissions) p;
	        } 
	 
		config = this.getConfiguration();
		setupMobMax();
		setupBlacklist();
		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.entityListener, Event.Priority.Normal, this);
		getCommand("moblimiter").setExecutor(new MobLimiterCommand(this));
		
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName()+" version "+pdfFile.getVersion()+" is enabled!");
	}
	
	public int getMobAmount(World world)
	{
		List<LivingEntity> mobs = world.getLivingEntities();
		for(int j=0; j<mobs.size(); j++)
		{
			if(!Creature.class.isInstance(mobs.get(j)))
			{
				mobs.remove(j);
				j--;
			}
		}
		return mobs.size();
	}
	
	public void purgeMobs(World world)
	{
		List<LivingEntity> mobs = world.getLivingEntities();
		for(int j=0; j<mobs.size(); j++)
		{
			if(Creature.class.isInstance(mobs.get(j)))
			{
				LivingEntity mob = mobs.remove(j);
				mob.remove();
				j--;
			}
		}
	}
	
	public void setupMobMax()
	{
		config.load();
		mobMax = config.getInt("mob-max", -1);
	}
	
	public void setupBlacklist()
	{
		config.load();
		String blacklist = config.getString("mob-blacklist");
		mobBlacklist = Arrays.asList(blacklist.split(" "));
		for(String mob : mobBlacklist)
		{
			mob = mob.toLowerCase();
		}
	}
	
	public void addBlackList(String type)
	{
		config.load();
		String blacklist = config.getString("mob-blacklist");
		blacklist = blacklist + " " + type.toLowerCase();
		config.setProperty("mob-blacklist",	blacklist);
		mobBlacklist.add(type.toLowerCase());
	}
	
	public boolean removeBlackList(String type)
	{
		config.load();
		boolean wasThere = mobBlacklist.remove(type.toLowerCase());
		if(!wasThere)
		{
			return false;
		}
		String blacklist = "";
		for(String mob : mobBlacklist)
		{
			blacklist = blacklist + " " + mob;
		}
		config.setProperty("mob-blacklist", blacklist.trim());
		return true;
	}
	
	public void setMobMax(int newMax)
	{
		mobMax = newMax;
		config.setProperty("mob-max", newMax);
		config.save();
	}
	
	public int getMobMax()
	{
		return mobMax;
	}
	
    public boolean checkPermission(Player player, String permission)
    {
    	if(player.isOp())
    	{
    		return true;
    	}else if(gm != null)
    	{
        return gm.getWorldsHolder().getWorldPermissions(player).has(player,permission);
    	}else if(perm != null)
    	{
    	return perm.getHandler().has(player, permission);
    	}else return false;
    }
	
	private class MobLimiterEntityListener extends EntityListener {

		private MobLimiter plugin;
		public MobLimiterEntityListener(MobLimiter plug) {
			plugin = plug;
		}
		
		public void onCreatureSpawn(CreatureSpawnEvent event)
		{	
			if(plugin.mobMax > -1 && plugin.getMobAmount(event.getEntity().getWorld()) >= plugin.mobMax)
			{
				
				event.setCancelled(true);
			}
			if(plugin.mobBlacklist.contains(event.getCreatureType().getName().toLowerCase()))
			{
				event.setCancelled(true);
			}
		}
	}
	

	
	private class MobLimiterCommand implements CommandExecutor 
	{
	    private final MobLimiter plugin;

	    public MobLimiterCommand(MobLimiter plugin) {
	        this.plugin = plugin;
	    }

	    public boolean onCommand(CommandSender sender, 
	    		Command command, 
	    		String label, String[] args) 
	    {
	    	boolean permission = false;
	    	try{
	    		permission = checkPermission((Player)sender, "moblimiter.sexMax");
	    	}catch(Exception E)
	    	{
	    		permission = true;
	    	}
	    	if(!permission)
	    	{
	    		sender.sendMessage(ChatColor.RED + "You do not have the permissions to do this");
	    		return true;
	    	}
	    	if(args.length < 1)
	    	{
	    		return false;
	    	}
	    	//setMax command
	    	if(args.length == 2 && args[0].compareTo("setmdax") == 0)
	    	{
	    		try{
	    			int newMax = Integer.parseInt(args[0]);
	    			if(newMax >=0)
	    			{
	    				plugin.setMobMax(newMax);
	    				sender.sendMessage("MobMax set to " + newMax);
	    				return true;
	    			}else return false;
	    		}catch(Exception e){
	    			return false;
	    		}
	    	}
	    	//purge command
	    	if(args.length == 1 && args[0].compareTo("purge") == 0)
	    	{
	    		try{
	    		Player p = (Player)sender;
		    	plugin.purgeMobs(p.getWorld());
		    	sender.sendMessage("All mobs purged.");
		    	return true;
		    	}catch(Exception E){
		    		sender.sendMessage("Must be run ingame!");
		    		return true;
		    	}
	    	}
	    	//add to blacklist command
	    	if(args.length == 2 && args[0].compareTo("addblacklist") == 0)
	    	{
	    		addBlackList(args[1]);
	    		sender.sendMessage("Added " + args[1] + " to Blacklist");
	    		return true;
	    	}
	    	if(args.length == 2 && args[0].compareTo("removeblacklist") == 0)
	    	{
	    		boolean didWork = removeBlackList(args[1]);
	    		if(didWork)
	    		{
	    			sender.sendMessage("Added " + args[1] + " to Blacklist");
	    			return true;
	    		}else{
	    			sender.sendMessage("Type not found in Blacklist");
	    			return true;
	    		}
	    	}
	    	return false;
	    }	
	}
	
}