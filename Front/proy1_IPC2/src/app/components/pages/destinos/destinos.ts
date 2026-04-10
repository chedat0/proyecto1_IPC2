import { Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DestinoService } from '../../../servicios/destino.service';
import { Destino } from '../../../modelos/destino';

@Component({
  selector: 'app-destinos',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './destinos.html',
  styleUrl: './destinos.css',
})

export class DestinosComponent implements OnInit {
  destinos: Destino[] = [];
  loading  = true;
  saving   = false;
  error    = '';
  success  = '';
  showModal = false;
  editando  = false;
  editId?: number;
  form!: FormGroup;

  constructor(private svc: DestinoService, private fb: FormBuilder) {}

  ngOnInit() {
    this.form = this.fb.group({
      nombre:     ['', Validators.required],
      pais:       ['', Validators.required],
      descripcion:[''],
      clima:      [''],
      mejorEpoca: [''],
      urlImagen:  ['']
    });
    this.cargar();
  }

  cargar() {
    this.loading = true;
    this.svc.obtenerTodos().subscribe({
      next:  d => { this.destinos = d; this.loading = false; },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  abrirModal()    { 
    this.editando = false; 
    this.editId = undefined; 
    this.form.reset(); 
    this.showModal = true; 
  }

  cerrarModal()   { 
    this.showModal = false; 
    this.form.reset(); 
  }

  editarDestino(d: Destino) {
    this.editando = true;
    this.editId   = d.idDestino;
    this.form.patchValue(d);
    this.showModal = true;
  }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error  = '';
    const op = this.editando
      ? this.svc.actualizar(this.editId!, this.form.value)
      : this.svc.crear(this.form.value);
    op.subscribe({
      next: () => {
        this.success = this.editando ? 'Destino actualizado.' : 'Destino creado.';
        this.cerrarModal();
        this.saving = false;
        this.cargar();
      },
      error: e => { this.error = e.message; this.saving = false; }
    });
  }

  eliminarDestino(id: number) {
    if (!confirm('¿Eliminar este destino?')) return;
    this.svc.eliminar(id).subscribe({
      next:  () => { this.success = 'Destino eliminado.'; this.cargar(); },
      error: e  => this.error = e.message
    });
  }
}
