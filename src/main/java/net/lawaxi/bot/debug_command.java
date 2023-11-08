package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.bot.models.Subscribe;
import net.lawaxi.bot.models.Time;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Contact;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.lawaxi.bot.Archaeologist.*;

public class debug_command extends JCompositeCommand {

    public debug_command() {
        super(Archaeologist.INSTANCE, "arch");
    }

    @SubCommand({"sub"})
    public void sub(CommandSender sender, long group, String name, int year) {
        Subscribe sub = new Subscribe(name, DateTime.now().year() - year);
        executeDebugLog(sub.name + "-" + sub.year);
        if (net.lawaxi.bot.listener.download(sub)) {
            sender.sendMessage("关注成功");
            config.addSubscribe(group, sub);
        } else {
            sender.sendMessage("关注失败");
        }
    }

    @SubCommand({"send"})
    public void send(CommandSender sender, Long group, String name, int index) {
        Subscribe sub = config.getFirstSubscribe(group, name);
        if (sub == null) {
            sender.sendMessage("未关注");
            return;
        }

        DateTime start = DateTime.now();
        start.setYear(start.getYear() - sub.year);

        if (index == 0) {
            start.setMinutes(0);
            start.setSeconds(0);

            List<JSONObject> l = snhey.getCurrent(sub.name, start);
            for (int i = l.size() - 1; i >= 0; i--) {
                try {
                    sender.sendMessage(snhey.getMessage(l.get(i), (Contact) sender));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Time time = Time.current(start);
            String source = config.loadSource(name, "" + time);
            if (source.equals("")) {
                snhey.download(name, "" + time, false);
                source = config.loadSource(name, "" + time);
            }

            JSONObject s = JSONUtil.parseObj(source);

            if (s.keySet().size() >= index) {
                List<String> keys = s.keySet()
                        .stream()
                        .sorted((a, b) -> Long.compare(Long.valueOf(b), Long.valueOf(a)))
                        .collect(Collectors.toList());

                try {
                    sender.sendMessage(snhey.getMessage(s.getJSONObject(keys.get(index - 1)), (Contact) sender));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage("超过上限");
            }
        }
    }


    @SubCommand({"download"})
    public void download(CommandSender sender, String name, int from, int to, boolean original, boolean save_img) {
        File source = new File(new File(config.sourceFolder, name), "img");

        Time t = new Time(from, 1, 1);
        while (t.year <= to) {
            //文字
            JSONObject s = snhey.download(name, "" + t, original);

            if (save_img) {
                //图片
                for (String d : s.keySet()) {
                    JSONObject w = s.getJSONObject(d);
                    Matcher b = Pattern.compile("(?<=%img=|%link=).*?(?=%)").matcher(w.getStr("content"));

                    int count = 1;
                    while (b.find()) {
                        String pic = b.group();
                        if (pic.startsWith("http://t.cn/")) {
                            String p = HttpRequest.get(pic).execute().body();
                            if (p.indexOf("<A HREF=\"") == -1 || p.indexOf("\">here") == -1)
                                continue;

                            String loc = p.substring(p.indexOf("<A HREF=\"") + "<A HREF=\"".length(), p.indexOf("\">here"));

                            if (loc.substring(6).indexOf(":") != -1) {
                            /*
                            String p1 = weibo.setCookie(HttpRequest.get(p.substring(p.indexOf("<A HREF=\"") + "<A HREF=\"".length(), p.indexOf("\">here")))
                                    .header("Host","photo.weibo.com")).execute().body();
                            executeDebugLog(p1);
                            p1 = p1.substring(p1.indexOf("src=\"")+"src=\"".length());
                            pic = p1.substring(0,p1.indexOf("\">"));*/

                                FileUtil.writeString(loc, new File(new File(source, t.getYearAndMonth()), d + "-" + count + ".txt"), StandardCharsets.UTF_8);
                                count++;
                                continue;

                            } else {
                                continue;
                            }
                        }

                        executeDebugLog(pic);
                        String suffix = pic.substring(pic.lastIndexOf("."));
                        File dest = new File(new File(source, t.getYearAndMonth()), d + "-" + count + suffix);

                        try {
                            InputStream stream = HttpRequest.get(pic).setReadTimeout(20000).header("Referer", "https://weibo.com/").execute().bodyStream();
                            BufferedInputStream bufferedInput = new BufferedInputStream(stream);
                            byte[] result = new byte[bufferedInput.available()];
                            bufferedInput.read(result, 0, result.length);
                            FileUtil.writeBytes(result, dest); //有自动判断存在的功能

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        count++;
                    }
                }
            }
            t = t.next();
        }
    }

}
