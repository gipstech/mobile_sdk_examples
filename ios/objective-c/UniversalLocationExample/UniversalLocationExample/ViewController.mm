#import "ViewController.h"

#include "LocationListener.hpp"

#define DEV_ID  "" // PUT YOUR DEV KEY HERE

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UITextView *mText;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    GiPStech::init(DEV_ID);
    
    mLocationListener.setTextView(_mText);
    
    [_mText insertText:@"Starting location...\n"];
    GiPStech::getUniversalLocalizer().registerListener(&mLocationListener);
}


@end
