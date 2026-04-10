export interface Usuario {
    idUsuario?: number;
    usuario: string;
    nombreCompleto: string;
    rol: number;
    rolNombre?: string;
    activo?: boolean;
    fechaCreacion?: string;
}