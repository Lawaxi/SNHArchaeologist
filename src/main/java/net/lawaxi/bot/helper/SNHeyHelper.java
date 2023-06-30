package net.lawaxi.bot.helper;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.bot.models.Time;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.lawaxi.bot.Archaeologist.config;
import static net.lawaxi.bot.Archaeologist.executeDebugLog;

public class SNHeyHelper {
    public static final String API = "http://13.94.46.135/Home/Profiles/%s?weiboPage=%d&timeRange=%s";

    public void download(String name, String time) {
        String s = get(String.format(API, name, 1, time));
        s = s.substring(s.indexOf("<b><span>1</span></b>") + "<b><span>1</span></b>".length());


        List<WeiboContent> contents = new ArrayList<>();
        //第一页
        read(name, contents, s.substring(s.indexOf("<fieldset>"), s.indexOf("</fieldset>")));

        //第n页
        s = s.substring(0, s.indexOf("</div>"));
        int count = 2;
        while (s.indexOf("[") != -1) {
            String s1 = get(String.format(API, name, count, time));
            read(name, contents, s1.substring(s1.indexOf("<fieldset>"), s1.indexOf("</fieldset>")));
            s = s.substring(s.indexOf("</a>") + "</a>".length());
            count++;
        }

        JSONObject o = new JSONObject();
        for (WeiboContent content : contents) {
            o.set("" + content.time, content.json);
        }

        config.storeSource(name, time, o.toStringPretty());
    }

    private void read(String name, List<WeiboContent> contents, String p) {
        String[] ps = p.split("<div class=\"status\">");
        for (String p0 : ps) {
            if (p0.indexOf("</div>") == -1) {
                continue;
            }

            JSONObject object = new JSONObject();
            p0 = p0.substring(p0.indexOf("<span class=\"name\">") + "<span class=\"name\">".length());
            String n = p0.substring(0, p0.indexOf("</span>"));
            object.set("name", n);
            object.set("original", n.equals(name));
            p0 = p0.substring(p0.indexOf("</wb:follow-button>") + "</wb:follow-button>".length());

            //正文与转发
            String c = p0.substring(0, p0.indexOf("<div><span class=\"name\">"));

            if (p0.indexOf("repost") != -1) {
                String r = c.substring(c.indexOf("<div class=\"repost\">"));
                c = c.substring(0, c.indexOf("<div class=\"repost\">"));

                JSONObject repost = new JSONObject();
                r = r.substring(r.indexOf("<span>") + "<span>".length());
                repost.set("name", r.substring(0, r.indexOf("</span>：")));
                r = r.substring(r.indexOf("</span>：") + "</span>：".length(), r.indexOf("</div>"));
                repost.set("content", getContent(r.replace("</p>", "")));
                object.set("repost", repost);
            }

            object.set("content", getContent(c.replace("</p>", "")));

            //时间
            p0 = p0.substring(p0.indexOf("<div><span class=\"name\">") + "<div><span class=\"name\">".length());
            long time = DateUtil.parse(p0.substring(0, p0.indexOf("</span>")), "yyyy年MM月dd日HH时mm分").getTime();

            //回复
            p0 = p0.substring(p0.indexOf("<div>") + "<div>".length());
            p0 = p0.substring(0, p0.indexOf("</div>"));
            JSONArray replies = new JSONArray();
            while (p0.contains("<span class=\"\">")) {
                p0 = p0.substring(p0.indexOf("<span class=\"\">") + "<span class=\"\">".length());
                JSONObject reply = new JSONObject();
                reply.set("name", p0.substring(0, p0.indexOf("</span></a>")));
                p0 = p0.substring(p0.indexOf("：") + "：".length());
                reply.set("content", getContent(p0.substring(0, p0.indexOf("</span>"))));
                p0 = p0.substring(p0.indexOf("\"name\">(") + "\"name\">(".length());
                reply.set("time", DateUtil.parse(p0.substring(0, p0.indexOf(")")), "yyyy年MM月dd日HH时mm分").getTime());
                replies.add(reply);
            }

            if (replies.size() > 0)
                object.set("reply", replies);
            contents.add(new WeiboContent(time, object));
        }

    }

    private String getContent(String pre_content) {
        if (pre_content.endsWith(" "))
            pre_content = pre_content.substring(0, pre_content.length() - 1);

        return matchEmoji(
                matchImg2(
                        matchImg(
                                pre_content
                                        .replace("\r\n                                        ", "")
                                        .replace("\r\n                                       ", "")
                                        .replace("        ", "")
                        )
                )
        );
    }

