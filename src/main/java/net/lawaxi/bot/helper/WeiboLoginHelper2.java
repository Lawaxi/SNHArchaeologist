package net.lawaxi.bot.helper;

import cn.hutool.http.HttpRequest;

public class WeiboLoginHelper2 {
    private static final String cookie = "";

    public String getPicSrc(String url) {
        String body = HttpRequest.get(url).header("Cookie", cookie).execute().body();
        if (body.indexOf("img") != -1) {
            body = body.substring(body.indexOf("src=\"") + "src=\"".length());
            return body.substring(0, body.indexOf("\">"));
        }
        return null;
    }
}
