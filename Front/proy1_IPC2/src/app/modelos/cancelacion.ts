export interface Cancelacion {
    idCancelacion?: number;
    idReservacion?: number;
    numeroReservacion?: string;
    fechaCancelacion?: string;
    motivo?: string;
    montoPagado?: number;
    porcentajeReembolso?: number;
    montoReembolso?: number;
    perdidaAgencia?: number;
}