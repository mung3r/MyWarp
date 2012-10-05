package me.taylorkelly.mywarp.data;

public class WarpLimit implements Comparable<WarpLimit> {

    public String name;
    public int maxTotal;
    public int maxPublic;
    public int maxPrivate;

    public WarpLimit(String name, int maxTotal, int maxPublic, int maxPrivate) {
        this.name = name;
        this.maxTotal = maxTotal;
        this.maxPublic = maxPublic;
        this.maxPrivate = maxPrivate;
    }

    @Override
    public int compareTo(WarpLimit l) {
        return name.compareTo(l.name);
    }
}