import { codegenNativeComponent } from 'react-native';
import type {
  CodegenTypes,
  ColorValue,
  HostComponent,
  ViewProps,
} from 'react-native';

export type ContextMenuAction = Readonly<{
  id: string;
  title: string;
  /** SF Symbol name (iOS). */
  systemImage?: string;
  /** Drawable resource name (Android). */
  androidIcon?: string;
  destructive?: boolean;
}>;

export type ContextMenuActionEvent = Readonly<{ id: string }>;

export interface NativeProps extends ViewProps {
  actions: ReadonlyArray<ContextMenuAction>;
  androidTrigger?: CodegenTypes.WithDefault<'longPress' | 'tap', 'longPress'>;
  androidDestructiveColor?: ColorValue;
  /** The corner radius of the lifted preview: the children's own. */
  iosPreviewCornerRadius?: CodegenTypes.WithDefault<CodegenTypes.Float, 0>;
  onActionPress?: CodegenTypes.DirectEventHandler<ContextMenuActionEvent>;
}

export default codegenNativeComponent<NativeProps>(
  'RNContextMenu'
) as HostComponent<NativeProps>;
