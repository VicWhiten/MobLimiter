package com.bukkit.vicwhiten.moblimiter;

import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;



public class MobLimiter extends JavaPlugin
{

	private final MobLimiterEntityListener entityListener = new MobLimiterEntityListener(this);
	public int mobMax;
	public Configuration config;

	public void onDisable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName()+" version "+pdfFile.getVersion()+" is disabled!");
	}

	public void onEnable()
	{

		PluginManager pm = getServer().getPluginManager();
		config = this.getConfiguration();

		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.entityListener, Event.Priority.Normal, this);
		getCommand("mobmax").setExecutor(new SetMaxCommand(this));
		
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
	        System.out.println("Setting up /invademin Command");
	    }

	    public boolean onCommand(CommandSender sender, 
	    		Command command, 
	    		String label, String[] args) 
	    {
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
}