import { create } from 'zustand';

interface PinGateState {
  isVerified: boolean;
  setVerified: (verified: boolean) => void;
  reset: () => void;
}

/**
 * Pin gate store - tracks whether the user has verified the access PIN for this session.
 * Uses sessionStorage so the PIN must be re-entered when the browser tab is closed.
 */
export const usePinGateStore = create<PinGateState>()((set) => ({
  isVerified:
    typeof window !== 'undefined'
      ? sessionStorage.getItem('pansis_pin_verified') === 'true'
      : false,

  setVerified: (verified) => {
    if (typeof window !== 'undefined') {
      if (verified) {
        sessionStorage.setItem('pansis_pin_verified', 'true');
        // Set cookie for middleware
        document.cookie = 'pansis_pin_verified=true; path=/; SameSite=Strict';
      } else {
        sessionStorage.removeItem('pansis_pin_verified');
        // Remove cookie
        document.cookie = 'pansis_pin_verified=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
      }
    }
    set({ isVerified: verified });
  },

  reset: () => {
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('pansis_pin_verified');
    }
    set({ isVerified: false });
  },
}));
