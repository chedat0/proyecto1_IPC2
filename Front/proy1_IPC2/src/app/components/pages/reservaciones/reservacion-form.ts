import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ReservacionService } from '../../../servicios/reservacion.service';
import { PaqueteService } from '../../../servicios/paquete.service';
import { ClienteService } from '../../../servicios/cliente.service';
import { DestinoService } from '../../../servicios/destino.service';
import { Paquete } from '../../../modelos/paquete';
import { Cliente } from '../../../modelos/cliente';
import { Destino } from '../../../modelos/destino';

@Component({
    selector: 'app-reservacion-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
    templateUrl: './reservacion-form.html',
    styleUrl: './reservacion-form.css'
})
export class ReservacionFormComponent implements OnInit {
    paso = 1;
    paso1Form!: FormGroup;
    paquetes: Paquete[] = [];
    paquetesFiltrados: Paquete[] = [];
    destinos: Destino[] = [];
    idDestinoFiltro = '';
    paqueteSeleccionado?: Paquete;
    busquedaCliente = '';
    resultadosBusqueda: Cliente[] = [];
    pasajerosSeleccionados: Cliente[] = [];
    sinResultados = false;
    loading = false;
    error = '';
    success = '';
    hoy = new Date().toISOString().split('T')[0];

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private resSvc: ReservacionService,
        private paqSvc: PaqueteService,
        private cliSvc: ClienteService,
        private destSvc: DestinoService
    ) { }

    ngOnInit() {
        this.paso1Form = this.fb.group({
            idPaquete: ['', Validators.required],
            fechaViaje: ['', Validators.required]
        });
        this.paqSvc.obtenerTodo(true).subscribe({ next: p => { this.paquetes = p; this.paquetesFiltrados = p; } });
        this.destSvc.obtenerTodos().subscribe({ next: d => this.destinos = d });
    }

    filtrarPaquetes() {
        this.paquetesFiltrados = this.idDestinoFiltro
            ? this.paquetes.filter(p => p.idDestino === Number(this.idDestinoFiltro))
            : this.paquetes;
    }

    onPaqueteChange() {
        const id = Number(this.paso1Form.get('idPaquete')?.value);
        this.paqueteSeleccionado = this.paquetes.find(p => p.idPaquete === id);
    }

    irPaso2() {
        if (this.paso1Form.invalid) { this.paso1Form.markAllAsTouched(); return; }
        this.paso = 2;
    }

    buscarCliente() {
        if (!this.busquedaCliente.trim()) return;
        this.sinResultados = false;
        this.cliSvc.buscar(this.busquedaCliente.trim()).subscribe({
            next: res => { this.resultadosBusqueda = res; this.sinResultados = res.length === 0; },
            error: e => this.error = e.message
        });
    }

    agregarPasajero(c: Cliente) {
        if (this.isPasajeroAgregado(c.idCliente!)) return;
        if (this.pasajerosSeleccionados.length >= (this.paqueteSeleccionado?.capacidadMaxima || 99)) {
            this.error = 'Se alcanzó la capacidad máxima del paquete.';
            return;
        }
        this.pasajerosSeleccionados.push(c);
    }

    quitarPasajero(id: number) {
        this.pasajerosSeleccionados = this.pasajerosSeleccionados.filter(p => p.idCliente !== id);
    }

    isPasajeroAgregado(id: number) {
        return this.pasajerosSeleccionados.some(p => p.idCliente === id);
    }

    irPaso3() {
        if (this.pasajerosSeleccionados.length === 0) { this.error = 'Agrega al menos un pasajero.'; return; }
        this.error = '';
        this.paso = 3;
    }

    get costoTotal() {
        return (this.paqueteSeleccionado?.precioVenta || 0) * this.pasajerosSeleccionados.length;
    }

    confirmarReservacion() {
        this.loading = true;
        this.error = '';
        const payload = {
            idPaquete: Number(this.paso1Form.get('idPaquete')?.value),
            fechaViaje: this.paso1Form.get('fechaViaje')?.value,
            pasajeros: this.pasajerosSeleccionados.map(p => p.idCliente!)
        };
        this.resSvc.crear(payload).subscribe({
            next: res => {
                this.loading = false;
                this.success = `Reservación ${res.numeroReservacion} creada exitosamente.`;
                setTimeout(() => this.router.navigate(['/reservaciones', res.idReservacion]), 1500);
            },
            error: e => { this.error = e.message; this.loading = false; }
        });
    }
}