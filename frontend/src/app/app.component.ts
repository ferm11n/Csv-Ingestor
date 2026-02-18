import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

// Módulos de Material Design
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDividerModule,
    MatCardModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'csv-ingestor-front';
  
  // Estado del archivo
  currentFileId: string | null = null;
  headers: string[] = [];
  rows: string[][] = [];
  message: string = '';
  showTable: boolean = false;

  // Estado de la selección (-1 significa "Ninguna columna seleccionada")
  selectedColumnIndex: number = -1; 

  constructor(private http: HttpClient, private cd: ChangeDetectorRef) {}

  // 1. SUBIR ARCHIVO
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.uploadFile(file);
    }
  }

  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    this.message = '⏳ Subiendo archivo...';
    
    this.http.post<any>('http://localhost:8080/api/files/upload', formData).subscribe({
      next: (response) => {
        this.currentFileId = response.fileId;
        this.headers = response.headers;
        this.rows = response.rows;
        this.showTable = true;
        this.selectedColumnIndex = -1; // Reset selección
        this.message = '✅ Archivo cargado correctamente.';
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.message = '❌ Error al subir el archivo.';
      }
    });
  }

  // 2. SELECCIONAR COLUMNA (Click en Header)
  selectColumn(index: number) {
    // Si tocas la misma, se deselecciona (-1). Si no, marca la nueva.
    if (this.selectedColumnIndex === index) {
      this.selectedColumnIndex = -1;
    } else {
      this.selectedColumnIndex = index;
    }
  }

  // 3. APLICAR REGLA (Con columna opcional)
  applyRule(rule: string) {
    if (!this.currentFileId) return;

    //debugg
    this.message = this.selectedColumnIndex === -1 
      ? `Aplicando '${rule}' a TODO el archivo...`
      : `Aplicando '${rule}' a columna '${this.headers[this.selectedColumnIndex]}'...`;
    
    const url = `http://localhost:8080/api/files/${this.currentFileId}/clean?rule=${rule}&colIndex=${this.selectedColumnIndex}`;

    this.http.post<any>(url, {}).subscribe({
      next: (response) => {
        this.rows = response.rows; // Actualizamos solo los datos
        this.message = '✨ ¡Limpieza aplicada con éxito!';
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error(error);
        this.message = '❌ Ocurrió un error al aplicar la regla.';
      }
    });
  }

  // 4. DESHACER / RESETEAR 
  resetFile() {
    if (!this.currentFileId) return;

    if(confirm('¿Estás seguro? Se perderán todos los cambios actuales y volverás al archivo original.')) {
        this.message = 'Restaurando versión original...';
        
        const url = `http://localhost:8080/api/files/${this.currentFileId}/reset`;
        
        this.http.post<any>(url, {}).subscribe({
          next: (response) => {
            this.rows = response.rows;
            this.selectedColumnIndex = -1;
            this.message = 'Archivo restaurado al original.';
            this.cd.detectChanges();
          },
          error: (e) => {
            console.error(e);
            this.message = '❌ Error al restaurar archivo.';
          }
        });
    }
  }

  // 5. DESCARGAR
  downloadFile() {
    if (!this.currentFileId) return;
    const downloadUrl = `http://localhost:8080/api/files/download/${this.currentFileId}.csv`;
    window.location.href = downloadUrl;
  }
}