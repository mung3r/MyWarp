package me.taylorkelly.mywarp.data;

public class WarpLimit implements Comparable<WarpLimit> {

    private String name;
    private int maxTotal;
    private int maxPublic;
    private int maxPrivate;

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

    public String getName() {
        return name;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getMaxPublic() {
        return maxPublic;
    }

    public int getMaxPrivate() {
        return maxPrivate;
    }

}