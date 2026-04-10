import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { backEnd } from '../app.config';
import { Usuario } from '../modelos/usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly base = `${backEnd.apiUrl}/auth`;
    private userSubject = new BehaviorSubject<Usuario | null>(null);
    public user$ = this.userSubject.asObservable();

    constructor(private http: HttpClient) {
        const saved = sessionStorage.getItem('usuario');
        if (saved) this.userSubject.next(JSON.parse(saved));
    }

    login(usuario: string, password: string): Observable<any> {
        return this.http.post(`${this.base}/login`, { usuario, contraHasheada: password }, { withCredentials: true, headers: { 'Content-Type': 'application/json' } })
            .pipe(tap((res: any) => {
                this.userSubject.next(res);
                sessionStorage.setItem('usuario', JSON.stringify(res));
            }));
    }

    logout(): Observable<any> {
        return this.http.post(`${this.base}/logout`, {}, { withCredentials: true })
            .pipe(tap(() => {
                this.userSubject.next(null);
                sessionStorage.removeItem('usuario');
            }));
    }

    getMe(): Observable<Usuario> {
        return this.http.get<Usuario>(`${this.base}/me`, { withCredentials: true });
    }

    get currentUser(): Usuario | null { return this.userSubject.value; }
    isLoggedIn(): boolean { return !!this.userSubject.value; }
    hasRole(rol: number): boolean { return this.userSubject.value?.rol === rol; }
    isAdmin(): boolean { return this.hasRole(3); }
    isOperaciones(): boolean { return this.hasRole(2) || this.isAdmin(); }
    isCliente(): boolean { return this.hasRole(1) || this.isAdmin(); }
}