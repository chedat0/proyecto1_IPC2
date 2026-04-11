import { Component, OnInit, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservacionService } from '../../../servicios/reservacion.service';
import { Reservacion } from '../../../modelos/reservacion';

@Component({
  selector: 'app-reservaciones',
  imports: [CommonModule, RouterLink],
  templateUrl: './reservaciones.html',
  styleUrl: './reservaciones.css',
})
export class ReservacionesComponent implements OnInit {

  reservaciones: Reservacion[] = [];
  loading = true;
  error   = '';
  filtro  = 'todas';

  constructor(private svc: ReservacionService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.loading = true;
    this.svc.obtenerTodas().subscribe({
      next:  d => { this.reservaciones = d; this.loading = false; this.cdr.detectChanges();},
      error: e => { this.error = e.message; this.loading = false; this.cdr.detectChanges();}
    });
  }

  setFiltro(f: string) {
    this.filtro = f;
    if (f === 'hoy') {
      this.loading = true;
      this.svc.obtenerHoy().subscribe({
        next:  d => { this.reservaciones = d; this.loading = false; },
        error: e => { this.error = e.message; this.loading = false; }
      });
    } else if (f === 'todas') {
      this.cargar();
    }
  }

  get reservacionesFiltradas() {
    if (['PENDIENTE','CONFIRMADA','CANCELADA','COMPLETADA'].includes(this.filtro)) {
      return this.reservaciones.filter(r => r.estado === this.filtro);
    }
    return this.reservaciones;
  }
}
