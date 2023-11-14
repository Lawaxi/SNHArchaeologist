# SNHArchaeologist

Mirai-Console插件，构建mirai（[mamoe/mirai](https://github.com/mamoe/mirai), [docs.mirai.mamoe.net](https://docs.mirai.mamoe.net/)）后拖入plugins文件夹运行即可，首次运行生成配置，至少填写应援会管理QQ后重启即可正常使用。

### 功能

根据[SNHey](http://snhey.cloudapp.net/)的成员微博备份播报那年今日成员微博更新

### 配置

~~~
{
    "114514": [
        {
            "name": "SNH48-陈琳",
            "year": 8,
            "original": true
        },
        {
            "name": "SNH48-谢天依",
            "year": 8,
            "original": true
        }
    ],
    "1919810": [
        {
            "name": "SNH48-林忆宁",
            "year": 7,
            "original": false
        }
    ]
}
~~~

### 指令

在配置中填写的群中输入以下指令可以以今年为2016年进行播报，每小时播报下一小时内全部微博更新

- /历史 关注 SNH48-林忆宁 2016
- /历史 取消关注 SNH48-林忆宁 

### debug模式

将debug设为true后获得更多的控制台报告，并新增管理员指令

- /arch sub SNH48-林忆宁 2016
- /arch send SNH48-林忆宁 x
  - 发送当前时间段内第x条信息(控制台无法实现)
- /arch download SNH48-林忆宁 2016 2018 true true
  - 下载成员时间段内所有微博 可用于机器人离线使用(目前不读取本地图片)或自用浏览
  - original(第一个bool参数): 是否仅下载本人微博，否则也下载本人回复过的其他成员微博
  - save_img(第二个bool参数): 是否保存图片

### 日志

#### 0.1.0

- test5：修复不保存xox自己评论（highlight类）bug
