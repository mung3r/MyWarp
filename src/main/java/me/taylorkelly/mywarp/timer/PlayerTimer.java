package me.taylorkelly.mywarp.timer;

import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class PlayerTimer extends BukkitRunnable {

    protected final Player player;
    private final Time durration;
    private final Long startTime;

    public PlayerTimer(MyWarp plugin, Player player, Time durration) {
        this.player = player;
        this.durration = durration;
        this.startTime = System.currentTimeMillis();
        
        //run the task right on creation
        runTaskLater(plugin, durration.getTicks());
        getTimerMap().put(player.getName(), this);
    }

    public abstract Map<String, PlayerTimer> getTimerMap();

    @Override
    public void run() {
        getTimerMap().remove(player.getName());
    }
    
    @Override
    public void cancel() {
        super.cancel();
        getTimerMap().remove(player.getName());
    }

    public long getStartTime() {
        return startTime;
    }

    public Time getDurration() {
        return durration;
    }

    public Integer getRemainingTime() {
        int time = (int) (durration.getTicks() - (startTime - System
                .currentTimeMillis()));
        return durration.getInt() - (time / 1000);
    }
}
