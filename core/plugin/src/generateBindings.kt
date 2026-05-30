import org.jetbrains.amper.plugins.*

import java.nio.file.Path
import kotlin.io.path.*

@TaskAction
@OptIn(ExperimentalPathApi::class)
fun generateBindings(
    @Input coreDir: Path,
    @Output generatedSourceDir: Path,
    @Output generatedIncludeDir: Path,
    @Output generatedJniLibsDir: Path,
    @Output generatedJvmResourcesDir: Path,
) {
    generatedSourceDir.deleteRecursively()
    generatedIncludeDir.deleteRecursively()
    generatedJniLibsDir.deleteRecursively()
    generatedJvmResourcesDir.deleteRecursively()

    val process = ProcessBuilder("boltffi", "pack", "all")
        .directory(coreDir.toFile())
        .inheritIO()
        .start()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("boltffi pack all failed with exit code $exitCode")
    }

    val sourceDir = coreDir / "dist/android/kotlin";
    sourceDir.copyToRecursively(
        generatedSourceDir.createParentDirectories(),
        followLinks = false
    )

    val includeDir = coreDir / "dist/android/include";
    includeDir.copyToRecursively(
        generatedIncludeDir.createParentDirectories(),
        followLinks = false
    )

    val jniLibsDir = coreDir / "dist/android/jniLibs";
    jniLibsDir.copyToRecursively(
        generatedJniLibsDir.createParentDirectories(),
        followLinks = false
    )

    val jvmResourcesDir = coreDir / "dist/kmp/src/jvmMain/resources";
    jvmResourcesDir.copyToRecursively(
        generatedJvmResourcesDir.createParentDirectories(),
        followLinks = false
    )
}
