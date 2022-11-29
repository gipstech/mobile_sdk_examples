#ifndef LocationListener_h
#define LocationListener_h

#include <GiPStechSDK/GiPStech.h>

using namespace std;
using namespace gipstech;

class LocationListener : public IndoorLocalizer::Listener
{
    UITextView *mText;
    
public:
    void setTextView(UITextView *text)
    {
        mText = text;
        [mText setText:@""];
    }
    
    ~LocationListener()
    {
    }
    
    virtual void onLocationUpdate(IndoorLocation_sp location)
    {
        NSString* message = [NSString stringWithFormat:@"Location: %.7f, %.7f\n",
                             location->getLatitude(),
                             location->getLongitude()];
        [mText insertText:message];
    }

    virtual void onError(GiPStechError_sp error)
    {
        if (GiPStechError::FAST_CALIBRATION_REQUIRED == error->getCode())
        {
            [mText insertText:@"Calibration required!\n"];
            [mText insertText:@"PLEASE ROTATE THE PHONE\n"];
            
            GiPStech::getCalibrationManager().beginCalibration(CalibrationManager::CALIBRATION_TYPE_FAST, [this](bool result, uint8_t progress, GiPStechError_sp error)
            {
                if (CALLBACK_DONE == progress)
                {
                    [mText insertText:@"Calibration completed\n"];
                    GiPStech::getIndoorLocalizer().registerListener(this);
                }
                else if (CALLBACK_ERROR == progress)
                {
                    string msg = "CALIBRATION ERROR: " + error->getMessage() + "\n";
                    [mText insertText:[NSString stringWithUTF8String:msg.c_str()]];
                }
                else
                {
                    if (progress > 25)
                    {
                        GiPStech::getCalibrationManager().endCalibration();
                    }
                    else
                    {
                        NSString* message = [NSString stringWithFormat:@"Calibration progress: %d\n", progress];
                        [mText insertText:message];
                    }
                }
            });
        }
        else
        {
            string msg = "ERROR: " + error->getMessage() + "\n";
            [mText insertText:[NSString stringWithUTF8String:msg.c_str()]];
        }
    }

    virtual bool onFloorChange(Floor_sp floor)
    {
        string msg = "Floor: " + floor->getName() + "\n";
        [mText insertText:[NSString stringWithUTF8String:msg.c_str()]];
        return true;
    }

    virtual void onRegistrationComplete()
    {
        [mText insertText:@"Registration completed\n"];
    }

    virtual void onAttitudeUpdate(Attitude_sp attitude)
    {
    }
};

LocationListener mLocationListener;

#endif /* LocationListener_h */
