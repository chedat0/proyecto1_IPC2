import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsuarioService } from '../../../servicios/usuario.service';
import { Usuario } from '../../../modelos/usuario';


@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  loading = true; 
  saving = false; 
  error = ''; 
  success = '';
  showModal = false; 
  editando = false; 
  editId?: number;
  form!: FormGroup;

  constructor(private svc: UsuarioService, private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      usuario: ['', Validators.required],
      password: ['', Validators.minLength(6)],
      nombreCompleto: ['', Validators.required],
      rol: ['', Validators.required],
      activo: [true]
    });
    this.cargar();
  }

  cargar() { 
    this.loading = true; 
    this.svc.obtenerTodo().subscribe({ next: u => { 
      this.usuarios = u; 
      this.loading = false; 
    }, 
    error: e => { 
      this.error = e.message; 
      this.loading = false; 
    }}); 
  }

  abrirModal() { 
    this.editando = false; 
    this.editId = undefined; 
    this.form.reset({ activo: true }); 
    this.form.get('password')?.setValidators([Validators.required, Validators.minLength(6)]); 
    this.showModal = true; 
  }

  editar(u: Usuario) { 
    this.editando = true; 
    this.editId = u.idUsuario; 
    this.form.patchValue({ ...u, password: '', rol: String(u.rol) }); 
    this.form.get('password')?.clearValidators(); 
    this.form.get('password')?.updateValueAndValidity(); 
    this.showModal = true; 
  }

  cerrar() { this.showModal = false; }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true; this.error = '';
    const val = { ...this.form.value, rol: Number(this.form.value.rol) };
    if (this.editando && !val.password) delete val.password;
    const op = this.editando ? this.svc.actualizar(this.editId!, val) : this.svc.crear(val);
    op.subscribe({ next: () => { 
      this.success = this.editando ? 'Usuario actualizado.' : 'Usuario creado.'; 
      this.cerrar(); 
      this.saving = false; 
      this.cargar(); 
    }, 
      error: e => { this.error = e.message; this.saving = false; } });
  }

  desactivar(u: Usuario) {
    if (!confirm(`¿Desactivar al usuario "${u.usuario}"?`)) return;
    this.svc.desactivar(u.idUsuario!).subscribe({ next: () => { 
      this.success = 'Usuario desactivado.'; 
      this.cargar(); 
    }, 
    error: e => this.error = e.message });
  }

  rolNombre(r: number) { 
    return ['', 'Atención Cliente', 'Operaciones', 'Administrador'][r] || '—'; 
  }

  rolBadge(r: number)  { 
    return r === 3 ? 'badge-admin' : r === 2 ? 'badge-operaciones' : 'badge-cliente'; 
  }
}
