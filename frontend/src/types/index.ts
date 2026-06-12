// ============================================================================
// CORE TYPE DEFINITIONS
// ============================================================================

export interface User {
  id: string;
  email: string;
  displayName: string;
  avatar?: string;
  createdAt: Date;
  updatedAt: Date;
  subscription: 'free' | 'pro' | 'enterprise';
  verificationStatus: 'pending' | 'verified';
}

export interface Project {
  id: string;
  userId: string;
  title: string;
  description?: string;
  type: 'app' | 'game' | 'website' | 'design';
  thumbnail?: string;
  data: ProjectData;
  createdAt: Date;
  updatedAt: Date;
  collaborators: string[];
  isPublic: boolean;
}

export interface ProjectData {
  canvas: CanvasState;
  layers: Layer[];
  assets: Asset[];
  settings: ProjectSettings;
}

export interface CanvasState {
  width: number;
  height: number;
  zoom: number;
  panX: number;
  panY: number;
  rotation: number;
  gridSize: number;
  showGrid: boolean;
  showGuides: boolean;
  backgroundColor: string;
}

export interface Layer {
  id: string;
  name: string;
  type: LayerType;
  visible: boolean;
  locked: boolean;
  opacity: number;
  blendMode: string;
  x: number;
  y: number;
  width: number;
  height: number;
  rotation: number;
  scaleX: number;
  scaleY: number;
  properties: Record<string, any>;
  children: Layer[];
  parentId?: string;
}

export type LayerType =
  | 'text'
  | 'shape'
  | 'image'
  | 'svg'
  | 'video'
  | 'audio'
  | 'button'
  | 'component'
  | 'animation'
  | 'particle'
  | 'camera'
  | 'tilemap'
  | 'sprite'
  | '3d'
  | 'group';

export interface Asset {
  id: string;
  name: string;
  type: AssetType;
  category: string;
  url: string;
  thumbnail?: string;
  size: number;
  createdAt: Date;
  tags: string[];
  metadata: Record<string, any>;
}

export type AssetType =
  | 'image'
  | 'icon'
  | 'vector'
  | 'font'
  | 'audio'
  | 'video'
  | 'template'
  | 'script'
  | 'plugin'
  | 'shader'
  | 'material'
  | 'animation'
  | 'model';

export interface ProjectSettings {
  name: string;
  version: string;
  author: string;
  description: string;
  tags: string[];
  resolution: 'mobile' | 'tablet' | 'desktop' | 'custom';
  customWidth?: number;
  customHeight?: number;
}

export interface MarketplaceItem {
  id: string;
  title: string;
  description: string;
  thumbnail: string;
  category: string;
  type: 'template' | 'asset' | 'plugin' | 'theme';
  price: number;
  rating: number;
  downloads: number;
  creator: Creator;
  createdAt: Date;
  updatedAt: Date;
}

export interface Creator {
  id: string;
  name: string;
  avatar: string;
  verified: boolean;
  followers: number;
}

export interface Workspace {
  id: string;
  userId: string;
  name: string;
  description?: string;
  projects: string[];
  collaborators: WorkspaceCollaborator[];
  settings: WorkspaceSettings;
  createdAt: Date;
  updatedAt: Date;
}

export interface WorkspaceCollaborator {
  userId: string;
  role: 'owner' | 'admin' | 'editor' | 'viewer';
  joinedAt: Date;
}

export interface WorkspaceSettings {
  theme: 'light' | 'dark' | 'auto';
  language: string;
  autoSave: boolean;
  autoSaveInterval: number;
  defaultCanvasSize: 'mobile' | 'tablet' | 'desktop';
}

export interface AIPrompt {
  id: string;
  title: string;
  description: string;
  category: 'code' | 'design' | 'ui' | 'game' | 'content';
  template: string;
  variables: PromptVariable[];
  createdAt: Date;
}

export interface PromptVariable {
  name: string;
  type: 'text' | 'number' | 'select' | 'boolean';
  label: string;
  required: boolean;
  options?: string[];
}

export interface Template {
  id: string;
  title: string;
  description: string;
  category: string;
  thumbnail: string;
  preview: string;
  type: 'app' | 'game' | 'website' | 'design';
  data: ProjectData;
  creator: Creator;
  downloads: number;
  rating: number;
  price: number;
  createdAt: Date;
}

export interface Comment {
  id: string;
  projectId: string;
  userId: string;
  text: string;
  createdAt: Date;
  updatedAt: Date;
  replies: Comment[];
  likes: number;
}

export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  actionUrl?: string;
  read: boolean;
  createdAt: Date;
}

export type NotificationType =
  | 'comment'
  | 'share'
  | 'mention'
  | 'system'
  | 'update'
  | 'collaboration';

export interface APIResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}
