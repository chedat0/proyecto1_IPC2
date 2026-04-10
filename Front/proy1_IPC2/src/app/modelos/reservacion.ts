import { Cliente } from "./cliente";

export interface Reservacion {
    idReservacion?: number;
    numeroReservacion?: string;
    idPaquete?: number;
    paqueteNombre?: string;
    destinoNombre?: string;
    idAgente?: number;
    agenteNombre?: string;
    fechaCreacion?: string;
    fechaViaje: string;
    cantidadPasajeros?: number;
    costoTotal?: number;
    estado?: 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA';
    pasajeros?: Cliente[];
    totalPagado?: number;
    saldoPendiente?: number;
}