    private String matchImg(String pre_content) {
        String[] a = pre_content.split("<a href=\".*?</a>", -1);
        if (a.length < 2)
            return pre_content;

        String out = a[0];
        int count = 1;
        Matcher b = Pattern.compile("(?<=<a href=\").*?(?=</a>)").matcher(pre_content);
        while (b.find()) {
            String pic = b.group();
            out += "%img=" + pic.substring(0, pic.indexOf("\">")) + "%";
            out += a[count];
        }
        return out;
    }

    private String matchImg2(String pre_content) {
        String[] a = pre_content.split("http://t\\.cn/[\\w]+", -1);
        if (a.length < 2)
            return pre_content;

        String out = a[0];
        int count = 1;
        Matcher b = Pattern.compile("http://t\\.cn/[\\w]+").matcher(pre_content);
        while (b.find()) {
            out += "%link=" + b.group() + "%";
            out += a[count];
        }
        return out;

    }

    private String matchEmoji(String pre_content) {
        String[] a = pre_content.split("<img src=.*?/>", -1);
        if (a.length < 2)
            return pre_content;

        String out = a[0];
        int count = 1;
        Matcher b = Pattern.compile("<img src=.*?/>").matcher(pre_content);
        while (b.find()) {
            String pic = b.group();
            out += pic.substring(pic.indexOf("["), pic.indexOf("]") + 1);
            out += a[count];
        }
        return out;

    }

    public Message getMessage(JSONObject w, Contact contact) throws IOException {
        Message m = new PlainText("【" + w.getStr("name") + "微博更新：" + DateTime.of(w.getLong("time")).toStringDefaultTimeZone() + "】\n");
        m = m.plus(parseContent(w.getStr("content"), contact));
        if (w.containsKey("repost")) {
            JSONObject repost = w.getJSONObject("repost");
            m = m.plus("\n---------\n转发：\n" + repost.getStr("name") + "：")
                    .plus(parseContent(repost.getStr("content"), contact));

        }
        if (w.containsKey("replies")) {
            for (Object object : w.getJSONArray("replies").toArray()) {
                JSONObject reply = JSONUtil.parseObj(object);
                m = m.plus("\n+++++++++\n" + reply.getStr("name") + "：")
                        .plus(parseContent(reply.getStr("content"), contact))
                        .plus("\n(" + DateTime.of(reply.getLong("time")).toStringDefaultTimeZone() + ")");
            }
        }

        return m;
    }

    public Message parseContent(String content, Contact contact) throws IOException {
        String[] a = content.split("%img=*?%", -1);
        if (a.length < 2)
            return new PlainText(content);

        Message out = new PlainText(a[0]);
        int count = 1;
        Matcher b = Pattern.compile("(?<=%img=|%link=)*?(?=%)").matcher(content);
        while (b.find()) {
            String pic = b.group();
            if (pic.startsWith("http://t.cn/")) {
                String p = get(pic);
                if (p.indexOf("<A HREF=\"") != -1 && p.indexOf("\">here") != -1) {
                    //图片短链
                    pic = p.substring(p.indexOf("<A HREF=\"") + "<A HREF=\"".length(), p.indexOf("\">here"));

                } else {
                    //其他短链
                    out = out.plus(pic + a[count]);
                    continue;
                }
            }

            out = out.plus(contact.uploadImage(ExternalResource.create(getRes(pic))));
            out = out.plus(a[count]);
        }
        return out;
    }

    public List<JSONObject> getCurrent(String name, DateTime start) {
        Time time = Time.current(start);
        DateTime end = DateTime.of(start.getTime() + 3600 * 1000);
        executeDebugLog("开始时间：" + start.toStringDefaultTimeZone() + "，结束时间" + end.toStringDefaultTimeZone());

        String source = config.loadSource(name, "" + time);
        if (source.equals("")) {
            download(name, "" + time);
            source = config.loadSource(name, "" + time);
        }

        JSONObject s = JSONUtil.parseObj(source);
        List<JSONObject> a = new ArrayList<>();
        for (String t : s.keySet()) {
            Date date = DateTime.of(Long.valueOf(t));
            if (date.compareTo(end) == -1 && date.compareTo(start) >= 0) {
                a.add(JSONUtil.parseObj(s.get(t)).set("time", Long.valueOf(t)));
            }
        }
        return a;
    }


    protected String get(String url) {
        executeDebugLog(url);
        return HttpRequest.get(url).execute().body();
    }

    public InputStream getRes(String resLoc) {
        return HttpRequest.get(resLoc).setReadTimeout(20000).header("Referer", "https://weibo.com/").execute().bodyStream();
    }

    private class WeiboContent {
        public final long time;
        public final JSONObject json;

        private WeiboContent(long time, JSONObject json) {
            this.time = time;
            this.json = json;
        }
    }
}
