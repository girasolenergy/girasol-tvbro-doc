plugins {
    java
}

val buildPagesTask = tasks.register<Sync>("buildPages") {
    group = "build"
    val destDir = project.layout.buildDirectory.dir("pages")
    into(destDir)
    from(project("heartbeat-monitor").tasks.named("jsBrowserProductionLibraryDistribution")) {
        into("heartbeat_monitor")
    }
    from(sourceSets.named("main").get().resources)
}
tasks.named("build").configure { dependsOn(buildPagesTask) }

val buildDevelopmentPagesTask = tasks.register<Sync>("buildDevelopmentPages") {
    group = "build"
    val destDir = project.layout.buildDirectory.dir("developmentPages")
    into(destDir)
    from(project("heartbeat-monitor").tasks.named("jsBrowserDevelopmentLibraryDistribution")) {
        into("heartbeat_monitor")
    }
    from(sourceSets.named("main").get().resources)
}
