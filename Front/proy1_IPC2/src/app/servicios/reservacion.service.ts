import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { backEnd } from "../app.config";
import { Reservacion } from "../modelos/reservacion";
import { Cliente } from "../modelos/cliente";
import { Cancelacion } from "../modelos/cancelacion";
import { Pago } from "../modelos/pago";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class ReservacionService {
    private base = `${backEnd.apiUrl}/reservaciones`;
    constructor(private http: HttpClient) { }

    obtenerTodas() { 
        return this.http.get<Reservacion[]>(this.base, OPT); 
    }

    obtenerHoy() { 
        return this.http.get<Reservacion[]>(`${this.base}?hoy=true`, OPT); 
    }

    obtenerPorDestinoFecha(idDestino: number, fecha: string) {
        return this.http.get<Reservacion[]>(`${this.base}?idDestino=${idDestino}&fecha=${fecha}`, OPT);
    }

    obtenerPorId(id: number) { 
        return this.http.get<Reservacion>(`${this.base}/${id}`, OPT); 
    }
    
    obtenerPasajeros(id: number) { 
        return this.http.get<Cliente[]>(`${this.base}/${id}/pasajeros`, OPT); 
    }

    obtenerPagos(id: number) { 
        return this.http.get<Pago[]>(`${this.base}/${id}/pagos`, OPT); 
    }
    
    crear(r: { idPaquete: number; fechaViaje: string; pasajeros: number[] }) {
        return this.http.post<Reservacion>(this.base, r, OPT);
    }
    
    cancelar(id: number, motivo: string) { 
        return this.http.post<Cancelacion>(`${this.base}/${id}/cancelar`, { motivo }, OPT); 
    }

    actualizarEstado(id: number, estado: string) { 
        return this.http.patch<any>(`${this.base}/${id}/estado`, { estado }, OPT); 
    }
    
    obtenerComprobante(id: number): Observable<Blob> {
        return this.http.get(`${this.base}/${id}/comprobante`, { ...OPT, responseType: 'blob' });
    }
}