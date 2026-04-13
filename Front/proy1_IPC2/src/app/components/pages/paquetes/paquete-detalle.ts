import { Component, OnInit, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PaqueteService } from '../../../servicios/paquete.service';
import { ProveedorService } from '../../../servicios/proveedor.service';
import { Paquete } from '../../../modelos/paquete';
import { ServicioPaquete } from '../../../modelos/servicioPaquete';
import { Proveedor } from '../../../modelos/proveedor';

@Component({
    selector: 'app-paquete-detalle',    
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    templateUrl: './paquete-detalle.html',
    styleUrl: './paquete-detalle.css'
})
export class PaqueteDetalleComponent implements OnInit {
    paquete?: Paquete;
    servicios: ServicioPaquete[] = [];
    proveedores: Proveedor[] = [];
    loading = true;
    error = '';
    success = '';
    showModal = false;
    savingServicio = false;
    servicioForm!: FormGroup;

    constructor(
        private route: ActivatedRoute,
        private svc: PaqueteService,
        private provSvc: ProveedorService,
        private fb: FormBuilder,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.servicioForm = this.fb.group({
            idProveedor: ['', Validators.required],
            descripcion: ['', Validators.required],
            costoProveedor: ['', [Validators.required, Validators.min(0)]]
        });
        this.cargar();
        this.provSvc.obtenerTodo().subscribe({ next: p => this.proveedores = p });
    }

    cargar() {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.loading = true;
        this.svc.obtenerPorId(id).subscribe({
            next: p => {
                this.paquete = p;
                this.servicios = p.servicios || [];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges(); }
        });
    }

    get costoPercent() {
        if (!this.paquete?.precioVenta || !this.paquete?.costoTotalProveedores) return 0;
        return Math.min(100, (this.paquete.costoTotalProveedores / this.paquete.precioVenta) * 100);
    }

    get gananciaPercent() {
        if (!this.paquete?.precioVenta || !this.paquete?.gananciaBruta) return 0;
        return Math.min(100, (this.paquete.gananciaBruta / this.paquete.precioVenta) * 100);
    }

    get totalCostoServicios() {
        return this.servicios.reduce((s, sv) => s + (sv.costoProveedor || 0), 0);
    }

    agregarServicio() {
        if (this.servicioForm.invalid) { this.servicioForm.markAllAsTouched(); return; }
        this.savingServicio = true;
        this.error = '';
        const val: ServicioPaquete = {
            ...this.servicioForm.value,
            idPaquete: this.paquete!.idPaquete,
            idProveedor: Number(this.servicioForm.value.idProveedor),
            costoProveedor: Number(this.servicioForm.value.costoProveedor)
        };
        this.svc.agregarServicio(val).subscribe({
            next: () => {
                this.showModal = false;
                this.savingServicio = false;
                this.success = 'Servicio agregado correctamente.';
                this.servicioForm.reset();
                this.cargar();
                this.cdr.detectChanges();
            },
            error: e => { this.error = e.message; this.savingServicio = false; this.cdr.detectChanges(); }
        });
    }

    eliminarServicio(id: number) {
        if (!confirm('¿Eliminar este servicio del paquete?')) return;
        this.svc.eliminarServicio(id).subscribe({
            next: () => { this.success = 'Servicio eliminado.'; this.cargar(); this.cdr.detectChanges();},
            error: e => this.error = e.message
        });
    }

    tipoNombre(n: number) { return ['', 'Aerolínea', 'Hotel', 'Tour', 'Traslado', 'Otro'][n] || '—'; }
}
