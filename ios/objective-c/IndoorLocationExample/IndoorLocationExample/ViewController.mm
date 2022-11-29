#import "ViewController.h"

#include "LocationListener.hpp"

#define DEV_ID          "" // PUT YOUR DEV KEY HERE
#define BUILDING_ID     "" // PUT THE ID OF THE BUILDING HERE

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UITextView *mText;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    GiPStech::init(DEV_ID);
    
    mLocationListener.setTextView(_mText);
    
    GiPStech::getIndoorLocalizer().selectBuilding(BUILDING_ID, [self](bool result, uint8_t progress, GiPStechError_sp error)
    {
        if (CALLBACK_DONE == progress)
        {
            [_mText insertText:@"Starting location...\n"];
            GiPStech::getIndoorLocalizer().registerListener(&mLocationListener);
        }
        else if (CALLBACK_ERROR == progress)
        {
            string msg = "LOADING ERROR: " + error->getMessage() + "\n";
            [_mText insertText:[NSString stringWithUTF8String:msg.c_str()]];
        }
        else
        {
            NSString* message = [NSString stringWithFormat:@"Loading: %d\n", progress];
            [_mText insertText:message];
        }
    });
}


@end
