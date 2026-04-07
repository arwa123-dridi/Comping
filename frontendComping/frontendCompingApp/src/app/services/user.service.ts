import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { User } from '../models/user.model';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class UserService {
   private apiUrl = 'http://localhost:8087/api/users';

   constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    const token = localStorage.getItem('token');

  return this.http.get<User[]>(this.apiUrl, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  }
 deleteUser(id: string): Observable<any> {
   const token = localStorage.getItem('token');

  return this.http.delete(`${this.apiUrl}/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`
    },
    responseType: 'text' as 'json'
  });
 }
updateUserStatus(id: string, statut: boolean) {
  const token = localStorage.getItem('token');

  return this.http.patch(
    `${this.apiUrl}/${id}/status`,
    { statut },
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  );
}

}
