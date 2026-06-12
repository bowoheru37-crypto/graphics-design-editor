// ============================================================================
// GESTURE ENGINE - Multi-touch gesture handling
// ============================================================================

import Hammer from 'hammerjs';

export interface GestureConfig {
  element: HTMLElement;
  onPan?: (event: { deltaX: number; deltaY: number; velocity: number }) => void;
  onZoom?: (event: { scale: number; deltaScale: number }) => void;
  onRotate?: (event: { rotation: number; deltaRotation: number }) => void;
  onTap?: (event: { x: number; y: number }) => void;
  onDoubleTap?: (event: { x: number; y: number }) => void;
  onLongPress?: (event: { x: number; y: number }) => void;
}

export class GestureEngine {
  private manager: HammerManager;
  private pan: Hammer.Pan;
  private pinch: Hammer.Pinch;
  private rotate: Hammer.Rotate;
  private tap: Hammer.Tap;
  private doubleTap: Hammer.Tap;
  private press: Hammer.Press;

  constructor(config: GestureConfig) {
    // Initialize Hammer.js
    this.manager = new Hammer.Manager(config.element);

    // Pan gesture (dragging)
    this.pan = new Hammer.Pan({ threshold: 0, pointers: 0 });
    this.manager.add(this.pan);
    this.pan.on('panstart pan panend', (event) => {
      if (config.onPan) {
        config.onPan({
          deltaX: event.deltaX,
          deltaY: event.deltaY,
          velocity: event.velocity,
        });
      }
    });

    // Zoom gesture (pinch)
    this.pinch = new Hammer.Pinch();
    this.manager.add(this.pinch);
    this.pinch.on('pinch pinchin pinchout', (event) => {
      if (config.onZoom) {
        config.onZoom({
          scale: event.scale,
          deltaScale: event.scale - (event.previousScale || 1),
        });
      }
    });

    // Rotate gesture
    this.rotate = new Hammer.Rotate();
    this.manager.add(this.rotate);
    this.rotate.on('rotate rotatestart rotateend', (event) => {
      if (config.onRotate) {
        config.onRotate({
          rotation: event.rotation,
          deltaRotation: event.deltaRotation,
        });
      }
    });

    // Single tap
    this.tap = new Hammer.Tap();
    this.manager.add(this.tap);
    this.tap.on('tap', (event) => {
      if (config.onTap) {
        config.onTap({
          x: event.center.x,
          y: event.center.y,
        });
      }
    });

    // Double tap
    this.doubleTap = new Hammer.Tap({ event: 'doubletap', taps: 2 });
    this.manager.add(this.doubleTap);
    this.doubleTap.on('doubletap', (event) => {
      if (config.onDoubleTap) {
        config.onDoubleTap({
          x: event.center.x,
          y: event.center.y,
        });
      }
    });

    // Long press
    this.press = new Hammer.Press({ time: 500 });
    this.manager.add(this.press);
    this.press.on('press', (event) => {
      if (config.onLongPress) {
        config.onLongPress({
          x: event.center.x,
          y: event.center.y,
        });
      }
    });
  }

  /**
   * Enable/disable specific gestures
   */
  setGestureEnabled(gesture: string, enabled: boolean) {
    const gestureMap: Record<string, Hammer.Recognizer> = {
      pan: this.pan,
      zoom: this.pinch,
      rotate: this.rotate,
      tap: this.tap,
      doubleTap: this.doubleTap,
      press: this.press,
    };

    if (gestureMap[gesture]) {
      gestureMap[gesture].set({ enable: enabled });
    }
  }

  /**
   * Dispose gesture engine
   */
  dispose() {
    this.manager.destroy();
  }
}
