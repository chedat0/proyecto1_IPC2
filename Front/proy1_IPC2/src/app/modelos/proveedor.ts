export interface Proveedor {
    idProveedor?: number;
    nombre: string;
    tipoServicio: number;
    tipoNombre?: string;
    paisOperacion?: string;
    telefono?: string;
    email?: string;
    activo?: boolean;
}