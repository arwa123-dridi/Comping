// ✅ Version corrigée avec headers
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EquipeRequest, EquipeResponse } from '../models/equipe.model';

@Injectable({
    providedIn: 'root'
})
export class EquipeService {
    private apiUrl = 'http://localhost:8087/api/equipes';

    constructor(private http: HttpClient) {}

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('authToken');
        return new HttpHeaders({
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        });
    }

    getAllEquipes(): Observable<EquipeResponse[]> {
        return this.http.get<EquipeResponse[]>(`${this.apiUrl}`, {
            headers: this.getHeaders()
        });
    }

    getEquipeById(id: string): Observable<EquipeResponse> {
        return this.http.get<EquipeResponse>(`${this.apiUrl}/${id}`, {
            headers: this.getHeaders()
        });
    }

    createEquipe(equipe: EquipeRequest): Observable<EquipeResponse> {
        return this.http.post<EquipeResponse>(`${this.apiUrl}`, equipe, {
            headers: this.getHeaders()
        });
    }

    updateEquipe(id: string, equipe: EquipeRequest): Observable<EquipeResponse> {
        return this.http.put<EquipeResponse>(`${this.apiUrl}/${id}`, equipe, {
            headers: this.getHeaders()
        });
    }

    deleteEquipe(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`, {
            headers: this.getHeaders()
        });
    }

    ajouterMembre(equipeId: string, userId: string, userNom: string): Observable<EquipeResponse> {
        const params = new HttpParams().set('utilisateurNom', userNom);
        return this.http.post<EquipeResponse>(`${this.apiUrl}/${equipeId}/membres/${userId}`, null, { 
            headers: this.getHeaders(),
            params 
        });
    }

    retirerMembre(equipeId: string, userId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${equipeId}/membres/${userId}`, {
            headers: this.getHeaders()
        });
    }
}