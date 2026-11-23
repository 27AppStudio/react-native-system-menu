#import "RNContextMenuView.h"

#import <React/RCTConversions.h>
#import <react/renderer/components/RNContextMenuSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNContextMenuSpec/EventEmitters.h>
#import <react/renderer/components/RNContextMenuSpec/Props.h>
#import <react/renderer/components/RNContextMenuSpec/RCTComponentViewHelpers.h>

#if __has_include(<RNContextMenu/RNContextMenu-Swift.h>)
#import <RNContextMenu/RNContextMenu-Swift.h>
#else
#import "RNContextMenu-Swift.h"
#endif

using namespace facebook::react;

@interface RNContextMenuView () <RCTRNContextMenuViewProtocol>
@end

@implementation RNContextMenuView {
  RNContextMenuHost *_host;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<RNContextMenuComponentDescriptor>();
}

// The system can still be animating this view after it unmounts, so never hand it to another card.
+ (BOOL)shouldBeRecycled
{
  return NO;
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    _props = RNContextMenuShadowNode::defaultSharedProps();
    _host = [RNContextMenuHost new];
    __weak RNContextMenuView *weakSelf = self;
    _host.onSelect = ^(NSString *actionId) {
      [weakSelf emitAction:actionId];
    };
    [self addSubview:_host.view];
  }
  return self;
}

- (void)layoutSubviews
{
  [super layoutSubviews];
  _host.view.frame = self.bounds;
}

- (void)didMoveToWindow
{
  [super didMoveToWindow];
  if (self.window) {
    [_host attachTo:self];
    // After SwiftUI has laid the host out in its window.
    __weak RNContextMenuHost *host = _host;
    dispatch_async(dispatch_get_main_queue(), ^{
      [host resolveGestureRecognizerContainers];
    });
  } else {
    [_host detach];
  }
}

- (void)invalidate
{
  [_host detach];
}

// Detox finds the touched view with a hit test that has no event, then builds the touch
// from it: resolve the chain now too, as a SwiftUI update since the last resolve (cards
// reflowing after a delete) leaves it stale again. Real touches always carry an event.
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event
{
  if (!event) {
    [_host resolveGestureRecognizerContainers];
  }
  return [super hitTest:point withEvent:event];
}

// The children belong to SwiftUI's representable, not to this view.
- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
  [_host.container insertSubview:childComponentView atIndex:index];
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
  [childComponentView removeFromSuperview];
}

- (void)updateProps:(const Props::Shared &)props oldProps:(const Props::Shared &)oldProps
{
  const auto &menuProps = static_cast<const RNContextMenuProps &>(*props);

  NSMutableArray<RNContextMenuItem *> *items = [NSMutableArray new];
  for (const auto &action : menuProps.actions) {
    [items addObject:[[RNContextMenuItem alloc] initWithId:RCTNSStringFromString(action.id)
                                                     title:RCTNSStringFromString(action.title)
                                               systemImage:RCTNSStringFromString(action.systemImage)
                                               destructive:action.destructive]];
  }
  [_host updateWithItems:items cornerRadius:menuProps.iosPreviewCornerRadius];

  [super updateProps:props oldProps:oldProps];
}

- (void)emitAction:(NSString *)actionId
{
  if (!_eventEmitter) {
    return;
  }
  static_cast<const RNContextMenuEventEmitter &>(*_eventEmitter).onActionPress({.id = std::string(actionId.UTF8String)});
}

@end
