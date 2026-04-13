import { Component, OnInit, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { ClienteService } from '../../../servicios/cliente.service';
import { Cliente } from '../../../modelos/cliente';
import { Reservacion } from '../../../modelos/reservacion';

@Component({
    selector: 'app-cliente-detail',    
    imports: [CommonModule, RouterLink],
    templateUrl: './detalle-cliente.html',
    styleUrl: './detalle-cliente.css'
})
export class DetalleClienteComponent implements OnInit {
    cliente?: Cliente;
    reservaciones: Reservacion[] = [];
    loading = true;
    error = '';    
    clienteId!: number;

    constructor(
        private svc: ClienteService,
        private route: ActivatedRoute,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.clienteId = Number(this.route.snapshot.paramMap.get('id'));
        this.svc.obtenerPorId(this.clienteId).subscribe({
            next: c => {
                this.cliente = c;
                this.svc.obtenerReservaciones(this.clienteId).subscribe({
                    next: r => { this.reservaciones = r; this.loading = false; this.cdr.detectChanges();},
                    error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges();}
                });
            },
            error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges();}
        });
    }
        
    get totalReservaciones() { 
        return this.reservaciones.length; 
    }

    get reservacionesConfirmadas() { 
        return this.reservaciones.filter(r => r.estado === 'CONFIRMADA').length; 
    }

    get reservacionesPendientes() { 
        return this.reservaciones.filter(r => r.estado !== 'CANCELADA' && (r.saldoPendiente || 0 > 0)).length; 
    }
    
    get totalGastado() {
        return this.reservaciones.filter(r => r.estado !== 'CANCELADA').reduce((s, r) => s + (r.totalPagado || 0), 0); 
    }

    regresar() { this.router.navigate(['/clientes']); }
}
