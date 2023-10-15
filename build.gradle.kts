plugins {
    id("java")
    id("maven-publish")
}

group = "de.tomjuri"
version = "1.0.0"

java.withSourcesJar()
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
java.toolchain.languageVersion = JavaLanguageVersion.of(8)

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()
        }
    }
}