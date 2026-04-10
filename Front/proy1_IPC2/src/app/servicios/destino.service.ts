import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Destino } from "../modelos/destino";
import { Paquete } from "../modelos/paquete";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class DestinoService {
    private base = `${backEnd.apiUrl}/destinos`;
    constructor(private http: HttpClient) { }

    obtenerTodos() { 
        return this.http.get<Destino[]>(this.base, OPT); 
    }
    
    obtenerPorId(id: number) { 
        return this.http.get<Destino>(`${this.base}/${id}`, OPT); 
    }
    
    obtenerPaquetes(id: number) { 
        return this.http.get<Paquete[]>(`${this.base}/${id}/paquetes`, OPT); 
    }

    crear(d: Destino) { 
        return this.http.post<any>(this.base, d, OPT); 
    }
    
    actualizar(id: number, d: Destino) { 
        return this.http.put<any>(`${this.base}/${id}`, d, OPT); 
    }

    eliminar(id: number) { 
        return this.http.delete<any>(`${this.base}/${id}`, OPT); 
    }
}