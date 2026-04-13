import { Component, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { CargaService } from '../../../servicios/carga.service';

@Component({
  selector: 'app-carga',
  imports: [CommonModule],
  templateUrl: './carga.html',
  styleUrl: './carga.css',
})
export class CargaComponent {
  archivoSeleccionado?: File;
  loading   = false;
  resultado: any = null;
  dragOver  = false;

  private lineasOriginales: string[] = [];

  constructor(private svc: CargaService, private cdr: ChangeDetectorRef) {}

  onFileSelected(event: any) {
    const file = event.target.files?.[0];
    if (file) this.seleccionarArchivo(file);
  }

  onDragOver(e: DragEvent) {
    e.preventDefault();
    this.dragOver = true;
  }

  onDragLeave() {
    this.dragOver = false;
  }

  onDrop(e: DragEvent) {
    e.preventDefault();
    this.dragOver = false;
    const file = e.dataTransfer?.files?.[0];
    if (file) this.seleccionarArchivo(file);
  }

  seleccionarArchivo(file: File) {
    if (!file.name.endsWith('.txt')) {
      alert('Solo se aceptan archivos .txt');
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const texto = e.target?.result as string;
      this.lineasOriginales = texto.split('\n');
    };
    reader.readAsText(file, 'UTF-8');

    this.archivoSeleccionado = file;
    this.resultado = null;
    this.cdr.detectChanges();
  }

  quitarArchivo() {
    this.archivoSeleccionado = undefined;
    this.resultado = null;
    this.cdr.detectChanges();
  }

  procesarArchivo() {
    if (!this.archivoSeleccionado) return;
    this.loading   = true;
    this.resultado = null;
    this.svc.cargarArchivo(this.archivoSeleccionado).subscribe({
      next:  res => { this.resultado = res; this.loading = false; this.cdr.detectChanges();},
      error: e   => {
        this.resultado = {
          registrosProcesados: 0,
          registrosExitosos:   0,
          registrosError:      1,
          errores:             [e.message]
        };
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  descargarLineasConError() {
    if (!this.resultado?.errores?.length) return;

    const lineasFallidas: string[] = [];

    lineasFallidas.push('# ============================================');
    lineasFallidas.push('# ARCHIVO DE CORRECCIONES');
    lineasFallidas.push('# Contiene solo las líneas que fallaron.');
    lineasFallidas.push('# Corrígelas y vuelve a subir este archivo.');
    lineasFallidas.push('# ============================================');
    lineasFallidas.push('');

    for (const error of this.resultado.errores) {
      const match = error.match(/Línea (\d+)/);
      if (match) {
        const numLinea = parseInt(match[1]) - 1;
        const lineaOriginal = this.lineasOriginales[numLinea]?.trim();
        if (lineaOriginal) {
          lineasFallidas.push(`# ERROR: ${error.replace(/Línea \d+ \[.*?\]: /, '')}`);
          lineasFallidas.push(lineaOriginal);
          lineasFallidas.push('');
        }
      }
    }

    
    const contenido = lineasFallidas.join('\n');
    const blob = new Blob([contenido], { type: 'text/plain;charset=utf-8' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = 'correcciones.txt';
    a.click();
    URL.revokeObjectURL(url);
  }

  get porcentajeExito() {
    if (!this.resultado?.registrosProcesados) return 0;
    return Math.round((this.resultado.registrosExitosos / this.resultado.registrosProcesados) * 100);
  }
}
