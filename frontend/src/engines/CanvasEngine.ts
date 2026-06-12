// ============================================================================
// CANVAS ENGINE - Core rendering and transformation
// ============================================================================

import * as BABYLON from 'babylon.js';

export interface CanvasEngineConfig {
  canvas: HTMLCanvasElement;
  width: number;
  height: number;
  backgroundColor?: string;
  enablePhysics?: boolean;
  fps?: number;
}

export class CanvasEngine {
  private engine: BABYLON.Engine;
  private scene: BABYLON.Scene;
  private camera: BABYLON.UniversalCamera;
  private light: BABYLON.Light;
  private width: number;
  private height: number;

  constructor(config: CanvasEngineConfig) {
    this.width = config.width;
    this.height = config.height;

    // Initialize Babylon.js
    this.engine = new BABYLON.Engine(config.canvas, true, {
      preserveDrawingBuffer: true,
      stencil: true,
      depth: true,
    });

    this.scene = new BABYLON.Scene(this.engine);
    this.scene.clearColor = new BABYLON.Color3.FromHexString(
      config.backgroundColor || '#ffffff'
    );

    // Setup camera
    this.camera = new BABYLON.UniversalCamera(
      'camera',
      new BABYLON.Vector3(0, 0, -50)
    );
    this.camera.attachControl(config.canvas, true);
    this.camera.inertia = 0.7;

    // Setup lighting
    this.light = new BABYLON.HemisphericLight(
      'light',
      new BABYLON.Vector3(1, 1, 1),
      this.scene
    );
    this.light.intensity = 1.5;

    // Setup physics if enabled
    if (config.enablePhysics) {
      const gravityVector = new BABYLON.Vector3(0, -9.81, 0);
      const physicsPlugin = new BABYLON.CannonJSPlugin();
      this.scene.enablePhysics(gravityVector, physicsPlugin);
    }

    // Render loop
    this.engine.runRenderLoop(() => {
      this.scene.render();
    });

    // Handle window resize
    window.addEventListener('resize', () => {
      this.engine.resize();
    });
  }

  /**
   * Create a shape layer
   */
  createShape(
    type: 'rect' | 'circle' | 'triangle',
    x: number,
    y: number,
    width: number,
    height: number,
    color: string
  ) {
    let mesh: BABYLON.Mesh;

    switch (type) {
      case 'rect':
        mesh = BABYLON.MeshBuilder.CreateBox(
          `rect_${Date.now()}`,
          { width, height, depth: 0.1 },
          this.scene
        );
        break;
      case 'circle':
        mesh = BABYLON.MeshBuilder.CreateCylinder(
          `circle_${Date.now()}`,
          { diameter: width, height: 0.1, tessellation: 64 },
          this.scene
        );
        break;
      case 'triangle':
        mesh = this.createTriangleMesh(width, height);
        break;
    }

    mesh.position.x = x;
    mesh.position.y = y;

    const material = new BABYLON.StandardMaterial(
      `material_${Date.now()}`,
      this.scene
    );
    material.diffuse = BABYLON.Color3.FromHexString(color);
    mesh.material = material;

    return mesh;
  }

  /**
   * Create text mesh
   */
  createText(
    text: string,
    x: number,
    y: number,
    fontSize: number,
    color: string
  ) {
    // Using dynamic texture for text rendering
    const dynamicTexture = new BABYLON.DynamicTexture(
      `text_${Date.now()}`,
      512,
      this.scene
    );
    const ctx = dynamicTexture.getContext();

    ctx.fillStyle = color;
    ctx.font = `${fontSize}px Arial`;
    ctx.fillText(text, 10, fontSize);

    dynamicTexture.update();

    const plane = BABYLON.MeshBuilder.CreatePlane(
      `text_plane_${Date.now()}`,
      { width: 10, height: 2 },
      this.scene
    );
    plane.position.x = x;
    plane.position.y = y;

    const material = new BABYLON.StandardMaterial(
      `text_material_${Date.now()}`,
      this.scene
    );
    material.emissiveTexture = dynamicTexture;
    plane.material = material;

    return plane;
  }

  /**
   * Create image mesh
   */
  createImage(url: string, x: number, y: number, width: number, height: number) {
    const plane = BABYLON.MeshBuilder.CreatePlane(
      `image_${Date.now()}`,
      { width, height },
      this.scene
    );
    plane.position.x = x;
    plane.position.y = y;

    const material = new BABYLON.StandardMaterial(
      `image_material_${Date.now()}`,
      this.scene
    );
    material.emissiveTexture = new BABYLON.Texture(url, this.scene);
    plane.material = material;

    return plane;
  }

  /**
   * Apply animation to mesh
   */
  animateMesh(
    mesh: BABYLON.Mesh,
    property: 'position' | 'rotation' | 'scaling',
    targetValue: BABYLON.Vector3,
    duration: number
  ) {
    const animation = new BABYLON.Animation(
      `${property}_animation`,
      property,
      30,
      BABYLON.Animation.ANIMATIONTYPE_VECTOR3,
      BABYLON.Animation.ANIMATIONLOOPMODE_CONSTANT
    );

    const keyFrames = [
      { frame: 0, value: mesh[property as any] },
      { frame: duration, value: targetValue },
    ];

    animation.setKeys(keyFrames);
    mesh.animations.push(animation);

    this.scene.beginAnimation(mesh, 0, duration, false);
  }

  /**
   * Set camera zoom
   */
  setZoom(level: number) {
    this.camera.position.z = -50 / level;
  }

  /**
   * Pan camera
   */
  pan(deltaX: number, deltaY: number) {
    this.camera.position.x -= deltaX * 0.01;
    this.camera.position.y += deltaY * 0.01;
  }

  /**
   * Rotate camera
   */
  rotate(angle: number) {
    this.camera.rotation.z += (angle * Math.PI) / 180;
  }

  /**
   * Get scene
   */
  getScene(): BABYLON.Scene {
    return this.scene;
  }

  /**
   * Dispose engine
   */
  dispose() {
    this.engine.dispose();
    this.scene.dispose();
  }

  // Helper method
  private createTriangleMesh(width: number, height: number) {
    const mesh = new BABYLON.Mesh(`triangle_${Date.now()}`, this.scene);
    const vertexData = new BABYLON.VertexData();

    const positions = [
      -width / 2,
      -height / 2,
      0,
      width / 2,
      -height / 2,
      0,
      0,
      height / 2,
      0,
    ];

    const indices = [0, 1, 2];

    vertexData.positions = positions;
    vertexData.indices = indices;
    vertexData.applyToMesh(mesh);

    return mesh;
  }
}
