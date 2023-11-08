package net.lawaxi.bot.helper;

import cn.hutool.http.HttpRequest;
import net.lawaxi.Shitboy;
import net.lawaxi.bot.Archaeologist;

public class WeiboLoginHelper {

    private static final String APITID = "https://passport.weibo.com/visitor/genvisitor?cb=gen_callback&fp={\"os\":\"1\",\"browser\":\"Chrome70,0,3538,25\",\"fonts\":\"undefined\",\"screenInfo\":\"1920*1080*24\",\"plugins\":\"\"}";
    private static final String APISUB = "https://passport.weibo.com/visitor/visitor?a=incarnate&t=%s&w=%d&c&cb=restore_back&from=weibo";
    private static String cookie = "";
    private final boolean hasShitboy;

    public WeiboLoginHelper(boolean hasShitboy) {
        this.hasShitboy = hasShitboy;
    }

    public void updateLoginToSuccess() {
        try {
            updateLogin();
        } catch (Exception e) {
            updateLoginToSuccess();
        }
    }

    public void updateLogin() throws Exception {
        String a = getWithDefaultHeader(APITID);

        String tid = a.substring(a.indexOf("\"tid\":\"") + "\"tid\":\"".length(), a.indexOf("\",\"new_tid\""));
        boolean isNew = Boolean.parseBoolean(a.substring(a.indexOf("\"new_tid\":") + "\"new_tid\":".length(), a.indexOf("}})")));

        String b = getWithDefaultHeader(String.format(APISUB, tid, isNew ? 3 : 2));

        if (b.contains("\"msg\":\"succ\"") && !b.contains("null")) { //tid不合法时需要重新申请
            String sub = b.substring(b.indexOf("\"sub\":\"") + "\"sub\":\"".length(), b.indexOf("\",\"subp\":\""));
            String subp = b.substring(b.indexOf("\",\"subp\":\"") + "\",\"subp\":\"".length(), b.indexOf("\"}})"));
            cookie = "SUB=" + sub + "; SUBP=" + subp;

        } else {
            throw new Exception();
        }
    }

    public HttpRequest setDefaultHeader(HttpRequest request) {
        return request.header("authority", "weibo.com").header(
                "sec-ch-ua", "\"Chromium\";v=\"94\", \"Google Chrome\";v=\"94\", \";Not A Brand\";v=\"99\"").header(
                "content-type", "application/x-www-form-urlencoded").header(
                "x-requested-with", "XMLHttpRequest").header(
                "sec-ch-ua-mobile", "?0").header(
                "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML).header( like Gecko) Chrome/94.0.4606.71 Safari/537.36").header(
                "sec-ch-ua-platform", "\"Windows\"").header(
                "accept", "*/*").header(
                "sec-fetch-site", "same-origin").header(
                "sec-fetch-mode", "cors").header(
                "sec-fetch-dest", "empty");
    }

    protected String getWithDefaultHeader(String url) {
        return setDefaultHeader(HttpRequest.get(url))
                .execute().body();
    }

    protected HttpRequest setHeader(HttpRequest request) {
        if (hasShitboy) {
            if (!Shitboy.INSTANCE.getProperties().save_login)
                return Shitboy.INSTANCE.handlerWeibo.setHeader_Public(request);
        }
        return setDefaultHeader(request).header("cookie", cookie);
    }


    public HttpRequest setCookie(HttpRequest request) {
        if (cookie.equals("")) {
            try {
                updateLoginToSuccess();
                Archaeologist.INSTANCE.getLogger().info("successfully update weibo cookie");

            } catch (Exception e) {
                Archaeologist.INSTANCE.getLogger().info("failed update weibo cookie");
                e.printStackTrace();
                return null;
            }
        }

        return setHeader(request);
    }
}
