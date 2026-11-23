# @27appstudio/react-native-system-menu

Native context menus for React Native:

- **iOS**: SwiftUI's `.contextMenu` around your React views. The card lifts and the menu is the system's own, in Liquid Glass on iOS 26 and later.
- **Android**: a Material 3 popup (AppCompat `PopupMenu` with Material 3 styles), opened at your finger on a long press, or on a tap for a header menu. Destructive items take your color.

It's a Fabric component: no Expo modules, no config plugin.

## Installation

```sh
npm install @27appstudio/react-native-system-menu
cd ios && pod install
```

Requirements:

- React Native 0.84+ with the New Architecture
- **Android**: nothing more. The library ships `com.google.android.material:material` 1.13. Your app theme can stay AppCompat: menus are built with their own Material 3 theme (see [Android theme](#android-theme)).

## Usage

```tsx
import { ContextMenu } from '@27appstudio/react-native-system-menu';
import { TouchableOpacity } from 'react-native';

<ContextMenu
  actions={[
    {
      id: 'edit',
      title: 'Edit',
      systemImage: 'pencil',
      androidIcon: 'ic_edit',
    },
    {
      id: 'delete',
      title: 'Delete',
      systemImage: 'trash',
      androidIcon: 'ic_delete',
      destructive: true,
    },
  ]}
  androidDestructiveColor="#FF3B30"
  iosPreviewCornerRadius={24}
  onActionPress={({ nativeEvent }) => handle(nativeEvent.id)}
>
  <TouchableOpacity onPress={open}>
    <Card />
  </TouchableOpacity>
</ContextMenu>;
```

`ContextMenu` is a plain native view: give it a style, a `testID` and accessibility props as you would a `View`. It works with Reanimated's `createAnimatedComponent`, so a card can use `layout` and `exiting` animations.

### A pressable child

A long press opens the menu and never presses the child: on iOS SwiftUI cancels the press, on Android the library does.

On iOS, touches that Detox synthesizes inside the app are not cancelled, so under Detox a long press also presses the child. If your Detox tests long-press a pressable, give it these props on iOS:

```tsx
<TouchableOpacity
  onPress={open}
  {...(Platform.OS === 'ios' && {
    delayLongPress: 250,
    onLongPress: () => {},
  })}
>
```

React Native then drops a press held longer than 250ms, so Detox sees the menu alone. Real users get the same, plus a short gap between 250ms and the menu where a release does nothing.

## API

| Prop                      |                                                                                                                 |
| ------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `actions`                 | The menu, in order. Each has an `id`, a `title`, and optionally `systemImage`, `androidIcon` and `destructive`. |
| `onActionPress`           | Called with `{ nativeEvent: { id } }`, the `id` of the item picked.                                             |
| `androidTrigger`          | `'longPress'` (default) or `'tap'`, for a menu behind a button.                                                 |
| `androidDestructiveColor` | Android: the title and icon of destructive items. iOS always uses its system red.                               |
| `iosPreviewCornerRadius`  | iOS: the corner radius of the lifted card, usually your view's own. Defaults to `0`.                            |

`systemImage` is an SF Symbol name. `androidIcon` is the name of a drawable in your app's resources.

## Android theme

Menus are built with the `Theme.RNContextMenu` style, a plain `Theme.Material3.DayNight.NoActionBar`. To change the surface, corners or text, redefine it in your app's `android/app/src/main/res/values/styles.xml`; your app's resources win over the library's:

```xml
<style name="Theme.RNContextMenu" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="android:contextPopupMenuStyle">@style/AppMenuPopup</item>
    <item name="textAppearanceLargePopupMenu">@style/AppMenuItem</item>
</style>

<style name="AppMenuPopup" parent="Widget.Material3.PopupMenu.ContextMenu">
    <item name="android:popupBackground">@drawable/menu_background</item>
</style>

<style name="AppMenuItem" parent="TextAppearance.Material3.BodyLarge">
    <item name="android:textColor">@color/label</item>
</style>
```

Menu rows read the un-prefixed `textAppearanceLargePopupMenu`, not the `android:` attribute.

## Testing with Detox

Long-press the `ContextMenu` (or the child), then tap the item by text:

```ts
await element(by.id('card-menu')).atIndex(0).longPress();
await element(by.text('Edit')).atIndex(0).tap();
```

While the iOS menu animates in, an item can show as two or three labels stacked at the same spot, so take the first match.

## Platform notes

- **Detox on iOS**: Detox creates its touches in the app, and UIKit gives such a touch its gesture recognizers by walking the private `_parentGestureRecognizerContainer` chain. SwiftUI's host answers `nil` until it has been asked once, and the touch then reached nothing. The library asks when the menu enters a window and on every hit test made without an event, as [react-native-pager-view does](https://github.com/callstack/react-native-pager-view/pull/1208). The selector is guarded by `responds(to:)`: if Apple removes it, synthesized taps stop working and nothing crashes. Real touches never depend on it.
- **iOS 26 and later**: as the card returns after a cancel, the system briefly crossfades it (a fraction of a second). Apple's own `.contextMenu` does the same.
- **Android anchor**: the popup is anchored to a temporary 1px view added to the window's content, at your finger, outside React Native's view tree.

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

## react-native-system-menu is made by 27AppStudio

[27AppStudio](https://27appstudio.com) is an independent French mobile app studio. We build iPhone and Android apps, and open-source the native UI components and release tooling we make along the way. Need a hand with a React Native app? [Get in touch](https://27appstudio.com).
