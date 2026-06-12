// ============================================================================
// AUTH ROUTES - Authentication endpoints
// ============================================================================

import { Router } from 'express';
import { AuthController } from '../controllers/AuthController';
import { authMiddleware } from '../middleware/auth';

const router = Router();

router.post('/register', AuthController.register);
router.post('/login', AuthController.login);
router.post('/verify-email', AuthController.verifyEmail);
router.get('/me', authMiddleware, AuthController.getCurrentUser);

export default router;
