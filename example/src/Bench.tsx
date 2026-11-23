import { useState } from 'react';
import {
  Appearance,
  FlatList,
  Modal,
  Platform,
  Pressable,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';
import { ContextMenu } from '@27appstudio/react-native-system-menu';
import type { ContextMenuAction } from '@27appstudio/react-native-system-menu';

import { createStyles } from './styles';

export const actions: ContextMenuAction[] = [
  {
    id: 'invite',
    title: 'Invite',
    systemImage: 'qrcode',
    androidIcon: 'ic_invite',
  },
  { id: 'edit', title: 'Edit', systemImage: 'pencil', androidIcon: 'ic_edit' },
  {
    id: 'delete',
    title: 'Delete',
    systemImage: 'trash',
    androidIcon: 'ic_delete',
    destructive: true,
  },
];

const initialCards = () =>
  Array.from({ length: 24 }, (_, i) => `Card ${i + 1}`);

// The spike's test bench: every event is counted, so a press that leaks through shows.
export function Bench({ onOpenStack }: { onOpenStack: () => void }) {
  const dark = useColorScheme() === 'dark';
  const styles = createStyles(dark);
  const [cards, setCards] = useState(initialCards);
  const [presses, setPresses] = useState(0);
  const [last, setLast] = useState('none');
  const [masked, setMasked] = useState(true);
  const [removeSoon, setRemoveSoon] = useState(false);
  const [modal, setModal] = useState(false);

  const header = (
    <ContextMenu
      accessibilityLabel="More actions"
      accessibilityRole="button"
      accessible
      actions={actions}
      androidDestructiveColor="#FF3B30"
      androidTrigger="tap"
      onActionPress={(e) => setLast(`header:${e.nativeEvent.id}`)}
      style={styles.more}
      testID="header-menu"
    >
      <Text style={styles.moreText}>⋮</Text>
    </ContextMenu>
  );

  const card = (name: string) => (
    <ContextMenu
      actions={actions}
      androidDestructiveColor="#FF3B30"
      iosPreviewCornerRadius={masked ? 24 : 0}
      key={name}
      onActionPress={(e) => {
        setLast(`${name}:${e.nativeEvent.id}`);
        if (e.nativeEvent.id === 'delete') {
          setCards((all) => all.filter((item) => item !== name));
        }
      }}
      style={styles.card}
      testID="card-menu"
    >
      <View style={styles.surface}>
        <TouchableOpacity
          {...(Platform.OS === 'ios' && {
            // See ContextMenu: keeps the long press from pressing, without a blink.
            activeOpacity: 1,
            delayLongPress: 250,
            onLongPress: () => {},
          })}
          onPress={() => setPresses((n) => n + 1)}
          onPressIn={() => {
            // Unmounts the card during the press or with its menu open.
            if (removeSoon) {
              setTimeout(
                () => setCards((all) => all.filter((item) => item !== name)),
                1500
              );
            }
          }}
          style={styles.button}
          testID="card-button"
        >
          <Text style={styles.label}>{name}</Text>
        </TouchableOpacity>
      </View>
    </ContextMenu>
  );

  const toggles = [
    ['theme', () => Appearance.setColorScheme(dark ? 'light' : 'dark')],
    [masked ? 'mask on' : 'mask off', () => setMasked((v) => !v)],
    [removeSoon ? 'unmount on' : 'unmount off', () => setRemoveSoon((v) => !v)],
    ['modal', () => setModal(true)],
  ] as const;

  return (
    <View style={styles.screen}>
      <View style={styles.bar}>
        <Text style={styles.status} testID="status">
          {`presses=${presses} last=${last}`}
        </Text>
        <Pressable onPress={onOpenStack} testID="open-stack">
          <Text style={styles.toggleText}>stack</Text>
        </Pressable>
        {header}
      </View>
      <View style={styles.toggles}>
        {toggles.map(([label, onPress]) => (
          <Pressable key={label} onPress={onPress} style={styles.toggle}>
            <Text style={styles.toggleText}>{label}</Text>
          </Pressable>
        ))}
      </View>
      <FlatList
        columnWrapperStyle={styles.row}
        contentContainerStyle={styles.list}
        data={cards}
        keyExtractor={(item) => item}
        numColumns={2}
        renderItem={({ item }) => card(item)}
      />
      <Modal onRequestClose={() => setModal(false)} visible={modal}>
        <View style={styles.screen}>
          <View style={styles.row}>{card('Modal card')}</View>
        </View>
      </Modal>
    </View>
  );
}
