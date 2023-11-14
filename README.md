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

在配置中填写的群中输入以下指令可以以今年为2016年进行播报，每小时播报下一小时内全部微博更新（可以同时关注不同年份）

- /历史 关注 SNH48-林忆宁 2016
- /历史 取消关注 SNH48-林忆宁
- /历史 取消关注 SNH48-林忆宁 2016

取消关注时不填年份则取消关注全部年份

### 日志

#### 0.1.0

- test5：修复不保存xox自己评论（highlight类）bug

#### 0.1.3

- 重置了配置格式，改用jsonArray
- 支持多群，支持多年份
- test4：播报排序优化
