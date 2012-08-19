package me.taylorkelly.mywarp.data;

public class WarpLimit {

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