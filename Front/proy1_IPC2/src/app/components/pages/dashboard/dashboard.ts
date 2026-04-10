import { Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../../servicios/dashboard.service';
import { AuthService } from '../../../servicios/auth.service';
import { DashboardStats } from '../../../modelos/dashboard';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;
  error   = '';

  constructor(
    private dashSvc: DashboardService,
    public  auth:    AuthService
  ) {}

  ngOnInit() {
    this.dashSvc.obtenerStats().subscribe({
      next:  s => { this.stats = s; this.loading = false; },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  get usuario() { 
    return this.auth.currentUser; 
  }

  get esAdmin() { 
    return this.auth.isAdmin(); 
  }

  get esClienteOAdmin() { 
    return this.auth.isCliente(); 
  }

  get esOperacionesOAdmin() { 
    return this.auth.isOperaciones(); 
  }
}
