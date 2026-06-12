import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Project, Layer, Asset } from '../../types';

interface ProjectState {
  currentProject: Project | null;
  projects: Project[];
  isLoading: boolean;
  error: string | null;
  unsavedChanges: boolean;
  selectedLayerId: string | null;
  selectedLayers: string[];
}

const initialState: ProjectState = {
  currentProject: null,
  projects: [],
  isLoading: false,
  error: null,
  unsavedChanges: false,
  selectedLayerId: null,
  selectedLayers: [],
};

const projectSlice = createSlice({
  name: 'project',
  initialState,
  reducers: {
    // Project management
    setCurrentProject: (state, action: PayloadAction<Project>) => {
      state.currentProject = action.payload;
      state.selectedLayerId = null;
      state.selectedLayers = [];
    },

    updateProjectData: (state, action: PayloadAction<Partial<Project>>) => {
      if (state.currentProject) {
        state.currentProject = { ...state.currentProject, ...action.payload };
        state.unsavedChanges = true;
      }
    },

    setProjects: (state, action: PayloadAction<Project[]>) => {
      state.projects = action.payload;
    },

    // Layer management
    addLayer: (state, action: PayloadAction<Layer>) => {
      if (state.currentProject) {
        state.currentProject.data.layers.push(action.payload);
        state.unsavedChanges = true;
      }
    },

    updateLayer: (
      state,
      action: PayloadAction<{ id: string; updates: Partial<Layer> }>
    ) => {
      if (state.currentProject) {
        const layerIndex = state.currentProject.data.layers.findIndex(
          (l) => l.id === action.payload.id
        );
        if (layerIndex !== -1) {
          state.currentProject.data.layers[layerIndex] = {
            ...state.currentProject.data.layers[layerIndex],
            ...action.payload.updates,
          };
          state.unsavedChanges = true;
        }
      }
    },

    deleteLayer: (state, action: PayloadAction<string>) => {
      if (state.currentProject) {
        state.currentProject.data.layers = state.currentProject.data.layers.filter(
          (l) => l.id !== action.payload
        );
        state.unsavedChanges = true;
      }
    },

    selectLayer: (state, action: PayloadAction<string | null>) => {
      state.selectedLayerId = action.payload;
      state.selectedLayers = action.payload ? [action.payload] : [];
    },

    selectMultipleLayers: (state, action: PayloadAction<string[]>) => {
      state.selectedLayers = action.payload;
      state.selectedLayerId = action.payload[0] || null;
    },

    // Canvas management
    updateCanvasState: (state, action: PayloadAction<any>) => {
      if (state.currentProject) {
        state.currentProject.data.canvas = {
          ...state.currentProject.data.canvas,
          ...action.payload,
        };
        state.unsavedChanges = true;
      }
    },

    // Asset management
    addAsset: (state, action: PayloadAction<Asset>) => {
      if (state.currentProject) {
        state.currentProject.data.assets.push(action.payload);
        state.unsavedChanges = true;
      }
    },

    // State management
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },

    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },

    setSavedChanges: (state) => {
      state.unsavedChanges = false;
    },
  },
});

export const {
  setCurrentProject,
  updateProjectData,
  setProjects,
  addLayer,
  updateLayer,
  deleteLayer,
  selectLayer,
  selectMultipleLayers,
  updateCanvasState,
  addAsset,
  setLoading,
  setError,
  setSavedChanges,
} = projectSlice.actions;

export default projectSlice.reducer;
