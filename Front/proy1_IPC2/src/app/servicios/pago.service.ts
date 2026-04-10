import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { Pago } from "../modelos/pago";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class PagoService {
    private base = `${backEnd.apiUrl}/pagos`;
    constructor(private http: HttpClient) { }

    obtenerPorReservacion(id: number) { 
        return this.http.get<Pago[]>(`${this.base}?idReservacion=${id}`, OPT); 
    }
    
    registrar(p: { idReservacion: number; monto: number; metodoPago: number }) {
        return this.http.post<any>(this.base, p, OPT);
    }
}