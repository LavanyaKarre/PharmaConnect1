import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.clone({ withCredentials: true }));
