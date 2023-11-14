package net.lawaxi.bot;

import cn.hutool.core.date.DateTime;
import cn.hutool.cron.Scheduler;
import cn.hutool.json.JSONObject;
import net.lawaxi.bot.helper.ConfigHelper;
import net.lawaxi.bot.helper.SNHeyHelper;
import net.lawaxi.bot.helper.WeiboLoginHelper2;
import net.lawaxi.bot.models.Subscribe;
import net.lawaxi.bot.util.TimeUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.Plugin;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.message.data.PlainText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public final class Archaeologist extends JavaPlugin {
    public static final Archaeologist INSTANCE = new Archaeologist();
    public static ConfigHelper config;
    public static SNHeyHelper snhey;
    public static WeiboLoginHelper2 weibo;

    private Archaeologist() {
        super(new JvmPluginDescriptionBuilder("net.lawaxi.snharch", "0.1.3-test4")
                .name("Archaeologist")
                .author("delay0delay")
                .dependsOn("net.lawaxi.shitboy", true)
                .build());
    }

    public static void executeDebugLog(String s) {
        if (false) {
            INSTANCE.getLogger().info(s);
        }
    }

    @Override
    public void onEnable() {
        config = new ConfigHelper(getConfigFolder(), resolveConfigFile("config.setting"));
        snhey = new SNHeyHelper();
        weibo = new WeiboLoginHelper2();
        listenBroadcast();

        boolean hasShitboy = hasShitboy();
        GlobalEventChannel.INSTANCE.registerListenerHost(new listener(hasShitboy));
        CommandManager.INSTANCE.registerCommand(new debug_command(), false);
    }

    private void listenBroadcast() {
        Scheduler archaeologist = new Scheduler();
        archaeologist.schedule("0 0 * * * ?", new Runnable() {
            @Override
            public void run() {
                for (Bot b : Bot.getInstances()) {
                    new Thread() {
                        @Override
                        public void run() {
                            for (Long g : config.getGroups()) {
                                Group group = b.getGroup(g);
                                if (group == null) {
                                    break;
                                }

                                List<JSONObject> ms = new ArrayList<>();
                                for (Subscribe sub : config.getSubscribes(g)) {
                                    DateTime start = DateTime.now();
                                    start.setYear(start.getYear() - sub.year);
                                    start.setMinutes(0);
                                    start.setSeconds(0);

                                    List<JSONObject> ms1 = snhey.getCurrent(sub.name, sub.original, start);
                                    for (JSONObject m1 : ms1) {
                                        for (JSONObject m : ms) {
                                            if (m.getLong("time") == m1.getLong("time") &&
                                                    m.getStr("name").equals(m1.getStr("name"))) {
                                                ms1.remove(m1); //避免重复
                                                break;
                                            }
                                        }
                                        m1.set("sub_year", sub.year);
                                    }

                                    ms.addAll(ms1);
                                }

                                ms.sort(Comparator.comparingLong(m -> m.getLong("time")));
                                for (JSONObject m : ms) {
                                    try {
                                        group.sendMessage(new PlainText("【" + m.getInt("sub_year") + "年前：" + m.getStr("name") + "微博更新：" + TimeUtil.time2String(m.getLong("time")) + "】\n")
                                                .plus(snhey.getMessage(m, group)));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }.start();
                }

            }
        });
        archaeologist.start();
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