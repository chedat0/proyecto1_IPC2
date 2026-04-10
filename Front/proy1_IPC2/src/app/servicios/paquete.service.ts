import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Paquete } from "../modelos/paquete";
import { ServicioPaquete } from "../modelos/servicioPaquete";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class PaqueteService {
    private base = `${backEnd.apiUrl}/paquetes`;
    constructor(private http: HttpClient) { }

    obtenerTodo(soloActivos = false) { 
        return this.http.get<Paquete[]>(`${this.base}${soloActivos ? '?activos=true' : ''}`, OPT); 
    }

    obtenerPorId(id: number) { 
        return this.http.get<Paquete>(`${this.base}/${id}`, OPT); 
    }
    
    obtenerServicios(id: number) { 
        return this.http.get<ServicioPaquete[]>(`${this.base}/${id}/servicios`, OPT); 
    }
    
    obtenerAltaDemanda() { 
        return this.http.get<Paquete[]>(`${this.base}?altaDemanda=true`, OPT); 
    }
    
    crear(p: Paquete) { 
        return this.http.post<any>(this.base, p, OPT); 
    }

    actualizar(id: number, p: Paquete) { 
        return this.http.put<any>(`${this.base}/${id}`, p, OPT); 
    }
    
    agregarServicio(s: ServicioPaquete) { 
        return this.http.post<any>(`${backEnd.apiUrl}/servicios`, s, OPT); 
    }

    eliminarServicio(id: number) { 
        return this.http.delete<any>(`${backEnd.apiUrl}/servicios/${id}`, OPT); 
    }
}