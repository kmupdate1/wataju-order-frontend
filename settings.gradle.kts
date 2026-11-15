pluginManagement { repositories { mavenCentral(); gradlePluginPortal(); google() } }
dependencyResolutionManagement { repositories { mavenCentral(); google() } }
rootProject.name = "wataju-order"

include(":b3-foundation", ":b3-ui", ":order-application")
