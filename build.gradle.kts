plugins {
	java
	id("org.springframework.boot") version "3.1.1-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.0"
	id("com.diffplug.spotless") version "6.19.0"
}

group = "de.rafael"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains:annotations:24.0.0")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

spotless {
	java {
		importOrder()
		removeUnusedImports()
		endWithNewline()
		licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
		trimTrailingWhitespace()
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks {
	bootJar {
		launchScript()
	}

	// From StackOverflow: https://stackoverflow.com/a/53087407
	// Licensed under: CC BY-SA 4.0
	// Adapted to Kotlin
	register<Copy>("buildForDocker") {
		from(bootJar)
		into("build/libs/docker")
		rename { fileName ->
			// a simple way is to remove the "-$version" from the jar filename
			// but you can customize the filename replacement rule as you wish.
			fileName.replace("-$version", "")
		}
	}
}
