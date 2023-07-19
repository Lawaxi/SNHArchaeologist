package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import net.lawaxi.bot.models.Subscribe;
import net.lawaxi.bot.models.Time;
import net.lawaxi.util.CommandOperator;
import net.mamoe.mirai.contact.Group;
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

    @EventHandler()
    public ListeningStatus onGroupMessageEvent(GroupMessageEvent event) {
        Group group = event.getGroup();
        if (group.getId() == config.group()) {
            String message = event.getMessage().contentToString();
            if (message.startsWith("/历史")) {
                String[] args = message.split(" ");
                if (args[1].equals("关注") && args.length == 4) {
                    if (config.getSubscribe(args[2]) == null) {
                        Subscribe sub = new Subscribe(args[2], DateTime.now().year() - Integer.valueOf(args[3]));
                        if (download(sub)) {
                            event.getGroup().sendMessage(new At(event.getSender().getId()).plus("关注成功"));
                            config.addSubscribe(sub);
                        } else {
                            event.getGroup().sendMessage(new At(event.getSender().getId()).plus("下载资源失败，关注失败"));
                        }
                    } else {
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("已经关注过"));
                    }

                } else if (args[1].equals("取消关注") && args.length == 3) {
                    if (config.rmSubscribe(args[2])) {
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("取关成功"));
                    } else {
                        event.getGroup().sendMessage(new At(event.getSender().getId()).plus("未关注"));
                    }
                } else {
                    event.getGroup().sendMessage(new At(event.getSender().getId()).plus(getHelp()));
                }
            }
        }
        return ListeningStatus.LISTENING;
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

    public String getHelp() {
        return "【成员历史微博考古相关】\n"
                + "/历史 关注 <成员名> <初始年份>\n"
                + "/历史 取消关注 <成员名>\n";
    }
}
