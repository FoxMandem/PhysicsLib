plugins {
    kotlin("jvm") version "1.9.23"
}

group = "me.foxmandem"
version = "0.0.1"

repositories {
    mavenCentral()
    maven( "https://jitpack.io" )
}

dependencies {
    // https://mvnrepository.com/artifact/net.minestom/minestom-snapshots
    implementation("net.minestom:minestom-snapshots:33dff6f458")
    implementation("com.github.stephengold:Libbulletjme:20.2.0")
    implementation("org.apache.commons:commons-geometry-euclidean:1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

}

kotlin {
    jvmToolchain(21)
}