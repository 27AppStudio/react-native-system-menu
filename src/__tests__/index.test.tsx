import { fireEvent, render } from '@testing-library/react-native';
import { Text } from 'react-native';

import { ContextMenu } from '../index';

const actions = [
  { id: 'edit', title: 'Edit', systemImage: 'pencil', androidIcon: 'ic_edit' },
  { id: 'delete', title: 'Delete', destructive: true },
];

describe('ContextMenu', () => {
  it('renders the native view around its children, with the props as given', () => {
    const screen = render(
      <ContextMenu
        actions={actions}
        androidDestructiveColor="#FF3B30"
        androidTrigger="tap"
        iosPreviewCornerRadius={24}
        testID="menu"
      >
        <Text>Card</Text>
      </ContextMenu>
    );

    const menu = screen.getByTestId('menu');
    expect(menu.type).toBe('RNContextMenu');
    expect(menu.props.actions).toEqual(actions);
    expect(menu.props.androidTrigger).toBe('tap');
    expect(menu.props.iosPreviewCornerRadius).toBe(24);
    expect(screen.getByText('Card')).toBeTruthy();
  });

  it('passes the picked id to onActionPress', () => {
    const onActionPress = jest.fn();
    const screen = render(
      <ContextMenu
        actions={actions}
        onActionPress={onActionPress}
        testID="menu"
      />
    );

    fireEvent(screen.getByTestId('menu'), 'onActionPress', {
      nativeEvent: { id: 'delete' },
    });

    expect(onActionPress).toHaveBeenCalledWith({
      nativeEvent: { id: 'delete' },
    });
  });
});
