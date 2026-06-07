'use client';

import { useState, useEffect } from 'react';

/**
 * Hook to prevent hydration mismatch with Zustand persisted stores.
 * Returns false during SSR and initial client render, true after hydration.
 */
export function useHydration() {
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => setHydrated(true), 0);
    return () => window.clearTimeout(timer);
  }, []);

  return hydrated;
}
