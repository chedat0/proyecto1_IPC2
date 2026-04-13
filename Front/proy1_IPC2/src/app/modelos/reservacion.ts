import { Cliente } from "./cliente";

export interface Reservacion {
    idReservacion?: number;
    numeroReservacion?: string;
    idPaquete?: number;
    nombrePaquete?: string;
    nombreDestino?: string;
    idAgente?: number;
    nombreAgente?: string;
    usuarioAgente?: string;
    fechaCreacion?: string;
    fechaViaje: string;
    cantidadPasajeros?: number;
    costoTotal?: number;
    estado?: 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA';
    pasajeros?: Cliente[];
    totalPagado?: number;
    saldoPendiente?: number;
}