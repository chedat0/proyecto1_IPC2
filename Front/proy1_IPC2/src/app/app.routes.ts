import { Routes } from '@angular/router';
import { authGuard, operacionesGuard, adminGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    {
        path: 'login',
        loadComponent: () => import('./components/pages/login/login').then(m => m.LoginComponent)
    },
    {
        path: '',
        loadComponent: () => import('./components/shared/layout/layout').then(m => m.LayoutComponent),
        canActivate: [authGuard],
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./components/pages/dashboard/dashboard').then(m => m.DashboardComponent)
            },
            // Atención al Cliente
            {
                path: 'clientes',
                loadComponent: () => import('./components/pages/cliente/cliente').then(m => m.ClienteComponent)
            },
            {
                path: 'clientes/nuevo',
                loadComponent: () => import('./components/pages/cliente/cliente-form').then(m => m.ClienteFormComponent)
            },
            {
                path: 'clientes/:id',
                loadComponent: () => import('./components/pages/cliente/detalle-cliente').then(m => m.DetalleClienteComponent)
            },
            {
                path: 'clientes/:id/editar',
                loadComponent: () => import('./components/pages/cliente/cliente-form')
                    .then(m => m.ClienteFormComponent)
            },
            {
                path: 'reservaciones',
                loadComponent: () => import('./components/pages/reservaciones/reservaciones').then(m => m.ReservacionesComponent)
            },
            {
                path: 'reservaciones/nueva',
                loadComponent: () => import('./components/pages/reservaciones/reservacion-form').then(m => m.ReservacionFormComponent)
            },
            {
                path: 'reservaciones/:id',
                loadComponent: () => import('./components/pages/reservaciones/detalle-reservacion').then(m => m.DetalleReservacionComponent)
            },
            // Operaciones
            {
                path: 'destinos',
                loadComponent: () => import('./components/pages/destinos/destinos').then(m => m.DestinosComponent)
            },
            {
                path: 'proveedores',
                loadComponent: () => import('./components/pages/proveedores/proveedores').then(m => m.ProveedoresComponent)
            },
            {
                path: 'paquetes',
                loadComponent: () => import('./components/pages/paquetes/paquetes').then(m => m.PaquetesComponent)
            },
            {
                path: 'paquetes/:id',
                loadComponent: () => import('./components/pages/paquetes/paquete-detalle').then(m => m.PaqueteDetalleComponent)
            },
            // Administración 
            {
                path: 'reportes',
                canActivate: [adminGuard],
                loadComponent: () => import('./components/pages/reportes/reportes').then(m => m.ReportesComponent)
            },
            {
                path: 'usuarios',
                canActivate: [adminGuard],
                loadComponent: () => import('./components/pages/usuarios/usuarios').then(m => m.UsuariosComponent)
            },
            {
                path: 'carga',
                canActivate: [adminGuard],
                loadComponent: () => import('./components/pages/carga/carga').then(m => m.CargaComponent)
            },
        ]
    },
    { path: '**', redirectTo: 'login' }
];
