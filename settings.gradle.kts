rootProject.name = "SS-Gun"

includeBuild("C:\\Folder\\SS\\SS-Core") {
    dependencySubstitution {
        substitute(module("me.syari.ss:core")).with(project(":"))
    }
}