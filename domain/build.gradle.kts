plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:model"))
    // api, not implementation: DataResult (from core:common) appears in TransactionRepository's
    // and RefreshTransactionsUseCase's public signatures, so consumers need it transitively.
    api(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.test {
    useJUnitPlatform()
}
