import { Component, ChangeDetectorRef } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ 
    MatToolbarModule, 
    MatButtonModule, 
    MatIconModule,
    MatTableModule,
    CommonModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Data Washer';
  message = '';

  //variables para la tablita
  headers: string[] = [];
  rows: any[] = [];
  showTable = false;

  //Aun que esten private, igual se crean automaticamente
  constructor(private http: HttpClient, private cd: ChangeDetectorRef) {}

  onFileSelected(event: any) {
    const file: File = event.target.files[0];

    if (file) {
      this.message = `Subiendo: ${file.name}... ⏳`;
      this.uploadFile(file);
      
      event.target.value = ''; 
    }
  }

  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    this.http.post<any>('http://localhost:8080/api/files/upload', formData).subscribe({
      next: (response) => {
        //console.log('Datos recibidos:', response); 
        this.headers = response.headers;
        this.rows = response.rows;
        this.showTable = true;

        this.message = `✅ ¡Archivo subido con éxito! ID: ${response.fileId}`;
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error:', error);
        this.message = '❌ Error al subir el archivo.';
        this.showTable = false;
        this.cd.detectChanges();
      }
    });
  }

  //los placeholder
  applyRule(ruleType: string) {
    alert("Aca va la regla: ${ruleType}");
  }

  downloadFile() {
    alert("Aca se descarga el archivo limpio")
  }
}