import SwiftUI

var sharedModel = Model()

@main
struct UniversalLocationExample: App {
    var body: some Scene {
        WindowGroup {
            ContentView(model: sharedModel)
        }
    }
}
