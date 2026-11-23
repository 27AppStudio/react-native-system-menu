/* global describe, beforeEach, it */
const { by, device, element, expect, waitFor } = require('detox');

const seeText = (text) =>
  waitFor(element(by.text(text)))
    .toBeVisible()
    .withTimeout(8000);

// The way Splitify's page objects drive the menus: long-press the wrapper, tap by text.
describe('context menu', () => {
  beforeEach(async () => {
    await device.launchApp({ newInstance: true });
    await waitFor(element(by.id('status')))
      .toBeVisible()
      .withTimeout(90000);
  });

  it('taps the child', async () => {
    await element(by.id('card-button')).atIndex(0).tap();
    await seeText('presses=1 last=none');
  });

  it('long-presses the wrapper and picks an item', async () => {
    await element(by.id('card-menu')).atIndex(0).longPress();
    await seeText('Edit');
    await element(by.text('Edit')).tap();
    await waitFor(element(by.id('status')))
      .toHaveText('presses=0 last=Card 1:edit')
      .withTimeout(8000);
  });

  it('long-presses the child without pressing it', async () => {
    await element(by.id('card-button')).atIndex(0).longPress();
    await seeText('Edit');
    await element(by.text('Edit')).tap();
    await seeText('presses=0 last=Card 1:edit');
  });

  it('deletes an animated card', async () => {
    await element(by.id('open-stack')).tap();
    await seeText('Animated A');
    await element(by.id('animated-menu-A')).longPress();
    await seeText('Delete');
    await element(by.text('Delete')).tap();
    await waitFor(element(by.text('Animated A')))
      .not.toExist()
      .withTimeout(8000);
    await expect(element(by.id('stack-status'))).toHaveText(
      'stack presses=0 last=A:delete'
    );
  });

  // A touch handler left disabled would freeze the screen: each ends on a tap that must count.
  it('keeps taps working when the card unmounts under the finger', async () => {
    await element(by.text('unmount off')).tap();
    await seeText('unmount on');
    await element(by.id('card-button')).atIndex(0).longPress(3000);
    await waitFor(element(by.text('Card 1')))
      .not.toExist()
      .withTimeout(8000);
    await seeText('presses=0 last=none');
    await element(by.id('card-button')).atIndex(1).tap();
    await seeText('presses=1 last=none');
  });

  it('keeps taps working after two quick menus', async () => {
    await element(by.id('card-menu')).atIndex(0).longPress();
    await seeText('Edit');
    await element(by.text('Edit')).tap();
    await element(by.id('card-menu')).atIndex(1).longPress();
    await seeText('Invite');
    await element(by.text('Invite')).tap();
    await seeText('presses=0 last=Card 2:invite');
    await element(by.id('card-button')).atIndex(0).tap();
    await seeText('presses=1 last=Card 2:invite');
  });

  it('keeps taps working after dragging off a card', async () => {
    await element(by.text('Card 3')).swipe('right', 'slow', 0.7);
    await element(by.id('card-button')).atIndex(0).tap();
    await seeText('presses=1 last=none');
  });

  it('keeps taps working after a menu and navigating back', async () => {
    await element(by.id('open-stack')).tap();
    await seeText('Animated A');
    await element(by.id('animated-menu-B')).longPress();
    await seeText('Edit');
    await element(by.text('Edit')).tap();
    await seeText('stack presses=0 last=B:edit');
    if (device.getPlatform() === 'android') await device.pressBack();
    else await element(by.text('Bench')).atIndex(0).tap();
    await waitFor(element(by.id('status')))
      .toBeVisible()
      .withTimeout(8000);
    await element(by.id('card-button')).atIndex(0).tap();
    await seeText('presses=1 last=none');
  });

  // A press or a menu, never both.
  const steps = Array.from({ length: 13 }, (_, i) => 150 + 25 * i);
  for (const ms of [100, ...steps, 500, 650, 800]) {
    it(`a ${ms}ms press either taps or opens the menu`, async () => {
      await element(by.id('card-button')).atIndex(0).longPress(ms);
      let outcome = 'tap';
      try {
        await waitFor(element(by.text('Edit')))
          .toBeVisible()
          .withTimeout(2500);
        outcome = 'menu';
      } catch {
        // No menu: it has to have been a press.
      }
      console.log(`threshold ${ms}ms=${outcome}`);
      if (outcome === 'menu') {
        await element(by.text('Edit')).tap();
        await seeText('presses=0 last=Card 1:edit');
      } else {
        // iOS: between the press deadline and UIKit's commit neither happens, as in Splitify today.
        const ios = device.getPlatform() === 'ios';
        // On the deadline itself either may win.
        if (ios && (ms === 250 || ms === 275)) return;
        const deadZone = ios && ms > 275;
        await seeText(deadZone ? 'presses=0 last=none' : 'presses=1 last=none');
      }
    });
  }

  const androidOnly =
    process.env.E2E_SERIAL && !process.env.E2E_UDID ? it : it.skip;
  androidOnly('opens the header menu on a tap', async () => {
    await element(by.id('open-stack')).tap();
    await seeText('Animated A');
    await element(by.id('stack-header-menu')).tap();
    await seeText('Invite');
    await element(by.text('Invite')).tap();
    await seeText('stack presses=0 last=header:invite');
  });
});
