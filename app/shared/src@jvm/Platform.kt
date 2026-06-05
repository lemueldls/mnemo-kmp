fun getWorkspaceRoot(): String {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")

    return when {
        os.contains("win") -> System.getenv("LOCALAPPDATA") ?: "$userHome\\Documents\\Mnemo"
        os.contains("mac") -> "$userHome/Documents/Mnemo"
        else -> "$userHome/Documents/Mnemo" // Linux standard
    }
}
