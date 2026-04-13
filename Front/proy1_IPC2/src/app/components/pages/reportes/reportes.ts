import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../servicios/reporte.service';

interface Tab {
  id:    string;
  icon:  string;
  label: string;
}

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class ReportesComponent implements OnInit {

  desde = '';
  hasta = '';
  tabActivo = 'ventas';
  loading   = false;
  error     = '';
  datos: any[]  = [];
  datosSingle: any = null;

  tabs: Tab[] = [
    { id: 'ventas',           icon: '📊', label: 'Ventas' },
    { id: 'cancelaciones',    icon: '❌', label: 'Cancelaciones' },
    { id: 'ganancias',        icon: '💰', label: 'Ganancias' },
    { id: 'agente-ventas',    icon: '🏆', label: 'Agentes / Ventas' },
    { id: 'agente-ganancias', icon: '💎', label: 'Agentes / Ganancias' },
    { id: 'paquete-mas',      icon: '📈', label: 'Más Vendido' },
    { id: 'paquete-menos',    icon: '📉', label: 'Menos Vendido' },
    { id: 'ocupacion',        icon: '🌎', label: 'Ocupación' },
  ];

  constructor(private svc: ReporteService, private drc: ChangeDetectorRef) {}

  ngOnInit() { this.cargar(); }

  cambiarTab(id: string) { this.tabActivo = id; this.cargar(); }

  limpiarFiltros() { this.desde = ''; this.hasta = ''; this.cargar(); }

  cargar() {
    this.loading     = true;
    this.error       = '';
    this.datos       = [];
    this.datosSingle = null;

    const d = this.desde || undefined;
    const h = this.hasta || undefined;

    const isSingle = ['ganancias', 'paquete-mas', 'paquete-menos'].includes(this.tabActivo);

    const obs = (() => {
      switch (this.tabActivo) {
        case 'ventas':           return this.svc.ventas(d, h);
        case 'cancelaciones':    return this.svc.cancelaciones(d, h);
        case 'ganancias':        return this.svc.ganancias(d, h);
        case 'agente-ventas':    return this.svc.agenteVentas(d, h);
        case 'agente-ganancias': return this.svc.agenteGanancias(d, h);
        case 'paquete-mas':      return this.svc.paqueteMasVendido(d, h);
        case 'paquete-menos':    return this.svc.paqueteMenosVendido(d, h);
        case 'ocupacion':        return this.svc.ocupacionDestino(d, h);
        default:                 return this.svc.ventas(d, h);
      }
    })();

    obs.subscribe({
      next: res => {
        if (isSingle) this.datosSingle = res;
        else          this.datos       = Array.isArray(res) ? res : [];
        this.loading = false;
        this.drc.detectChanges();
      },
      error: e => { this.error = e.message; this.loading = false; this.drc.detectChanges(); }
    });
  }

  get totalVentas() {
    return this.datos.reduce((s: number, r: any) => s + (Number(r.costo_total) || 0), 0);
  }

  get totalReembolsos() {
    return this.datos.reduce((s: number, r: any) => s + (Number(r.monto_reembolso) || 0), 0);
  }

  get totalPerdidas() {
    return this.datos.reduce((s: number, r: any) => s + (Number(r.perdida_agencia) || 0), 0);
  }

  medallon(i: number) {
    return ['🥇', '🥈', '🥉'][i] || '';
  }
}
