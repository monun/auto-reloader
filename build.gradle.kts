import java.text.SimpleDateFormat
import java.util.*

plugins {
    java
    idea
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

    register<Copy>("testJar") {
        val prefix = project.name
        val plugins = file(".server/plugins")
        val update = File(plugins, "update")
        val regex = Regex("($prefix).*(.jar)")

        from(jar)
        into(if (plugins.listFiles { _, it -> it.matches(regex) }?.isNotEmpty() == true) update else plugins)

        doFirst { update.deleteRecursively() }
        doLast {
            update.mkdirs()
            File(update, "UPDATE").delete()
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".server"))
    }
}