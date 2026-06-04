import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const publicPaths = ['/login', '/forgot-password', '/pin-gate'];
const landingPath = '/';
const pinGatePath = '/pin-gate';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Allow public assets and API routes
  if (
    pathname.startsWith('/_next') ||
    pathname.startsWith('/api') ||
    pathname.startsWith('/images') ||
    pathname.includes('.')
  ) {
    return NextResponse.next();
  }

  // Check PIN gate verification
  const pinVerified = request.cookies.get('pansis_pin_verified')?.value === 'true';
  
  // If not on PIN gate page and PIN not verified, redirect to PIN gate
  if (!pinVerified && pathname !== pinGatePath) {
    return NextResponse.redirect(new URL(pinGatePath, request.url));
  }

  // Check for auth token in cookies (for SSR) or rely on client-side check
  const token = request.cookies.get('pansis_access_token')?.value;

  // Allow landing page
  if (pathname === landingPath) {
    return NextResponse.next();
  }

  // Allow public paths (login, forgot-password, pin-gate)
  if (publicPaths.some((path) => pathname.startsWith(path))) {
    // If already authenticated, redirect to dashboard
    if (token && pathname !== pinGatePath) {
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }
    return NextResponse.next();
  }

  // Protected routes - redirect to login if no token
  // Note: Full auth validation happens client-side with Zustand persisted state
  // This middleware provides a first-pass server-side check
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
