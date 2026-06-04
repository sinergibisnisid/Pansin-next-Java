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
      } else {
        sessionStorage.removeItem('pansis_pin_verified');
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
