import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../servicios/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  form: FormGroup;
  loading   = false;
  errorMsg  = '';
  showPass  = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      usuario: ['', Validators.required],
      password: ['', Validators.required]
    });
    if (this.auth.isLoggedIn()) this.router.navigate(['/dashboard']);
  }

  onSubmit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading  = true;
    this.errorMsg = '';
    const { usuario, password } = this.form.value;
    this.auth.login(usuario, password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (e: Error) => {
        this.loading  = false;
        this.errorMsg = e.message || 'Error al iniciar sesión.';
        this.cdr.detectChanges();
      }
    });
  }
}
