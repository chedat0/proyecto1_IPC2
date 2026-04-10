export interface Pago {
    idPago?: number;
    idReservacion?: number;
    numeroReservacion?: string;
    monto: number;
    metodoPago: number;
    fechaPago?: string;
    numeroComprobante?: string;
    metodoNombre?: string;
}