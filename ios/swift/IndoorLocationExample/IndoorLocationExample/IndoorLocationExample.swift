import SwiftUI

var sharedModel = Model()

@main
struct IndoorLocationExample: App {
    var body: some Scene {
        WindowGroup {
            ContentView(model: sharedModel)
        }
    }
}
