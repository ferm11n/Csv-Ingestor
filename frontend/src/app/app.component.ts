import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatButtonModule, MatIconModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Data Washer';
  message = ''; 

  // Inyectamos el HttpClient
  constructor(private http: HttpClient) {}

  onFileSelected(event: any) {
    const file: File = event.target.files[0];

    if (file) {
      this.message = `Subiendo: ${file.name}... ⏳`;
      this.uploadFile(file);
    }
  }

  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    // 🚀 Enviamos el archivo al Backend (Puerto 8080)
    this.http.post<any>('http://localhost:8080/api/files/upload', formData).subscribe({
      next: (response) => {
        console.log('Éxito:', response);
        this.message = `✅ ¡Archivo subido! ID: ${response.fileId}`;
      },
      error: (error) => {
        console.error('Error:', error);
        this.message = '❌ Error al subir el archivo. Revisa la consola.';
      }
    });
  }
}