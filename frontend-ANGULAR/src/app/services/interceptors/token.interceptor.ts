import { HttpInterceptorFn } from '@angular/common/http';
import { TokenService} from '../auth/token.service';
import { inject } from '@angular/core';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {

  const tokenService = inject(TokenService);
  const accessToken = tokenService.getAccessToken();
    const publicRoutes = ['/auth/login', '/auth/register', '/users', '/posts', '/carts'];
  const isPublic = publicRoutes.some(route => req.url.includes(route));

  const cloneReq = req.clone({
    setHeaders:{
      'Content-Type': 'application/json',
      ...(accessToken && !isPublic ? {'Authorization':'Bearer ' + accessToken} : undefined)
    }
  })

  return next(cloneReq);
};
