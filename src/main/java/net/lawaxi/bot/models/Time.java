package net.lawaxi.bot.models;

import cn.hutool.core.date.DateTime;

public class Time {
    public int year;
    public int month;
    public int date;

    public Time(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.date = date;
    }

    @Override
    public String toString() {
        return year + "-" + (month > 9 ? "" : "0") + month + "-" + (date > 9 ? "" : "0") + date;
    }

    public String getYearAndMonth() {
        return year + "-" + (month > 9 ? "" : "0") + month;
    }

    public Time next() {
        switch (date) {
            case 1:
            case 11:
                return new Time(year, month, date + 10);
            default: {
                if (month == 12) {
                    return new Time(year + 1, 1, 1);
                }
                return new Time(year, month + 1, 1);
            }
        }
    }

    public static Time fromStart() {
        return new Time(2014, 7, 11);
    }

    public static Time current(DateTime now) {
        int date = now.getDate();
        if (date < 11)
            return new Time(now.year(), now.month() + 1, 1);
        else if (date < 21)
            return new Time(now.year(), now.month() + 1, 11);
        else
            return new Time(now.year(), now.month() + 1, 21);
    }

    public static Time current(long now) {
        return current(DateTime.of(now));
    }
}
