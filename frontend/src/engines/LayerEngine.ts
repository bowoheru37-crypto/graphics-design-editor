// ============================================================================
// LAYER ENGINE - Advanced layer system management
// ============================================================================

import { Layer, LayerType } from '../types';
import { v4 as uuidv4 } from 'uuid';

export interface LayerEngineConfig {
  maxLayers?: number;
  enableHierarchy?: boolean;
  enableGrouping?: boolean;
}

export class LayerEngine {
  private layers: Map<string, Layer> = new Map();
  private layerOrder: string[] = [];
  private config: Required<LayerEngineConfig>;

  constructor(config: LayerEngineConfig = {}) {
    this.config = {
      maxLayers: config.maxLayers || 1000,
      enableHierarchy: config.enableHierarchy ?? true,
      enableGrouping: config.enableGrouping ?? true,
    };
  }

  /**
   * Create a new layer
   */
  createLayer(
    name: string,
    type: LayerType,
    properties?: Record<string, any>
  ): Layer {
    if (this.layers.size >= this.config.maxLayers) {
      throw new Error(`Maximum layers (${this.config.maxLayers}) reached`);
    }

    const layer: Layer = {
      id: uuidv4(),
      name,
      type,
      visible: true,
      locked: false,
      opacity: 1,
      blendMode: 'normal',
      x: 0,
      y: 0,
      width: 100,
      height: 100,
      rotation: 0,
      scaleX: 1,
      scaleY: 1,
      properties: properties || {},
      children: [],
    };

    this.layers.set(layer.id, layer);
    this.layerOrder.push(layer.id);

    return layer;
  }

  /**
   * Update layer properties
   */
  updateLayer(id: string, updates: Partial<Layer>) {
    const layer = this.layers.get(id);
    if (!layer) throw new Error(`Layer ${id} not found`);

    Object.assign(layer, updates);
  }

  /**
   * Delete layer
   */
  deleteLayer(id: string) {
    this.layers.delete(id);
    this.layerOrder = this.layerOrder.filter((lid) => lid !== id);
  }

  /**
   * Get layer by ID
   */
  getLayer(id: string): Layer | undefined {
    return this.layers.get(id);
  }

  /**
   * Get all layers in order (top to bottom)
   */
  getAllLayers(): Layer[] {
    return this.layerOrder.map((id) => this.layers.get(id)!).filter(Boolean);
  }

  /**
   * Add layer to group
   */
  addToGroup(layerId: string, groupId: string) {
    if (!this.config.enableGrouping) throw new Error('Grouping disabled');

    const group = this.layers.get(groupId);
    if (!group) throw new Error(`Group ${groupId} not found`);
    if (group.type !== 'group') throw new Error('Target is not a group');

    const layer = this.layers.get(layerId);
    if (!layer) throw new Error(`Layer ${layerId} not found`);

    if (!group.children.some((l) => l.id === layerId)) {
      group.children.push(layer);
      layer.parentId = groupId;
    }
  }

  /**
   * Remove layer from group
   */
  removeFromGroup(layerId: string) {
    const layer = this.layers.get(layerId);
    if (!layer || !layer.parentId) return;

    const parent = this.layers.get(layer.parentId);
    if (parent) {
      parent.children = parent.children.filter((l) => l.id !== layerId);
    }
    layer.parentId = undefined;
  }

  /**
   * Create group
   */
  createGroup(name: string): Layer {
    return this.createLayer(name, 'group');
  }

  /**
   * Duplicate layer
   */
  duplicateLayer(id: string): Layer {
    const source = this.layers.get(id);
    if (!source) throw new Error(`Layer ${id} not found`);

    const duplicate = this.createLayer(`${source.name} copy`, source.type);
    Object.assign(duplicate, {
      opacity: source.opacity,
      blendMode: source.blendMode,
      x: source.x,
      y: source.y,
      width: source.width,
      height: source.height,
      rotation: source.rotation,
      scaleX: source.scaleX,
      scaleY: source.scaleY,
      properties: { ...source.properties },
    });

    return duplicate;
  }

  /**
   * Move layer in hierarchy
   */
  moveLayerIndex(id: string, direction: 'up' | 'down') {
    const index = this.layerOrder.indexOf(id);
    if (index === -1) return;

    if (direction === 'up' && index < this.layerOrder.length - 1) {
      [this.layerOrder[index], this.layerOrder[index + 1]] = [
        this.layerOrder[index + 1],
        this.layerOrder[index],
      ];
    } else if (direction === 'down' && index > 0) {
      [this.layerOrder[index], this.layerOrder[index - 1]] = [
        this.layerOrder[index - 1],
        this.layerOrder[index],
      ];
    }
  }

  /**
   * Toggle layer visibility
   */
  toggleVisibility(id: string) {
    const layer = this.layers.get(id);
    if (layer) {
      layer.visible = !layer.visible;
    }
  }

  /**
   * Toggle layer lock
   */
  toggleLock(id: string) {
    const layer = this.layers.get(id);
    if (layer) {
      layer.locked = !layer.locked;
    }
  }

  /**
   * Clear all layers
   */
  clear() {
    this.layers.clear();
    this.layerOrder = [];
  }
}
