import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { backEnd } from "../app.config";
import { DashboardStats } from "../modelos/dashboard";

const OPT = { withCredentials: true };

@Injectable({ providedIn: 'root' })
export class DashboardService {
    constructor(private http: HttpClient) { }
    obtenerStats() { 
        return this.http.get<DashboardStats>(`${backEnd.apiUrl}/dashboard`, OPT); 
    }
}