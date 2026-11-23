import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { Bench } from './Bench';
import { StackScreen } from './StackScreen';

const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Bench" options={{ headerShown: false }}>
          {({ navigation }) => (
            <Bench onOpenStack={() => navigation.navigate('Animated')} />
          )}
        </Stack.Screen>
        <Stack.Screen component={StackScreen} name="Animated" />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
