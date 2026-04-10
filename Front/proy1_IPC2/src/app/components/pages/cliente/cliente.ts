import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../servicios/cliente.service';
import { Cliente } from '../../../modelos/cliente';

@Component({
  selector: 'app-cliente',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cliente.html',
  styleUrl: './cliente.css',
})
export class ClienteComponent implements OnInit{

  clientes: Cliente[] = [];
  loading  = true;
  error    = '';
  searchQ  = '';
  private debounce: any;

  constructor(private svc: ClienteService) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.loading = true;
    this.svc.obtenerTodos().subscribe({
      next:  data => { this.clientes = data; this.loading = false; },
      error: e    => { this.error = e.message; this.loading = false; }
    });
  }

  onSearch() {
    clearTimeout(this.debounce);
    this.debounce = setTimeout(() => {
      if (this.searchQ.trim().length < 2) { this.cargar(); return; }
      this.loading = true;
      this.svc.buscar(this.searchQ.trim()).subscribe({
        next:  data => { this.clientes = data; this.loading = false; },
        error: e    => { this.error = e.message; this.loading = false; }
      });
    }, 350);
  }
}
