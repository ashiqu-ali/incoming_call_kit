// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "incoming_call_kit",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "incoming-call-kit",
            targets: ["incoming_call_kit"]
        )
    ],
    dependencies: [],
    targets: [
        .target(
            name: "incoming_call_kit",
            dependencies: [],
            path: "Classes",
            resources: [
                .process("Resources")
            ]
        )
    ]
)
