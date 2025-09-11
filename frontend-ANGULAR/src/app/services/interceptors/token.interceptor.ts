import { HttpInterceptorFn } from '@angular/common/http';
import { TokenService} from '../auth/token.service';
import { inject } from '@angular/core';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const accessToken = tokenService.getAccessToken();

  console.log('🛰️ Interceptor ejecutado → URL:', req.url, '| Token:', accessToken);

  const publicRoutes = ['/auth/login', '/auth/register', '/users', '/posts', '/carts'];
  const isPublic = publicRoutes.some(route => req.url.includes(route));

  let headers: Record<string, string> = {};

  if (!(req.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (accessToken && !isPublic) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  return next(req.clone({ setHeaders: headers }));
};


