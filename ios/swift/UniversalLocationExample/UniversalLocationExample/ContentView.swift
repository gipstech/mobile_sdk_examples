import SwiftUI
import CoreLocation
import GiPStechSwiftSDK

let DEV_KEY: String = "" // PUT YOUR DEV KEY HERE

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
            GiPStech.getUniversalLocalizer().registerListener( MyUniversalListener() )
        } else {
            sharedModel.mainTextView.append("Calibration progress: \(progress)%\n")
            if (progress >= 25) {
                GiPStech.getCalibrationManager().endCalibration()
            }
        }
    }
}

func startLocation() {
    GiPStech.getUniversalLocalizer().registerListener(MyUniversalListener())
}

class MyUniversalListener: NSObject, UniversalProtocol {
    func onLocationUpdate(_ location: UniversalLocation) {
        sharedModel.mainTextView.append("Location: \(location.getLatitude()) \(location.getLongitude())\n")
    }
    
    func onTransition(_ building: Building!, _ floor: Floor!) {
        if (building != nil) {
            if (floor != nil) {
                sharedModel.mainTextView.append("Going at " + floor.getName() + "\n")
            } else {
                sharedModel.mainTextView.append("Going inside " + building.getName() + "\n")
            }
        } else {
            sharedModel.mainTextView.append("Going outdoor\n")
        }
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
