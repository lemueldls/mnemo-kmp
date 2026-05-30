import org.jetbrains.amper.plugins.*

import java.nio.file.Path
import kotlin.io.path.*

@TaskAction
@OptIn(ExperimentalPathApi::class)
fun packageArtifacts(
    @Input coreDir: Path,
    @Output generatedCommonSourceDir: Path,
    @Output generatedJvmSourceDir: Path,
    @Output generatedJvmResourcesDir: Path,
    @Output generatedAndroidSourceDir: Path,
) {
    generatedCommonSourceDir.deleteRecursively()
    generatedJvmSourceDir.deleteRecursively()
    generatedJvmResourcesDir.deleteRecursively()
    generatedAndroidSourceDir.deleteRecursively()

    val process = ProcessBuilder("boltffi", "pack", "kmp")
        .directory(coreDir.toFile())
        .inheritIO()
        .start()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("boltffi pack all failed with exit code $exitCode")
    }

    val commonSourceDir = coreDir / "dist/kotlin-multiplatform/src/commonMain/kotlin/core";
    commonSourceDir.copyToRecursively(
        generatedCommonSourceDir.createParentDirectories(),
        followLinks = false
    )
    val jvmSourceDir = coreDir / "dist/kotlin-multiplatform/src/jvmMain/kotlin/core";
    jvmSourceDir.copyToRecursively(
        generatedJvmSourceDir.createParentDirectories(),
        followLinks = false
    )
    val jvmResourcesDir = coreDir / "dist/kotlin-multiplatform/src/jvmMain/resources";
    jvmResourcesDir.copyToRecursively(
        generatedJvmResourcesDir.createParentDirectories(),
        followLinks = false
    )
    val androidSourceDir = coreDir / "dist/kotlin-multiplatform/src/androidMain/kotlin/core";
    androidSourceDir.copyToRecursively(
        generatedAndroidSourceDir.createParentDirectories(),
        followLinks = false
    )
}
