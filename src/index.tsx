/**
 * A native context menu around its children. A long press opens the menu and never presses
 * the child (see the README for Detox on iOS).
 */
export { default as ContextMenu } from './RNContextMenuNativeComponent';
export type {
  ContextMenuAction,
  ContextMenuActionEvent,
  NativeProps as ContextMenuProps,
} from './RNContextMenuNativeComponent';
