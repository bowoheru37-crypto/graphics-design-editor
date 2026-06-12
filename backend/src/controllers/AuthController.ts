// ============================================================================
// AUTH CONTROLLER - Authentication logic
// ============================================================================

import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { User } from '../models/User';
import { v4 as uuidv4 } from 'uuid';

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
const JWT_EXPIRE = '7d';

export class AuthController {
  /**
   * Register new user
   */
  static async register(req: Request, res: Response, next: NextFunction) {
    try {
      const { email, password, displayName } = req.body;

      // Check if user exists
      const existingUser = await User.findOne({ email });
      if (existingUser) {
        return res.status(409).json({ error: 'User already exists' });
      }

      // Create user
      const verificationToken = uuidv4();
      const user = new User({
        email,
        password,
        displayName,
        verificationToken,
        verificationExpires: new Date(Date.now() + 24 * 60 * 60 * 1000),
      });

      await user.save();

      // Generate token
      const token = jwt.sign({ userId: user._id }, JWT_SECRET, {
        expiresIn: JWT_EXPIRE,
      });

      res.status(201).json({
        success: true,
        data: {
          user: {
            id: user._id,
            email: user.email,
            displayName: user.displayName,
          },
          token,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * Login user
   */
  static async login(req: Request, res: Response, next: NextFunction) {
    try {
      const { email, password } = req.body;

      // Find user
      const user = await User.findOne({ email }).select('+password');
      if (!user) {
        return res.status(401).json({ error: 'Invalid credentials' });
      }

      // Compare password
      const isValid = await user.comparePassword(password);
      if (!isValid) {
        return res.status(401).json({ error: 'Invalid credentials' });
      }

      // Update last login
      user.lastLogin = new Date();
      await user.save();

      // Generate token
      const token = jwt.sign({ userId: user._id }, JWT_SECRET, {
        expiresIn: JWT_EXPIRE,
      });

      res.status(200).json({
        success: true,
        data: {
          user: {
            id: user._id,
            email: user.email,
            displayName: user.displayName,
            subscription: user.subscription,
          },
          token,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * Verify email
   */
  static async verifyEmail(req: Request, res: Response, next: NextFunction) {
    try {
      const { token } = req.body;

      const user = await User.findOne({
        verificationToken: token,
        verificationExpires: { $gt: new Date() },
      });

      if (!user) {
        return res.status(400).json({ error: 'Invalid or expired token' });
      }

      user.verificationStatus = 'verified';
      user.verificationToken = undefined;
      user.verificationExpires = undefined;
      await user.save();

      res.status(200).json({
        success: true,
        message: 'Email verified successfully',
      });
    } catch (error) {
      next(error);
    }
  }

  /**
   * Get current user
   */
  static async getCurrentUser(
    req: Request & { userId?: string },
    res: Response,
    next: NextFunction
  ) {
    try {
      const user = await User.findById(req.userId);
      if (!user) {
        return res.status(404).json({ error: 'User not found' });
      }

      res.status(200).json({
        success: true,
        data: user,
      });
    } catch (error) {
      next(error);
    }
  }
}
