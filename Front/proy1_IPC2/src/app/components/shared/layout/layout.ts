import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../servicios/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class LayoutComponent implements OnInit {
  sidebarOpen = false;
  year = new Date().getFullYear();

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {}

  get user()               { return this.auth.currentUser; }
  get isAdmin()            { return this.auth.isAdmin(); }
  get isClienteOrAdmin()   { return this.auth.isCliente(); }
  get isOperacionesOrAdmin(){ return this.auth.isOperaciones(); }
  
  get initials() {
    const n = this.user?.nombreCompleto || '';
    return n.split(' ').slice(0,2).map((w:string) => w[0]).join('').toUpperCase();
  }
  get roleBadge() {
    const r = this.user?.rol;
    return r === 3 ? 'badge-admin' : r === 2 ? 'badge-operaciones' : 'badge-cliente';
  }

  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }
  closeMobile()   { if (window.innerWidth <= 768) this.sidebarOpen = false; }

  logout() {
    this.auth.logout().subscribe({ complete: () => this.router.navigate(['/login']) });
  }
}

