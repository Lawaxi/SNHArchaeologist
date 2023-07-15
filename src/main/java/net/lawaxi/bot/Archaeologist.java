package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import cn.hutool.cron.CronUtil;
import cn.hutool.json.JSONObject;
import net.lawaxi.bot.helper.ConfigHelper;
import net.lawaxi.bot.helper.SNHeyHelper;
import net.lawaxi.bot.helper.WeiboLoginHelper;
import net.lawaxi.bot.models.Subscribe;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.Plugin;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.message.data.PlainText;

import java.util.List;


public final class Archaeologist extends JavaPlugin {
    public static final Archaeologist INSTANCE = new Archaeologist();
    public static ConfigHelper config;
    public static SNHeyHelper snhey;
    public static WeiboLoginHelper weibo;

    public static void executeDebugLog(String s) {
        if (config.debug()) {
            INSTANCE.getLogger().info(s);
        }
    }

    private Archaeologist() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.snharch", "0.1.0")
                .name("Archaeologist")
                .author("delay0delay")
                .dependsOn("net.lawaxi.shitboy", true)
                .build());
    }

    @Override
    public void onEnable() {
        config = new ConfigHelper(getConfigFolder(), resolveConfigFile("config.setting"));
        snhey = new SNHeyHelper();
        weibo = new WeiboLoginHelper();

        GlobalEventChannel.INSTANCE.parentScope(INSTANCE).subscribeOnce(BotOnlineEvent.class, event -> {
            listenBroadcast(event.getBot());
        });

        GlobalEventChannel.INSTANCE.registerListenerHost(new listener(hasShitboy()));
        if (config.debug()) {
            CommandManager.INSTANCE.registerCommand(new debug_command(), false);
        }
    }

    private void listenBroadcast(Bot b) {
        CronUtil.schedule("0 0 * * * ?", new Runnable() {
            @Override
            public void run() {
                Group group = b.getGroup(config.group());
                if (group == null)
                    return;
                new Thread() {
                    @Override
                    public void run() {
                        for (Subscribe sub : config.getSubscribes()) {
                            DateTime start = DateTime.now();
                            start.setYear(start.getYear() - sub.year);

                            List<JSONObject> l = snhey.getCurrent(sub.name, start);
                            for (int i = l.size() - 1; i >= 0; i--) {
                                try {
                                    group.sendMessage(new PlainText("【" + sub.year + "年前：" + l.get(i).getStr("name") + "微博更新：" + DateTime.of(l.get(i).getLong("time")).toStringDefaultTimeZone() + "】\n")
                                            .plus(snhey.getMessage(l.get(i), group)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }.start();

            }
        });
        CronUtil.start();
    }

    private boolean hasShitboy() {
        for (Plugin plugin : PluginManager.INSTANCE.getPlugins()) {
            if (PluginManager.INSTANCE.getPluginDescription(plugin).getId().equals("net.lawaxi.shitboy")) {
                return true;
            }
        }
        return false;
    }

}