import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthUserService {

  private API = "http://localhost:8087/api/auth";

  constructor(private http: HttpClient) {}

  // 👤 GET ALL LIVREURS
  getLivreurs(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API}/livreurs`);
  }

}