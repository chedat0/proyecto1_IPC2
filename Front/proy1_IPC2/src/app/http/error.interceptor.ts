import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router);
    return next(req).pipe(
        catchError(err => {
            if (err.status === 401 && !req.url.includes('/auth/login')) {
                sessionStorage.removeItem('usuario');
                router.navigate(['/login']);
            }
            const msg = err.error?.error || err.error?.mensaje || err.message || 'Error desconocido';
            return throwError(() => new Error(msg));
        })
    );
};