import { Component } from '@angular/core';
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

  constructor(private svc: CargaService) {}

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
    this.archivoSeleccionado = file;
    this.resultado = null;
  }

  quitarArchivo() {
    this.archivoSeleccionado = undefined;
    this.resultado = null;
  }

  procesarArchivo() {
    if (!this.archivoSeleccionado) return;
    this.loading   = true;
    this.resultado = null;
    this.svc.cargarArchivo(this.archivoSeleccionado).subscribe({
      next:  res => { this.resultado = res; this.loading = false; },
      error: e   => {
        this.resultado = {
          registrosProcesados: 0,
          registrosExitosos:   0,
          registrosError:      1,
          errores:             [e.message]
        };
        this.loading = false;
      }
    });
  }

  get porcentajeExito() {
    if (!this.resultado?.registrosProcesados) return 0;
    return Math.round((this.resultado.registrosExitosos / this.resultado.registrosProcesados) * 100);
  }
}
