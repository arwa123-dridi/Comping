import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Urgence {
  id?: string;
  titre: string;
  description: string;
  dateCreation?: Date;
  dateExpiration?: Date;
  statut?: string; // ATTENDANT, ACCEPTE, REJETEE, COMPLETEE
  siteCampingId: string;
  userId: string;
  niveauUrgence: string; // IMMEDIATE, TRES_URGENT, URGENT, NORMAL, BASSE
  estimatedMinutesBeforeResolution: number;
  assigneId?: string;
  dateAssignment?: Date;
  resolution?: string;
  dateResolution?: Date;
  impactScore: number;
  estimatedCost?: number;
  affectedUsers?: string[];
  categorie: string;
  priorite: string;
  reporterId: string;
  tags?: string[];
  attachments?: string[];
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  location?: string;
  latitude?: number;
  longitude?: number;
  dateModification?: Date;
  modifiedBy?: string;
  numberOfEscalations?: number;
  notes?: string;
  comments?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UrgenceService {
  private readonly apiUrl = 'http://localhost:8087/api/urgences';
  private urgencesSubject = new BehaviorSubject<Urgence[]>([]);
  public urgences$ = this.urgencesSubject.asObservable();
  
  private selectedUrgenceSubject = new BehaviorSubject<Urgence | null>(null);
  public selectedUrgence$ = this.selectedUrgenceSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadAllUrgences();
  }

  private loadAllUrgences(): void {
    this.getAll().subscribe(urgences => {
      this.urgencesSubject.next(urgences);
    });
  }

  creer(urgence: Urgence): Observable<Urgence> {
    return this.http.post<Urgence>(this.apiUrl, urgence)
      .pipe(tap(newUrgence => {
        const current = this.urgencesSubject.value;
        this.urgencesSubject.next([...current, newUrgence]);
      }));
  }

  getById(id: string): Observable<Urgence> {
    return this.http.get<Urgence>(`${this.apiUrl}/${id}`)
      .pipe(tap(urgence => this.selectedUrgenceSubject.next(urgence)));
  }

  getAll(): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(this.apiUrl);
  }

  getBySite(siteCampingId: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/site/${siteCampingId}`);
  }

  getByStatut(statut: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getByNiveau(niveau: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/niveau/${niveau}`);
  }

  getByAssignee(assigneId: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/assignee/${assigneId}`);
  }

  getByUser(userId: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/utilisateur/${userId}`);
  }

  getByCategory(categorie: string): Observable<Urgence[]> {
    return this.http.get<Urgence[]>(`${this.apiUrl}/categorie/${categorie}`);
  }

  update(id: string, urgence: Urgence): Observable<Urgence> {
    return this.http.put<Urgence>(`${this.apiUrl}/${id}`, urgence)
      .pipe(tap(updated => {
        const current = this.urgencesSubject.value;
        const index = current.findIndex(u => u.id === id);
        if (index > -1) {
          current[index] = updated;
          this.urgencesSubject.next([...current]);
        }
      }));
  }

  updateStatut(id: string, statut: string): Observable<Urgence> {
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/statut/${statut}`, {});
  }

  assign(id: string, assigneId: string): Observable<Urgence> {
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/assigner/${assigneId}`, {});
  }

  resolve(id: string, resolution: string): Observable<Urgence> {
    let params = new HttpParams().set('resolution', resolution);
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/resoudre`, {}, { params });
  }

  reject(id: string, reason: string): Observable<Urgence> {
    let params = new HttpParams().set('reason', reason);
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/rejeter`, {}, { params });
  }

  complete(id: string): Observable<Urgence> {
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/completer`, {});
  }

  addComment(id: string, comment: string): Observable<Urgence> {
    let params = new HttpParams().set('comment', comment);
    return this.http.patch<Urgence>(`${this.apiUrl}/${id}/commentaire`, {}, { params });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(tap(() => {
        const current = this.urgencesSubject.value;
        this.urgencesSubject.next(current.filter(u => u.id !== id));
      }));
  }

  getCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/count`);
  }

  getCountByStatut(statut: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/count/${statut}`);
  }

  setSelectedUrgence(urgence: Urgence | null): void {
    this.selectedUrgenceSubject.next(urgence);
  }

  getSelectedUrgence(): Urgence | null {
    return this.selectedUrgenceSubject.value;
  }
}
