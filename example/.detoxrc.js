// Devices come from the environment, so a run only touches the ones it was given.
module.exports = {
  testRunner: {
    args: { $0: 'jest', config: 'e2e/jest.config.js' },
    jest: { setupTimeout: 180000 },
  },
  apps: {
    ios: {
      type: 'ios.app',
      binaryPath:
        'ios/build/Build/Products/Debug-iphonesimulator/ContextMenuExample.app',
      launchArgs: { RCT_jsLocation: process.env.E2E_METRO ?? 'localhost:8081' },
    },
    android: {
      type: 'android.apk',
      binaryPath: 'android/app/build/outputs/apk/debug/app-debug.apk',
    },
  },
  devices: {
    simulator: {
      type: 'ios.simulator',
      device: { id: process.env.E2E_UDID ?? 'set E2E_UDID' },
    },
    attached: {
      type: 'android.attached',
      device: { adbName: process.env.E2E_SERIAL ?? 'set E2E_SERIAL' },
    },
  },
  configurations: {
    ios: { device: 'simulator', app: 'ios' },
    android: { device: 'attached', app: 'android' },
  },
};
