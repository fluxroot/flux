import com.fluxchess.flux.build.plugin.ci.CiExtension

plugins {
	java
}

repositories {
	jcenter()
}

apply(plugin = "flux-ci")
val ci: CiExtension by extensions

group = "com.fluxchess.flux"
version = "2.3.0"
apply(plugin = "flux-versioning")

tasks.withType<AbstractArchiveTask> {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
	implementation("com.fluxchess.jcpi:jcpi:1.4.1")

	testImplementation(Libs.junitJupiterApi)
	testRuntimeOnly(Libs.junitJupiterEngine)
}

tasks.test {
	ignoreFailures = ci.buildingOnCi
	useJUnitPlatform()
}
