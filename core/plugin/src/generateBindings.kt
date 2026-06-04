import java.nio.file.Path
import kotlin.io.path.*
import org.jetbrains.amper.plugins.*

@TaskAction
@OptIn(ExperimentalPathApi::class)
fun generateBindings(
    @Input projectDir: Path,
    @Output generatedCommonSourceDir: Path,
    @Output generatedJvmSourceDir: Path,
    @Output generatedJvmResourcesDir: Path,
    @Output generatedAndroidSourceDir: Path,
    @Output generatedAndroidJniLibsDir: Path,
) {
  generatedCommonSourceDir.deleteRecursively()
  generatedJvmSourceDir.deleteRecursively()
  generatedJvmResourcesDir.deleteRecursively()
  generatedAndroidSourceDir.deleteRecursively()
  generatedAndroidJniLibsDir.deleteRecursively()

  val process =
      ProcessBuilder("boltffi", "pack", "kmp").directory(projectDir.toFile()).inheritIO().start()

  val exitCode = process.waitFor()
  if (exitCode != 0) {
    error("boltffi pack all failed with exit code $exitCode")
  }

  val commonSourceDir = projectDir / "dist/kmp/src/commonMain/kotlin/core"
  commonSourceDir.copyToRecursively(
      generatedCommonSourceDir.createParentDirectories(),
      followLinks = false,
  )
  val jvmSourceDir = projectDir / "dist/kmp/src/jvmMain/kotlin/core"
  jvmSourceDir.copyToRecursively(
      generatedJvmSourceDir.createParentDirectories(),
      followLinks = false,
  )
  val jvmResourcesDir = projectDir / "dist/kmp/src/jvmMain/resources"
  jvmResourcesDir.copyToRecursively(
      generatedJvmResourcesDir.createParentDirectories(),
      followLinks = false,
  )
  val androidSourceDir = projectDir / "dist/kmp/src/androidMain/kotlin/core"
  androidSourceDir.copyToRecursively(
      generatedAndroidSourceDir.createParentDirectories(),
      followLinks = false,
  )
  val androidJniLibsDir = projectDir / "dist/kmp/src/androidMain/jniLibs"
  androidJniLibsDir.copyToRecursively(
      generatedAndroidJniLibsDir.createParentDirectories(),
      followLinks = false,
  )
}
