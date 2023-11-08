package net.lawaxi.bot.models;

public class Subscribe {

    public final String name;
    public final int year;

    public Subscribe(String name, int year) {
        this.name = name;
        this.year = year;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Subscribe) {
            return ((Subscribe) obj).name.equals(name) && ((Subscribe) obj).year == year;
        }
        return false;
    }
}
