package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import net.lawaxi.bot.models.Subscribe;
import net.lawaxi.bot.models.Time;
import net.lawaxi.util.CommandOperator;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

import static net.lawaxi.bot.Archaeologist.config;
import static net.lawaxi.bot.Archaeologist.snhey;

public class listener extends SimpleListenerHost {

    public listener(boolean hasShitboy) {
        if (hasShitboy) {
            CommandOperator.INSTANCE.addHelp(getHelp());
        }
    }

    public static boolean download(Subscribe sub) {
        try {
            DateTime start = DateTime.now();
            start.setYear(start.getYear() - sub.year);

            Time time = Time.current(start);
            snhey.download(sub.name, "" + time, false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @EventHandler()
    public ListeningStatus onGroupMessageEvent(GroupMessageEvent event) {
        long g = event.getGroup().getId();
        String message = event.getMessage().contentToString();
        if (message.startsWith("/历史")) {
            String[] args = message.split(" ");
            if (args[1].equals("关注") && args.length == 4) {
                String name = args[2];
                int year = DateTime.now().year() - Integer.valueOf(args[3]);
                if (config.getSubscribe(g, name, year) == null) {
                    Subscribe sub = new Subscribe(name, year);
                    if (download(sub)) {
                        config.addSubscribe(g, sub);
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("关注成功"));
                    } else {
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("下载资源失败，关注失败"));
                    }
                } else {
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经关注过"));
                }

            } else if (args[1].equals("取消关注") && args.length >= 3) {
                String name = args[2];
                boolean s;
                if (args.length == 3) {
                    s = config.rmSubscribe(g, name);
                } else {
                    int year = DateTime.now().year() - Integer.valueOf(args[3]);
                    s = config.rmSubscribe(g, name, year);
                }

                if (s) {
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus((args.length == 3 ? "全部" : "") + "取关成功"));
                } else {
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus("未关注"));
                }
            } else {
                event.getGroup().sendMessage(new At(event.getSender().getId()).plus(getHelp()));
            }
        }
        return ListeningStatus.LISTENING;
    }

    public String getHelp() {
        return "【成员历史微博考古相关】\n"
                + "/历史 关注 <成员名> <初始年份>\n"
                + "/历史 取消关注 <成员名> (<初始年份>)\n"
                + "注：取关时不填初始年份默认全部取关\n";
    }
}
