import { useNavigation } from '@react-navigation/native';
import { useLayoutEffect, useState } from 'react';
import {
  Platform,
  ScrollView,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';
import Animated, { FadeOut, LinearTransition } from 'react-native-reanimated';
import { ContextMenu } from '@27appstudio/react-native-system-menu';

import { actions } from './Bench';
import { createStyles } from './styles';

const AnimatedContextMenu = Animated.createAnimatedComponent(ContextMenu);

// How Splitify uses the menu: animated grid cards, and a menu in the native header.
export function StackScreen() {
  const styles = createStyles(useColorScheme() === 'dark');
  const navigation = useNavigation();
  const [cards, setCards] = useState(['A', 'B', 'C', 'D', 'E']);
  const [presses, setPresses] = useState(0);
  const [last, setLast] = useState('none');

  useLayoutEffect(() => {
    if (Platform.OS !== 'android') return;
    navigation.setOptions({
      headerRight: () => (
        <ContextMenu
          accessibilityLabel="Stack actions"
          accessibilityRole="button"
          accessible
          actions={actions}
          androidDestructiveColor="#FF3B30"
          androidTrigger="tap"
          onActionPress={(e) => setLast(`header:${e.nativeEvent.id}`)}
          style={styles.more}
          testID="stack-header-menu"
        >
          <Text style={styles.moreText}>⋮</Text>
        </ContextMenu>
      ),
    });
  }, [navigation, styles]);

  return (
    <ScrollView contentInsetAdjustmentBehavior="automatic">
      <Text style={styles.label} testID="stack-status">
        {`stack presses=${presses} last=${last}`}
      </Text>
      <View style={styles.grid}>
        {cards.map((name) => (
          <AnimatedContextMenu
            actions={actions}
            androidDestructiveColor="#FF3B30"
            exiting={FadeOut.duration(200)}
            iosPreviewCornerRadius={24}
            key={name}
            layout={LinearTransition.duration(200)}
            onActionPress={(e) => {
              setLast(`${name}:${e.nativeEvent.id}`);
              if (e.nativeEvent.id === 'delete') {
                setCards((all) => all.filter((item) => item !== name));
              }
            }}
            style={styles.card}
            testID={`animated-menu-${name}`}
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
                style={styles.button}
                testID={`animated-button-${name}`}
              >
                <Text style={styles.label}>{`Animated ${name}`}</Text>
              </TouchableOpacity>
            </View>
          </AnimatedContextMenu>
        ))}
      </View>
    </ScrollView>
  );
}
