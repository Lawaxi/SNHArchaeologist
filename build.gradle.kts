plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.15.0"
}

group = "net.lawaxi.bot"
version = "0.1.3-test2"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation ("cn.hutool:hutool-all:5.8.18")
    api (files("libs/shitboy-0.1.9-test4.mirai2.jar"))
}