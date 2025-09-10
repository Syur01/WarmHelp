import { HttpInterceptorFn } from '@angular/common/http';
import { TokenService} from '../auth/token.service';
import { inject } from '@angular/core';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {

  const tokenService = inject(TokenService);
  const accessToken = tokenService.getAccessToken();
  const publicRoutes = ['/auth/login', '/auth/register', '/users', '/posts', '/carts'];
  const isPublic = publicRoutes.some(route => req.url.includes(route));

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (accessToken && !isPublic) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  console.log('🔎 Interceptor -> URL:', req.url, '| isPublic:', isPublic, '| token:', accessToken);

  return next(req.clone({ setHeaders: headers }));
};
