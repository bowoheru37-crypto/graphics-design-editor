// ============================================================================
// USER MODEL - MongoDB schema for users
// ============================================================================

import mongoose, { Schema, Document } from 'mongoose';
import bcrypt from 'bcrypt';

export interface IUser extends Document {
  email: string;
  password: string;
  displayName: string;
  avatar?: string;
  bio?: string;
  subscription: 'free' | 'pro' | 'enterprise';
  verificationStatus: 'pending' | 'verified';
  verificationToken?: string;
  verificationExpires?: Date;
  createdAt: Date;
  updatedAt: Date;
  lastLogin?: Date;
  profile: {
    website?: string;
    location?: string;
    socialLinks?: Record<string, string>;
  };
  settings: {
    theme: 'light' | 'dark' | 'auto';
    language: string;
    emailNotifications: boolean;
    twoFactorEnabled: boolean;
  };
  comparePassword(password: string): Promise<boolean>;
}

const userSchema = new Schema<IUser>(
  {
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    password: {
      type: String,
      required: true,
      select: false,
    },
    displayName: {
      type: String,
      required: true,
      trim: true,
    },
    avatar: String,
    bio: String,
    subscription: {
      type: String,
      enum: ['free', 'pro', 'enterprise'],
      default: 'free',
    },
    verificationStatus: {
      type: String,
      enum: ['pending', 'verified'],
      default: 'pending',
    },
    verificationToken: String,
    verificationExpires: Date,
    lastLogin: Date,
    profile: {
      website: String,
      location: String,
      socialLinks: Schema.Types.Mixed,
    },
    settings: {
      theme: { type: String, enum: ['light', 'dark', 'auto'], default: 'auto' },
      language: { type: String, default: 'en' },
      emailNotifications: { type: Boolean, default: true },
      twoFactorEnabled: { type: Boolean, default: false },
    },
  },
  { timestamps: true }
);

// Hash password before saving
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();

  try {
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error as Error);
  }
});

// Method to compare passwords
userSchema.methods.comparePassword = async function (
  password: string
): Promise<boolean> {
  return bcrypt.compare(password, this.password);
};

export const User = mongoose.model<IUser>('User', userSchema);
