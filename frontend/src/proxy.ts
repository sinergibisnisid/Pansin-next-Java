import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const publicPaths = ['/login', '/forgot-password', '/pin-gate'];
const landingPath = '/';
const pinGatePath = '/pin-gate';

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (
    pathname.startsWith('/_next') ||
    pathname.startsWith('/api') ||
    pathname.startsWith('/images') ||
    pathname.includes('.')
  ) {
    return NextResponse.next();
  }

  const pinVerified = request.cookies.get('pansis_pin_verified')?.value === 'true';

  if (!pinVerified && pathname !== pinGatePath) {
    return NextResponse.redirect(new URL(pinGatePath, request.url));
  }

  const token = request.cookies.get('pansis_access_token')?.value;

  if (pathname === landingPath) {
    return NextResponse.next();
  }

  if (publicPaths.some((path) => pathname.startsWith(path))) {
    if (token && pathname !== pinGatePath) {
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }
    return NextResponse.next();
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
