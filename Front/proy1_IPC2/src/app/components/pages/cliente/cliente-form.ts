import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClienteService } from '../../../servicios/cliente.service';
import { Cliente } from '../../../modelos/cliente';

@Component({
    selector: 'app-cliente-form',   
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './cliente-form.html',
    styleUrl: './cliente-form.css'
})
export class ClienteFormComponent implements OnInit {
    form!: FormGroup;
    loading = false;
    editMode = false;
    error = '';
    success = '';
    clienteId?: number;

    constructor(
        private fb: FormBuilder,
        private svc: ClienteService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        this.form = this.fb.group({
            dpiPasaporte: ['', Validators.required],
            nombreCompleto: ['', Validators.required],
            fechaNacimiento: ['', Validators.required],
            telefono: [''],
            email: ['', Validators.email],
            nacionalidad: ['']
        });

        const id = this.route.snapshot.paramMap.get('id');
        if (id && id !== 'nuevo') {
            this.editMode = true;
            this.clienteId = Number(id);
            this.svc.obtenerPorId(this.clienteId).subscribe({
                next: c => this.form.patchValue(c),
                error: e => this.error = e.message
            });
        }
    }

    get f() { return this.form.controls; }

    onSubmit() {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading = true;
        this.error = '';
        this.success = '';
        const data: Cliente = this.form.value;
        const op = this.editMode
            ? this.svc.actualizar(this.clienteId!, data)
            : this.svc.crear(data);
        op.subscribe({
            next: () => {
                this.success = this.editMode
                    ? 'Cliente actualizado correctamente.'
                    : 'Cliente registrado correctamente.';
                this.loading = false;
                setTimeout(() => this.router.navigate(['/clientes']), 1200);
            },
            error: e => { this.error = e.message; this.loading = false; }
        });
    }

    regresar() { this.router.navigate(['/clientes']); }
}