import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PaqueteService } from '../../../servicios/paquete.service';
import { DestinoService } from '../../../servicios/destino.service';
import { Paquete } from '../../../modelos/paquete';
import { Destino } from '../../../modelos/destino';

@Component({
  selector: 'app-paquetes',
  imports: [CommonModule, ReactiveFormsModule,RouterLink],
  templateUrl: './paquetes.html',
  styleUrl: './paquetes.css',
})
export class PaquetesComponent implements OnInit {

  paquetes: Paquete[]    = [];
  destinos: Destino[]    = [];
  altaDemanda: Paquete[] = [];
  loading  = true;
  saving   = false;
  error    = '';
  success  = '';
  showModal = false;
  editando  = false;
  editId?:  number;
  form!: FormGroup;

  constructor(private svc: PaqueteService, private destSvc: DestinoService, private fb: FormBuilder, private cdr: ChangeDetectorRef ) {}

  ngOnInit() {
    this.form = this.fb.group({
      nombre:          ['', Validators.required],
      idDestino:       ['', Validators.required],
      duracionDias:    ['', [Validators.required, Validators.min(1)]],
      descripcion:     [''],
      precioVenta:     ['', [Validators.required, Validators.min(0.01)]],
      capacidadMaxima: ['', [Validators.required, Validators.min(1)]],
      activo:          [true]
    });
    this.cargar();
    this.destSvc.obtenerTodos().subscribe({ next: d => this.destinos = d });
    this.svc.obtenerAltaDemanda().subscribe({ next: p => this.altaDemanda = p });
  }

  cargar() {
    this.loading = true;
    this.svc.obtenerTodo().subscribe({
      next:  p => { this.paquetes = p; this.loading = false; this.cdr.detectChanges(); },
      error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges();}
    });
  }

  abrirModal() { 
    this.editando = false; 
    this.editId = undefined; 
    this.form.reset({ activo: true }); 
    this.showModal = true; 
  }

  cerrar()     { this.showModal = false; }

  editar(p: Paquete) {
    this.editando = true;
    this.editId   = p.idPaquete;
    this.form.patchValue({ ...p, idDestino: String(p.idDestino) });
    this.showModal = true;
  }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error  = '';
    const val = { ...this.form.value, idDestino: Number(this.form.value.idDestino) };
    const op  = this.editando ? this.svc.actualizar(this.editId!, val) : this.svc.crear(val);
    op.subscribe({
      next: () => {
        this.success = this.editando ? 'Paquete actualizado.' : 'Paquete creado.';
        this.cerrar();
        this.saving = false;
        this.cargar();
      },
      error: e => { this.error = e.message; this.saving = false; }
    });
  }
}
