package com.bukkit.vicwhiten.moblimiter;

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



public class MobLimiter extends JavaPlugin
{

	private final MobLimiterEntityListener entityListener = new MobLimiterEntityListener(this);
	public int mobMax;
	public Configuration config;
	public GroupManager gm;

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
	 
		config = this.getConfiguration();

		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.entityListener, Event.Priority.Normal, this);
		getCommand("mobmax").setExecutor(new SetMaxCommand(this));
		getCommand("purgemobs").setExecutor(new PurgeCommand(this));
		
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
		mobMax = config.getInt("mob-max", -1);
	}
	
	public void setMobMax(int newMax)
	{
		mobMax = newMax;
		config.setProperty("mob-max", newMax);
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
		}
	}
	
	private class SetMaxCommand implements CommandExecutor 
	{
	    private final MobLimiter plugin;

	    public SetMaxCommand(MobLimiter plugin) {
	        this.plugin = plugin;
	    }

	    public boolean onCommand(CommandSender sender, 
	    		Command command, 
	    		String label, String[] args) 
	    {
	    	if(!checkPermission((Player)sender, "moblimiter.setMax"))
	    	{
	    		sender.sendMessage(ChatColor.RED + "You do not have the permissions to do this");
	    		return true;
	    	}
	    	if(args.length > 1)
	    	{
	    		return false;
	    	}
	    	if(args.length == 1)
	    	{
	    		try{
	    			int newMax = Integer.parseInt(args[0]);
	    			if(newMax >=0)
	    			{
	    				plugin.setMobMax(newMax);
	    				return true;
	    			}else return false;
	    		}catch(Exception e){
	    			return false;
	    		}
	    	}
	    	sender.sendMessage("Mob Max is set to " + plugin.mobMax);
	    	return true;
	    		
	    }	
	}
	
	private class PurgeCommand implements CommandExecutor 
	{
	    private final MobLimiter plugin;

	    public PurgeCommand(MobLimiter plugin) {
	        this.plugin = plugin;
	    }

	    public boolean onCommand(CommandSender sender, 
	    		Command command, 
	    		String label, String[] args) 
	    {
	    	if(!checkPermission((Player)sender, "moblimiter.purge"))
	    	{
	    		sender.sendMessage(ChatColor.RED + "You do not have the permissions to do this");
	    		return true;
	    	}
	    	Player p = (Player)sender;
	    	plugin.purgeMobs(p.getWorld());
	    	return true;
	    }
	    
	}
}