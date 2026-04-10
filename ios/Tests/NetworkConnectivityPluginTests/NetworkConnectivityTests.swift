import XCTest
@testable import NetworkConnectivityPlugin

class NetworkConnectivityTests: XCTestCase {
    func testEcho() {
        let implementation = NetworkConnectivity()
        let value = "Hello, World!"
        let result = implementation.echo(value)

        XCTAssertEqual(value, result)
    }
}
