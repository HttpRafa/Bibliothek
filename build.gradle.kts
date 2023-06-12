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
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
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
