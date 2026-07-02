// ============================================================================
// API SERVICE - REST API client (improved)
// ============================================================================

import axios, { AxiosInstance, AxiosError } from 'axios';
import { APIResponse, PaginatedResponse } from '../types';

class APIService {
  private client: AxiosInstance;
  private baseURL = process.env.REACT_APP_API_URL || 'http://localhost:3000/api';

  constructor() {
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken');
      if (token) {
        if (!config.headers) config.headers = {};
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle errors
    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          // Clear token and redirect to login in a SPA-friendly way
          localStorage.removeItem('authToken');
          window.location.replace('/login');
        }
        return Promise.reject(error);
      }
    );
  }

  // ========== AUTH ENDPOINTS ==========
  async register(email: string, password: string, displayName: string) {
    return this.client.post('/auth/register', {
      email,
      password,
      displayName,
    });
  }

  async login(email: string, password: string) {
    return this.client.post('/auth/login', { email, password });
  }

  async logout() {
    // Call server to revoke token and then clean up locally
    try {
      await this.client.post('/auth/logout');
    } catch (e) {
      // ignore network errors but still clear local session
    } finally {
      localStorage.removeItem('authToken');
      window.location.replace('/login');
    }
  }

  async verifyEmail(token: string) {
    return this.client.post('/auth/verify-email', { token });
  }

  // ========== PROJECT ENDPOINTS ==========
  async getProjects(page = 1, pageSize = 20) {
    return this.client.get<PaginatedResponse<any>>('/projects', { params: { page, pageSize } });
  }

  async getProject(id: string) {
    return this.client.get(`/projects/${id}`);
  }

  async createProject(data: any) {
    return this.client.post('/projects', data);
  }

  async updateProject(id: string, data: any) {
    return this.client.put(`/projects/${id}`, data);
  }

  async deleteProject(id: string) {
    return this.client.delete(`/projects/${id}`);
  }

  async duplicateProject(id: string) {
    return this.client.post(`/projects/${id}/duplicate`);
  }

  // ========== ASSET ENDPOINTS ==========
  async getAssets(category?: string, page = 1) {
    return this.client.get('/assets', {
      params: { category, page, pageSize: 50 },
    });
  }

  async uploadAsset(file: File, category: string) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);

    return this.client.post('/assets/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  }

  async deleteAsset(id: string) {
    return this.client.delete(`/assets/${id}`);
  }

  // ========== MARKETPLACE ENDPOINTS ==========
  async getMarketplaceItems(category?: string, page = 1) {
    return this.client.get('/marketplace', {
      params: { category, page, pageSize: 20 },
    });
  }

  async searchMarketplace(query: string) {
    return this.client.get('/marketplace/search', { params: { q: query } });
  }

  async purchaseItem(itemId: string) {
    return this.client.post(`/marketplace/${itemId}/purchase`);
  }

  // ========== AI ENDPOINTS ==========
  async generateCode(prompt: string) {
    return this.client.post('/ai/generate-code', { prompt });
  }

  async generateUI(prompt: string) {
    return this.client.post('/ai/generate-ui', { prompt });
  }

  async generateDesign(prompt: string) {
    return this.client.post('/ai/generate-design', { prompt });
  }

  async generateImage(prompt: string) {
    return this.client.post('/ai/generate-image', { prompt });
  }

  // ========== EXPORT ENDPOINTS ==========
  async exportProject(id: string, format: string) {
    return this.client.post(`/projects/${id}/export`, { format }, {
      responseType: 'blob',
    });
  }

  async exportAsAPK(id: string) {
    return this.client.post(`/projects/${id}/export-apk`, {}, {
      responseType: 'blob',
    });
  }

  async exportAsHTML(id: string) {
    return this.client.post(`/projects/${id}/export-html`, {}, {
      responseType: 'blob',
    });
  }
}

export const apiService = new APIService();
