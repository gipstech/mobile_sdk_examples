import SwiftUI
import CoreLocation
import GiPStechSwiftSDK

let DEV_KEY: String = "" // PUT YOUR DEV KEY HERE
let BUILDING_ID: String = "" // PUT THE ID OF THE BUILDING HERE

class Model: ObservableObject {
    @Published var mainTextView: String = ""
}

struct ContentView: View {
    @ObservedObject var model: Model
    
    init(model: Model) {
        self.model = model
    }
    
    var body: some View {
        ScrollView {
            HStack {
                Text(model.mainTextView)
                    .onAppear {
                        CLLocationManager().requestWhenInUseAuthorization()
                        GiPStech.initialize(DEV_KEY)
                        startLocation()
                    }
                Spacer()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(model: sharedModel)
    }
}

func performFastCalibration() {
    GiPStech.getCalibrationManager().beginCalibration(CALIBRATION_TYPE_FAST) { completed, progress, error in
        if (CallbackCode.ERROR.rawValue == progress) {
            sharedModel.mainTextView.append("ERROR: \(error!.getMessage()!)\n")
        } else if (CallbackCode.DONE.rawValue == progress) {
            sharedModel.mainTextView.append("Calibration completed\n")
            GiPStech.getIndoorLocalizer().registerListener(MyIndoorListener())
        } else {
            sharedModel.mainTextView.append("Calibration progress: \(progress)%\n")
            if (progress >= 25) {
                GiPStech.getCalibrationManager().endCalibration()
            }
        }
    }
}

func startLocation() {
    GiPStech.getIndoorLocalizer().selectBuilding(BUILDING_ID) { completed, progress, error in
        if (CallbackCode.ERROR.rawValue == progress) {
            sharedModel.mainTextView.append("ERROR: \(error!.getMessage()!)\n")
        } else if (CallbackCode.DONE.rawValue == progress) {
            sharedModel.mainTextView.append("Selection completed\n")
            GiPStech.getIndoorLocalizer().registerListener(MyIndoorListener())
        } else {
            sharedModel.mainTextView.append("Selection progress: \(progress)%\n")
        }
    }
}

class MyIndoorListener: NSObject, IndoorProtocol {
    func onRegistrationUpdate(_ percentage: UInt8) {
        sharedModel.mainTextView.append("Registration progress: \(percentage)%\n")
    }
 
    func onRegistrationComplete() {
        sharedModel.mainTextView.append("Registration completed\n")
    }
    
    func onLocationUpdate(_ location: IndoorLocation!) {
        sharedModel.mainTextView.append("Location: \(location.getLatitude()) \(location.getLongitude())\n")
    }
 
    func onFloorChange(_ floor: Floor!) -> Bool {
        sharedModel.mainTextView.append("Floor: \(floor!.getName()!)\n")
        return true
    }
 
    func onAttitudeUpdate(_ attitude: Attitude!) {
        sharedModel.mainTextView.append("Heading: \(attitude.getHeading())\n")
    }
    
    func onError(_ error: GiPStechError!) {
        sharedModel.mainTextView.append("ERROR: \(error!.getMessage()!)\n")
        
        if (ErrorCode.FAST_CALIBRATION_REQUIRED == error.getCode()) {
            performFastCalibration()
        }
    }
}
