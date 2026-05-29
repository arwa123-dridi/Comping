import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UploadService {
  private readonly apiUrl = 'http://localhost:8087/api/upload/image';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    // Note: Pour multipart/form-data, on ne met PAS explicitement le Content-Type
    // car le navigateur le fait lui-même avec le boundary correct.
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  uploadImage(file: File): Observable<{ url: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ url: string }>(this.apiUrl, formData, {
      headers: this.getHeaders()
    });
  }
}
