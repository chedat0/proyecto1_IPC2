import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Cliente } from "../modelos/cliente";
import { Reservacion } from "../modelos/reservacion";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class ClienteService {
    private base = `${backEnd.apiUrl}/clientes`;
    constructor(private http: HttpClient) { }

    obtenerTodos() { 
        return this.http.get<Cliente[]>(this.base, OPT); 
    }
    
    buscar(q: string) { 
        return this.http.get<Cliente[]>(`${this.base}?q=${q}`, OPT); 
    }
    
    obtenerPorId(id: number) { 
        return this.http.get<Cliente>(`${this.base}/${id}`, OPT); 
    }
    
    obtenerPorDPI(dpi: string) {
        return this.http.get<Cliente>(`${this.base}/dpi/${dpi}`, OPT); 
    }
    
    obtenerReservaciones(id: number) { 
        return this.http.get<Reservacion[]>(`${this.base}/${id}/reservaciones`, OPT); 
    }
    
    crear(c: Cliente) { 
        return this.http.post<any>(this.base, c, OPT); 
    }
    
    actualizar(id: number, c: Cliente) { 
        return this.http.put<any>(`${this.base}/${id}`, c, OPT); 
    }
}