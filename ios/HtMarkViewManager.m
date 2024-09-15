#import <React/RCTViewManager.h>

@interface RCT_EXTERN_REMAP_MODULE(HtMarkView, HtMarkViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(params, NSDictionary)
RCT_EXPORT_SHADOW_PROPERTY(params, NSDictionary)

RCT_EXTERN_METHOD(findLink:(nonnull NSNumber *)node
                  locationX:(nonnull NSNumber *)locationX
                  locationY:(nonnull NSNumber *)locationY
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(findLinkSync:(nonnull NSNumber *)node
                  locationX:(nonnull NSNumber *)locationX
                  locationY:(nonnull NSNumber *)locationY)
@end
