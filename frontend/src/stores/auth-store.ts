import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, AuthTokens } from '@/types';
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/constants';

interface AuthState {
  user: User | null;
  tokens: AuthTokens | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User) => void;
  setTokens: (tokens: AuthTokens) => void;
  login: (user: User, tokens: AuthTokens) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  updateUser: (updates: Partial<User>) => void;
}

/**
 * Sync tokens to direct localStorage keys so apiClient interceptor can read them.
 * apiClient reads from localStorage.getItem('pansis_access_token'), NOT from Zustand persist.
 */
function syncTokensToStorage(tokens: AuthTokens | null) {
  if (typeof window === 'undefined') return;

  if (tokens) {
    localStorage.setItem(TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  } else {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      tokens: null,
      isAuthenticated: false,
      isLoading: true,

      setUser: (user) => set({ user }),

      setTokens: (tokens) => {
        syncTokensToStorage(tokens);
        set({ tokens });
      },

      login: (user, tokens) => {
        syncTokensToStorage(tokens);
        set({
          user,
          tokens,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      logout: () => {
        syncTokensToStorage(null);
        set({
          user: null,
          tokens: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      setLoading: (isLoading) => set({ isLoading }),

      updateUser: (updates) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...updates } : null,
        })),
    }),
    {
      name: 'pansis-auth-storage',
      partialize: (state) => ({
        user: state.user,
        tokens: state.tokens,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => {
        // After Zustand rehydrates from localStorage, sync tokens to direct keys
        return (state) => {
          if (state?.tokens) {
            syncTokensToStorage(state.tokens);
          }
        };
      },
    }
  )
);
