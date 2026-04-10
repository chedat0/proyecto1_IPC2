import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Usuario } from "../modelos/usuario";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class UsuarioService {
    private base = `${backEnd.apiUrl}/usuarios`;
    constructor(private http: HttpClient) { }

    obtenerTodo() { 
        return this.http.get<Usuario[]>(this.base, OPT); 
    }

    obtenerPorId(id: number) { 
        return this.http.get<Usuario>(`${this.base}/${id}`, OPT); 
    }

    crear(u: any) { 
        return this.http.post<any>(this.base, u, OPT); 
    }

    actualizar(id: number, u: any) { 
        return this.http.put<any>(`${this.base}/${id}`, u, OPT); 
    }

    desactivar(id: number) { 
        return this.http.delete<any>(`${this.base}/${id}`, OPT); 
    }
}
