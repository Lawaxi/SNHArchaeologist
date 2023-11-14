package net.lawaxi.bot.models;

import cn.hutool.json.JSONObject;

public class MessageWithID {
    public final long time;
    public final String star;
    public final JSONObject message;

    public MessageWithID(long time, String star, JSONObject message) {
        this.time = time;
        this.star = star;
        this.message = message;
    }
}
