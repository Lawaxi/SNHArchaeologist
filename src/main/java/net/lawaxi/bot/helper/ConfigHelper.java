package net.lawaxi.bot.helper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.lawaxi.bot.Archaeologist;
import net.lawaxi.bot.models.Subscribe;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigHelper {

    public final File sourceFolder;
    public final File configFile;
    private final HashMap<Long, List<Subscribe>> subscribes = new HashMap<>();

    public ConfigHelper(File sourceFolder, File file) {
        this.sourceFolder = sourceFolder;
        this.configFile = file;

        if (!file.exists()) {
            FileUtil.writeString("{\"817151561\":[{\"name\":\"SNH48-林忆宁\",\"year\":7}]}", file, StandardCharsets.UTF_8);
        }

        JSONObject c = JSONUtil.readJSONObject(file, StandardCharsets.UTF_8);
        for (String g : c.keySet()) {
            try {
                JSONArray jsonArray = c.getJSONArray(g);
                subscribes.put(Long.valueOf(g), constructSubscribes(jsonArray));
            } catch (Exception e) {
                Archaeologist.INSTANCE.getLogger().warning("一个群的关注信息读取错误");
            }
        }
    }

    private List<Subscribe> constructSubscribes(JSONArray jsonArray) {
        List<Subscribe> subscribeList = new ArrayList<>();
        for (Object item : jsonArray) {
            JSONObject jsonObject = (JSONObject) item;
            String name = jsonObject.getStr("name");
            int year = jsonObject.getInt("year");
            boolean original = jsonObject.getBool("original", true);
            subscribeList.add(new Subscribe(name, year, original));
        }
        return subscribeList;
    }

    public List<Subscribe> getSubscribes(long group) {
        return subscribes.getOrDefault(group, new ArrayList<>());
    }

    public void addSubscribe(long group, Subscribe subscribe) {
        List<Subscribe> groupSubscribes = subscribes.getOrDefault(group, new ArrayList<>());
        groupSubscribes.add(subscribe);
        subscribes.put(group, groupSubscribes);

        // Update the JSON configuration file
        updateConfigFile();
    }

    public boolean rmSubscribe(long group, String name) {
        List<Subscribe> groupSubscribes = subscribes.get(group);

        if (groupSubscribes == null) {
            return false; // Group not found
        }

        boolean removed = false;
        for (Subscribe subscribe : groupSubscribes) {
            if (subscribe.name.equals(name)) {
                groupSubscribes.remove(subscribe);
                removed = true;
            }
        }

        if (removed) {
            updateConfigFile();
        }

        return removed;
    }

    public boolean rmSubscribe(long group, String name, int year) {
        List<Subscribe> groupSubscribes = subscribes.get(group);

        if (groupSubscribes == null) {
            return false; // Group not found
        }

        for (Subscribe subscribe : groupSubscribes) {
            if (subscribe.equals(new Subscribe(name, year))) {
                groupSubscribes.remove(subscribe);
                updateConfigFile();
                return true;
            }
        }

        return false;
    }

    public Subscribe getFirstSubscribe(long group, String name) {
        List<Subscribe> groupSubscribes = subscribes.get(group);

        if (groupSubscribes == null) {
            return null; // Group not found
        }

        for (Subscribe subscribe : groupSubscribes) {
            if (subscribe.name.equals(name)) {
                return subscribe;
            }
        }

        return null; // Subscribe not found
    }

    public Subscribe getSubscribe(long group, String name, int year) {
        List<Subscribe> groupSubscribes = subscribes.get(group);

        if (groupSubscribes == null) {
            return null; // Group not found
        }

        for (Subscribe subscribe : groupSubscribes) {
            if (subscribe.equals(new Subscribe(name, year))) {
                return subscribe;
            }
        }

        return null; // Subscribe not found
    }

    public List<Long> getGroups() {
        return new ArrayList<>(subscribes.keySet());
    }

    private void updateConfigFile() {
        JSONObject updatedConfig = new JSONObject();

        for (Long group : subscribes.keySet()) {
            JSONArray groupSubscriptions = new JSONArray();
            List<Subscribe> groupSubscribes = subscribes.get(group);
            for (Subscribe subscribe : groupSubscribes) {
                JSONObject subscriptionObject = new JSONObject();
                subscriptionObject.put("name", subscribe.name);
                subscriptionObject.put("year", subscribe.year);
                subscriptionObject.put("original", subscribe.original);
                groupSubscriptions.add(subscriptionObject);
            }
            updatedConfig.put(group.toString(), groupSubscriptions);
        }

        FileUtil.writeString(updatedConfig.toStringPretty(), configFile, StandardCharsets.UTF_8);
    }

    public void storeSource(String name, String time, boolean original, String content) {
        File folder = new File(this.sourceFolder, name + (original ? "-Ori" : ""));
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(folder, time + ".json");
        if (!file.exists()) {
            FileUtil.touch(file);
        }
        FileUtil.writeString(content, file, StandardCharsets.UTF_8);
    }

    public String loadSource(String name, String time, boolean original) {
        File file = new File(new File(this.sourceFolder, name + (original ? "-Ori" : "")), time + ".json");
        if (file.exists()) {
            return FileUtil.readString(file, StandardCharsets.UTF_8);
        } else return "";
    }
}