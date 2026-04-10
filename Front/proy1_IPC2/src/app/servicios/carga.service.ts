import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { backEnd } from "../app.config";


const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class CargaService {
    constructor(private http: HttpClient) { }
    cargarArchivo(file: File): Observable<any> {
        const form = new FormData();
        form.append('archivo', file, file.name);
        return this.http.post<any>(`${backEnd.apiUrl}/carga`, form, OPT);
    }
}
