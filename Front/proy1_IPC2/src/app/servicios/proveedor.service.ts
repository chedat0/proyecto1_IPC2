import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Proveedor } from "../modelos/proveedor";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class ProveedorService {
    private base = `${backEnd.apiUrl}/proveedores`;
    constructor(private http: HttpClient) { }

    obtenerTodo() { 
        return this.http.get<Proveedor[]>(this.base, OPT); 
    }
    
    obtenerPorId(id: number) { 
        return this.http.get<Proveedor>(`${this.base}/${id}`, OPT); 
    }

    crear(p: Proveedor) { 
        return this.http.post<any>(this.base, p, OPT); 
    }

    actualizar(id: number, p: Proveedor) { 
        return this.http.put<any>(`${this.base}/${id}`, p, OPT); 
    }
}