// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SpoonconsultingCapacitorPluginNetworkConnectivity",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "SpoonconsultingCapacitorPluginNetworkConnectivity",
            targets: ["NetworkConnectivityPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "NetworkConnectivityPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NetworkConnectivityPlugin"),
        .testTarget(
            name: "NetworkConnectivityPluginTests",
            dependencies: ["NetworkConnectivityPlugin"],
            path: "ios/Tests/NetworkConnectivityPluginTests")
    ]
)
