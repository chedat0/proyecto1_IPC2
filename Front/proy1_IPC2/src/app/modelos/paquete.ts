import { ServicioPaquete } from "./servicioPaquete";

export interface Paquete {
    idPaquete?: number;
    nombre: string;
    idDestino?: number;
    destinoNombre?: string;
    destinoPais?: string;
    duracionDias: number;
    descripcion?: string;
    precioVenta: number;
    costoTotalProveedores?: number;
    gananciaBruta?: number;
    capacidadMaxima: number;
    activo?: boolean;
    servicios?: ServicioPaquete[];
}