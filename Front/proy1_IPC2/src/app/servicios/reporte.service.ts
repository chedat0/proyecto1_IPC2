import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class ReporteService {
    private base = `${backEnd.apiUrl}/reportes`;
    constructor(private http: HttpClient) { }

    private params(desde?: string, hasta?: string) {
        let p: any = {};
        if (desde) p['desde'] = desde;
        if (hasta) p['hasta'] = hasta;
        return { ...OPT, params: p };
    }

    ventas(desde?: string, hasta?: string) { 
        return this.http.get<any[]>(`${this.base}/ventas`, this.params(desde, hasta)); 
    }
    
    cancelaciones(desde?: string, hasta?: string) { 
        return this.http.get<any[]>(`${this.base}/cancelaciones`, this.params(desde, hasta)); 

    }
    
    ganancias(desde?: string, hasta?: string) { 
        return this.http.get<any>(`${this.base}/ganancias`, this.params(desde, hasta)); 
    }

    agenteVentas(desde?: string, hasta?: string) { 
        return this.http.get<any[]>(`${this.base}/agente-ventas`, this.params(desde, hasta)); 
    }

    agenteGanancias(desde?: string, hasta?: string) { 
        return this.http.get<any[]>(`${this.base}/agente-ganancias`, this.params(desde, hasta)); 
    }

    paqueteMasVendido(d?: string, h?: string) { 
        return this.http.get<any>(`${this.base}/paquete-mas-vendido`, this.params(d, h)); 
    }

    paqueteMenosVendido(d?: string, h?: string) { 
        return this.http.get<any>(`${this.base}/paquete-menos-vendido`, this.params(d, h)); 
    }

    ocupacionDestino(d?: string, h?: string) { 
        return this.http.get<any[]>(`${this.base}/ocupacion-destino`, this.params(d, h)); 
    }
}