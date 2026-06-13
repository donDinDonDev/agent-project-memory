rootProject.name = "gradle-multi-project"
include(":app", ":services:orders", ":libs:missing", ":libs:empty", ":app")
include(projectName)
project(":services:orders").projectDir = file("custom/orders")
