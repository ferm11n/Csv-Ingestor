import { Component, ChangeDetectorRef } from '@angular/core';
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
        console.log('Datos recibidos:', response); 
        
        this.message = `✅ ¡Archivo subido con éxito! ID: ${response.fileId}`;
                
        this.cd.detectChanges();
      },
      error: (error) => {
        console.error('Error:', error);
        this.message = '❌ Error al subir el archivo.';
        this.cd.detectChanges();
      }
    });
  }
}