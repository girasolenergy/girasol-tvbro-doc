plugins {
    java
}

val buildPagesTask = tasks.register<Sync>("buildPages") {
    group = "build"
    val destDir = project.layout.buildDirectory.dir("pages")
    into(destDir)
    from(project("heartbeat_monitor").tasks.named("jsBrowserDistribution")) {
        into("heartbeat_monitor")
    }
    from(sourceSets.named("main").get().resources)
}
tasks.named("build").configure { dependsOn(buildPagesTask) }
