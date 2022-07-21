import java.text.SimpleDateFormat
import java.util.*


plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
}

if ((version as String).endsWith("SNAPSHOT")) {
    version = (version as String).replace("SNAPSHOT", SimpleDateFormat("yyMMdd.HHmmss").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date()))
}

tasks {
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }

    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    test {
        useJUnitPlatform()
    }

    register<Copy>("paperJar") {
        from(jar)

        val archiveBaseName = jar.get().archiveBaseName.get()
        val plugins = File("./.debug-server/plugins")
        val files = plugins.listFiles { file: File -> file.isFile && file.name.endsWith(".jar") } ?: emptyArray()

        if (files.any { it.name.startsWith(archiveBaseName, true) }) into(File(plugins, "update"))
        else into(plugins)
    }
}