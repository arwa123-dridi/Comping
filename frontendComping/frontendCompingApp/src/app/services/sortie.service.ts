import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SortieRequest, SortieResponse } from '../models/sortie.model';
import { ParticipationDTO } from '../models/participation.model';
import { SortieScoreDTO } from '../models/sortie-score.model';


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

    //  Méthode appelée par le composant
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

    //  Méthode appelée par le composant
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

    //   Recommandations basées sur l'historique utilisateur
  private recommandationsUrl = 'http://localhost:8087/api/recommandations';

getRecommandations(userId: string): Observable<SortieScoreDTO[]> {
    return this.http.get<SortieScoreDTO[]>(
        `${this.recommandationsUrl}/sorties?userId=${userId}`,
        { headers: this.getHeaders() }
    );
}
    
    //  recommandations calculées
    getRecommandationsLocales(userId: string, sorties: SortieResponse[]): SortieResponse[] {
        const participatedIds: string[] = [];
        const participatedDifficulties: string[] = [];

        sorties.forEach(s => {
            if (s.participantIds?.includes(userId)) {
                participatedIds.push(s.id);
                participatedDifficulties.push(s.difficulte);
            }
            
        });

        if (participatedIds.length === 0) {
            // Aucun historique → recommander les sorties FACILE avec places disponibles
            return sorties
                .filter(s => s.difficulte === 'FACILE' && s.placesDisponibles > 0)
                .slice(0, 4);
        }

        // Trouver la difficulté la plus pratiquée
        const freq: Record<string, number> = {};
        participatedDifficulties.forEach(d => freq[d] = (freq[d] || 0) + 1);
        const topDiff = Object.entries(freq).sort((a, b) => b[1] - a[1])[0][0];

        // Recommander sorties similaires non encore participées
        return sorties
            .filter(s => !participatedIds.includes(s.id) && s.placesDisponibles > 0)
            .sort((a, b) => {
                const aScore = a.difficulte === topDiff ? 2 : 1;
                const bScore = b.difficulte === topDiff ? 2 : 1;
                return bScore - aScore;
            })
            .slice(0, 4);
    }
}