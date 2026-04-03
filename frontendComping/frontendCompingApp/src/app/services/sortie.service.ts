import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SortieRequest, SortieResponse } from '../models/sortie.model';
import { ParticipationDTO } from '../models/participation.model';

@Injectable({
    providedIn: 'root'
})
export class SortieService {
    private apiUrl = 'http://localhost:8087/api/sorties';

    constructor(private http: HttpClient) {}

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('authToken');
        return new HttpHeaders({
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        });
    }

    getAllSorties(): Observable<SortieResponse[]> {
        return this.http.get<SortieResponse[]>(`${this.apiUrl}`, {
            headers: this.getHeaders()
        });
    }

    getSortieById(id: string): Observable<SortieResponse> {
        return this.http.get<SortieResponse>(`${this.apiUrl}/${id}`, {
            headers: this.getHeaders()
        });
    }

    createSortie(sortie: SortieRequest): Observable<SortieResponse> {
        return this.http.post<SortieResponse>(`${this.apiUrl}`, sortie, {
            headers: this.getHeaders()
        });
    }

    updateSortie(id: string, sortie: SortieRequest): Observable<SortieResponse> {
        return this.http.put<SortieResponse>(`${this.apiUrl}/${id}`, sortie, {
            headers: this.getHeaders()
        });
    }

    deleteSortie(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`, {
            headers: this.getHeaders()
        });
    }

    // ✅ Méthode appelée par le composant
    inscrire(sortieId: string): Observable<ParticipationDTO> {
        const userId = localStorage.getItem('userId');
        const userNom = localStorage.getItem('userNom');
        const userEmail = localStorage.getItem('userEmail');
        
        const params = new HttpParams()
            .set('utilisateurId', userId || '')
            .set('utilisateurNom', userNom || '')
            .set('utilisateurEmail', userEmail || '');
            
        return this.http.post<ParticipationDTO>(`${this.apiUrl}/${sortieId}/inscription`, null, { 
            headers: this.getHeaders(),
            params 
        });
    }

    // ✅ Méthode appelée par le composant
    desinscrire(sortieId: string): Observable<void> {
        const userId = localStorage.getItem('userId');
        return this.http.delete<void>(`${this.apiUrl}/${sortieId}/inscription/${userId}`, {
            headers: this.getHeaders()
        });
    }

    getProchainesSorties(): Observable<SortieResponse[]> {
        return this.http.get<SortieResponse[]>(`${this.apiUrl}/prochaines`, {
            headers: this.getHeaders()
        });
    }
}