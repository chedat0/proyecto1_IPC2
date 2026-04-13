import { Component, OnInit, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../../servicios/reporte.service';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

interface Tab {
  id: string;
  icon: string;
  label: string;
}

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css',
})
export class ReportesComponent implements OnInit {

  @ViewChild('reporteContenido') reporteContenido!: ElementRef;

  desde = '';
  hasta = '';
  tabActivo = 'ventas';
  loading = false;
  exportando = false;
  error = '';
  datos: any[] = [];
  datosSingle: any = null;

  tabs: Tab[] = [
    { id: 'ventas', icon: '📊', label: 'Ventas' },
    { id: 'cancelaciones', icon: '❌', label: 'Cancelaciones' },
    { id: 'ganancias', icon: '💰', label: 'Ganancias' },
    { id: 'agente-ventas', icon: '🏆', label: 'Agentes / Ventas' },
    { id: 'agente-ganancias', icon: '💎', label: 'Agentes / Ganancias' },
    { id: 'paquete-mas', icon: '📈', label: 'Más Vendido' },
    { id: 'paquete-menos', icon: '📉', label: 'Menos Vendido' },
    { id: 'ocupacion', icon: '🌎', label: 'Ocupación' },
  ];

  constructor(private svc: ReporteService, private drc: ChangeDetectorRef) { }

  ngOnInit() { this.cargar(); }

  cambiarTab(id: string) { this.tabActivo = id; this.cargar(); }

  limpiarFiltros() { this.desde = ''; this.hasta = ''; this.cargar(); }

  cargar() {
    this.loading = true;
    this.error = '';
    this.datos = [];
    this.datosSingle = null;

    const d = this.desde || undefined;
    const h = this.hasta || undefined;

    const isSingle = ['ganancias', 'paquete-mas', 'paquete-menos'].includes(this.tabActivo);

    const obs = (() => {
      switch (this.tabActivo) {
        case 'ventas': return this.svc.ventas(d, h);
        case 'cancelaciones': return this.svc.cancelaciones(d, h);
        case 'ganancias': return this.svc.ganancias(d, h);
        case 'agente-ventas': return this.svc.agenteVentas(d, h);
        case 'agente-ganancias': return this.svc.agenteGanancias(d, h);
        case 'paquete-mas': return this.svc.paqueteMasVendido(d, h);
        case 'paquete-menos': return this.svc.paqueteMenosVendido(d, h);
        case 'ocupacion': return this.svc.ocupacionDestino(d, h);
        default: return this.svc.ventas(d, h);
      }
    })();

    obs.subscribe({
      next: res => {
        if (isSingle) this.datosSingle = res;
        else this.datos = Array.isArray(res) ? res : [];
        this.loading = false;
        this.drc.detectChanges();
      },
      error: e => { this.error = e.message; this.loading = false; this.drc.detectChanges(); }
    });
  }

  async exportarPDF() {
    this.exportando = true;
    try {
      const elemento = this.reporteContenido.nativeElement;
      const canvas = await html2canvas(elemento, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff'
      });

      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const imgWidth = pageWidth - 20;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      // Título del reporte
      const tabLabel = this.tabs.find(t => t.id === this.tabActivo)?.label || this.tabActivo;
      const fecha = new Date().toLocaleDateString('es-GT');
      const periodo = this.desde && this.hasta
        ? `Período: ${this.desde} al ${this.hasta}`
        : 'Todos los registros';

      pdf.setFontSize(16);
      pdf.setTextColor(13, 33, 64);
      pdf.text(`Reporte: ${tabLabel}`, 10, 14);
      pdf.setFontSize(9);
      pdf.setTextColor(96, 108, 122);
      pdf.text(`${periodo}   |   Generado: ${fecha}`, 10, 20);
      pdf.text('Horizontes Sin Límites — Agencia de Viajes', 10, 25);
      
      pdf.setDrawColor(13, 33, 64);
      pdf.line(10, 27, pageWidth - 10, 27);

      // Imagen del contenido
      let yOffset = 31;
      let remaining = imgHeight;
      let srcY = 0;

      while (remaining > 0) {
        const sliceHeight = Math.min(remaining, pageHeight - yOffset - 10);
        const sliceCanvas = document.createElement('canvas');
        sliceCanvas.width = canvas.width;
        sliceCanvas.height = (sliceHeight / imgWidth) * canvas.width;
        const ctx = sliceCanvas.getContext('2d')!;
        ctx.drawImage(canvas, 0, srcY * (canvas.width / imgWidth), canvas.width, sliceCanvas.height,
          0, 0, canvas.width, sliceCanvas.height);
        pdf.addImage(sliceCanvas.toDataURL('image/png'), 'PNG', 10, yOffset, imgWidth, sliceHeight);
        remaining -= sliceHeight;
        srcY += sliceHeight;
        if (remaining > 0) { pdf.addPage(); yOffset = 10; }
      }

      pdf.save(`reporte-${this.tabActivo}-${fecha.replace(/\//g, '-')}.pdf`);
    } catch (e) {
      this.error = 'Error al generar el PDF.';
    } finally {
      this.exportando = false;
    }
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
