import { StyleSheet } from 'react-native';

export const createStyles = (dark: boolean) => {
  const label = dark ? '#FFFFFF' : '#000000';
  const surface = dark ? '#1C1C1E' : '#FFFFFF';

  return StyleSheet.create({
    bar: { alignItems: 'center', flexDirection: 'row', paddingHorizontal: 16 },
    button: { alignItems: 'center', flex: 1, justifyContent: 'center' },
    card: { aspectRatio: 1, width: '48%' },
    grid: {
      flexDirection: 'row',
      flexWrap: 'wrap',
      justifyContent: 'space-between',
      padding: 12,
      rowGap: 12,
    },
    label: { color: label, fontSize: 17, fontWeight: '600' },
    list: { gap: 12, paddingBottom: 48, paddingHorizontal: 12 },
    more: {
      alignItems: 'center',
      height: 44,
      justifyContent: 'center',
      width: 44,
    },
    moreText: { color: label, fontSize: 24 },
    row: { justifyContent: 'space-between' },
    screen: {
      backgroundColor: dark ? '#000000' : '#F2F2F2',
      flex: 1,
      paddingTop: 64,
    },
    status: { color: label, flex: 1 },
    surface: {
      backgroundColor: surface,
      borderColor: '#88888844',
      borderRadius: 24,
      borderWidth: 1,
      flex: 1,
    },
    toggle: { paddingHorizontal: 8, paddingVertical: 12 },
    toggleText: { color: label },
    toggles: { flexDirection: 'row', justifyContent: 'space-between' },
  });
};
