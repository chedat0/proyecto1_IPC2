import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProveedorService } from '../../../servicios/proveedor.service';
import { Proveedor } from '../../../modelos/proveedor';

@Component({
  selector: 'app-proveedores',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './proveedores.html',
  styleUrl: './proveedores.css',
})
export class ProveedoresComponent implements OnInit {
  proveedores: Proveedor[] = [];
  loading = true;
  saving = false;
  error = '';
  success = '';
  showModal = false;
  editando = false;
  editId?: number;
  form!: FormGroup;

  constructor(private svc: ProveedorService, private fb: FormBuilder, private cdr: ChangeDetectorRef) { }

  ngOnInit() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      tipoServicio: ['', [Validators.required, Validators.min(1)]],
      paisOperacion: [''],
      telefono: [''],
      email: ['', Validators.email],
      activo: [true]
    });
    this.cargar();
  }

  cargar() {
    this.loading = true;
    this.svc.obtenerTodo().subscribe({
      next: d => { this.proveedores = d; this.loading = false; this.cdr.detectChanges();},
      error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges();}
    });
  }

  abrirModal() { 
    this.editando = false; 
    this.editId = undefined; 
    this.form.reset(); 
    this.showModal = true; 
  }

  cerrar() { this.showModal = false; }

  editar(p: Proveedor) {
    this.editando = true;
    this.editId = p.idProveedor;
    this.form.patchValue({ ...p, tipoServicio: String(p.tipoServicio) });
    this.showModal = true;
  }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error = '';
    const val = { ...this.form.value, tipoServicio: Number(this.form.value.tipoServicio) };
    const op = this.editando ? this.svc.actualizar(this.editId!, val) : this.svc.crear(val);
    op.subscribe({
      next: () => {
        this.success = this.editando ? 'Proveedor actualizado.' : 'Proveedor creado.';
        this.cerrar();
        this.saving = false;
        this.cargar();
      },
      error: e => { this.error = e.message; this.saving = false; }
    });
  }

  tipoNombre(n: number) { return ['', 'Aerolínea', 'Hotel', 'Tour', 'Traslado', 'Otro'][n] || '—'; }
}
