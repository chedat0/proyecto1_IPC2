import { Component, OnInit, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReservacionService } from '../../../servicios/reservacion.service';
import { PagoService } from '../../../servicios/pago.service';
import { Reservacion } from '../../../modelos/reservacion';
import { Pago } from '../../../modelos/pago';

@Component({
    selector: 'app-reservacion-detail',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
    templateUrl: './detalle-reservacion.html',
    styleUrl: './detalle-reservacion.css'
})
export class DetalleReservacionComponent implements OnInit {
    reservacion?: Reservacion;
    pagos: Pago[] = [];
    loading = true;
    error = '';
    success = '';
    showPagoModal = false;
    loadingPago = false;
    loadingCancelar = false;
    motivoCancelacion = '';
    pagoForm!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private resSvc: ReservacionService,
        private pagoSvc: PagoService,
        private drc: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.pagoForm = this.fb.group({
            monto: ['', [Validators.required, Validators.min(0.01)]],
            metodoPago: ['', Validators.required]
        });
        this.cargar();
    }

    cargar() {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.loading = true;
        this.resSvc.obtenerPorId(id).subscribe({
            next: r => {
                this.reservacion = r;
                this.pagoSvc.obtenerPorReservacion(id).subscribe({
                    next: p => { this.pagos = p; this.loading = false; this.drc.detectChanges();},
                    error: e => { this.error = e.message; this.loading = false; this.drc.detectChanges();}
                });
            },
            error: e => { this.error = e.message; this.loading = false; this.drc.detectChanges();}
        });
    }

    get porcentajePago() {
        if (!this.reservacion?.costoTotal) return 0;
        return Math.min(100, ((this.reservacion.totalPagado || 0) / this.reservacion.costoTotal) * 100);
    }

    get puedeRegistrarPago() {
        return this.reservacion?.estado === 'PENDIENTE' || this.reservacion?.estado === 'CONFIRMADA';
    }

    get puedeCancelar() {
        return this.reservacion?.estado === 'PENDIENTE' || this.reservacion?.estado === 'CONFIRMADA';
    }

    registrarPago() {
        if (this.pagoForm.invalid) { this.pagoForm.markAllAsTouched(); return; }
        this.loadingPago = true;
        this.error = '';
        const payload = {
            idReservacion: this.reservacion!.idReservacion!,
            monto: Number(this.pagoForm.get('monto')?.value),
            metodoPago: Number(this.pagoForm.get('metodoPago')?.value)
        };
        this.pagoSvc.registrar(payload).subscribe({
            next: () => {
                this.showPagoModal = false;
                this.loadingPago = false;
                this.success = 'Pago registrado correctamente.';
                this.pagoForm.reset();
                this.cargar();
                this.drc.detectChanges();
            },
            error: e => { this.error = e.message; this.loadingPago = false; this.drc.detectChanges();}
        });
    }

    procesarCancelacion() {
        if (!confirm('¿Estás seguro de cancelar esta reservación? Esta acción no se puede deshacer.')) return;
        this.loadingCancelar = true;
        this.error = '';
        this.resSvc.cancelar(this.reservacion!.idReservacion!, this.motivoCancelacion).subscribe({
            next: can => {
                this.success = `Reservación cancelada. Reembolso: Q.${can.montoReembolso}`;
                this.loadingCancelar = false;
                this.cargar();
                this.drc.detectChanges();
            },
            error: e => { this.error = e.message; this.loadingCancelar = false; this.drc.detectChanges();}
        });
    }

    descargarComprobante() {
        this.resSvc.obtenerComprobante(this.reservacion!.idReservacion!).subscribe(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `comprobante-${this.reservacion!.numeroReservacion}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
        });
    }

    metodo(n: number) { return ['', 'Efectivo', 'Tarjeta', 'Transferencia'][n] || '—'; }
}