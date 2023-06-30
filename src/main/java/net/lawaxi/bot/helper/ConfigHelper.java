package net.lawaxi.bot.helper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.bot.models.Subscribe;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigHelper {

    public final File sourceFolder;
    public final Setting setting;
    private final List<Subscribe> subscribes;
    private final long group;
    private final boolean debug;

    public ConfigHelper(File sourceFolder, File file) {
        this.sourceFolder = sourceFolder;
        if (!file.exists()) {
            FileUtil.touch(file);
            Setting s = new Setting(file, StandardCharsets.UTF_8, false);
            s.set("group", "817151561");
            s.set("debug", "false");
            s.store();
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        this.subscribes = new ArrayList<>();
        for (String key : this.setting.keySet("subscribes")) {
            this.subscribes.add(new Subscribe(key, this.setting.getInt(key, "subscribes", 0)));
        }
        this.group = this.setting.getLong("group", 817151561L);
        this.debug = this.setting.getBool("debug", false);
    }

    public List<Subscribe> getSubscribes() {
        return this.subscribes;
    }

    public void addSubscribe(Subscribe subscribe) {
        this.subscribes.add(subscribe);
        this.setting.setByGroup(subscribe.name, "subscribes", "" + subscribe.year);
        this.setting.store();
    }

    public boolean rmSubscribe(String name) {
        Subscribe s = getSubscribe(name);
        if (s == null)
            return false;

        else {
            this.subscribes.remove(s);
            this.setting.remove(name, "subscribes");
            this.setting.store();
            return true;
        }
    }

    public Subscribe getSubscribe(String name) {
        for (Subscribe subscribe : this.subscribes) {
            if (subscribe.name.equals(name)) {
                return subscribe;
            }
        }
        return null;

    }

    public void storeSource(String name, String time, String content) {
        File folder = new File(this.sourceFolder, name);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, time + ".json");
        if (!file.exists()) {
            FileUtil.touch(file);
        }
        FileUtil.writeString(content, file, StandardCharsets.UTF_8);
    }

    public String loadSource(String name, String time) {
        File file = new File(new File(this.sourceFolder, name), time + ".json");
        if (file.exists()) {
            return FileUtil.readString(file, StandardCharsets.UTF_8);
        } else return "";
    }

    public long group() {
        return this.group;
    }

    public boolean debug() {
        return this.debug;
    }
}
