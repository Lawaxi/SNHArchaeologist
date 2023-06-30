package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.bot.models.Subscribe;
import net.lawaxi.bot.models.Time;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Contact;

import java.util.List;

import static net.lawaxi.bot.Archaeologist.*;
import static net.lawaxi.bot.listener.download;

public class debug_command extends JCompositeCommand {

    public debug_command() {
        super(Archaeologist.INSTANCE, "arch");
    }

    @SubCommand({"sub"})
    public void sub(CommandSender sender, String name, int year) {
        Subscribe sub = new Subscribe(name, DateTime.now().year() - year);
        executeDebugLog(sub.name + "-" + sub.year);
        if (download(sub)) {
            sender.sendMessage("关注成功");
            config.addSubscribe(sub);
        } else {
            sender.sendMessage("关注失败");
        }
    }

    @SubCommand({"send"})
    public void send(CommandSender sender, String name, int index) {
        Subscribe sub = config.getSubscribe(name);
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
                snhey.download(name, "" + time);
                source = config.loadSource(name, "" + time);
            }

            JSONObject s = JSONUtil.parseObj(source);

            if (s.keySet().size() >= index) {
                List<String> keys = s.keySet().stream().sorted((a, b) -> Long.valueOf(b) - Long.valueOf(a) > 0 ? 1 : -1).toList();

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

}
