// ============================================================================
// PROJECT MODEL - MongoDB schema for projects
// ============================================================================

import mongoose, { Schema, Document } from 'mongoose';

export interface IProject extends Document {
  userId: string;
  title: string;
  description?: string;
  type: 'app' | 'game' | 'website' | 'design';
  thumbnail?: string;
  data: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
  collaborators: Array<{ userId: string; role: 'editor' | 'viewer' }>;
  isPublic: boolean;
  tags: string[];
  status: 'draft' | 'published' | 'archived';
  viewCount: number;
  likes: string[];
  comments: Array<{
    userId: string;
    text: string;
    createdAt: Date;
  }>;
}

const projectSchema = new Schema<IProject>(
  {
    userId: {
      type: String,
      required: true,
      index: true,
    },
    title: {
      type: String,
      required: true,
      trim: true,
    },
    description: String,
    type: {
      type: String,
      enum: ['app', 'game', 'website', 'design'],
      required: true,
    },
    thumbnail: String,
    data: Schema.Types.Mixed,
    collaborators: [
      {
        userId: String,
        role: { type: String, enum: ['editor', 'viewer'] },
      },
    ],
    isPublic: { type: Boolean, default: false },
    tags: [String],
    status: {
      type: String,
      enum: ['draft', 'published', 'archived'],
      default: 'draft',
    },
    viewCount: { type: Number, default: 0 },
    likes: [String],
    comments: [
      {
        userId: String,
        text: String,
        createdAt: { type: Date, default: Date.now },
      },
    ],
  },
  { timestamps: true }
);

export const Project = mongoose.model<IProject>('Project', projectSchema);
