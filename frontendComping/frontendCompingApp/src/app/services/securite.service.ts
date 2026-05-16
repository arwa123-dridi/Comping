import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';

export interface Securite {
  id?: string;
  titre: string;
  description: string;
  dateCreation?: Date;
  dateDebut?: Date;
  dateFin?: Date;
  statut?: string; // PLANIFIEE, EN_COURS, COMPLETEE, ANNULEE
  siteCampingId: string;
  typeMesure: string;
  niveauSecurite: string;
  zoneSecurisee: string;
  responsableId: string;
  teamMemberIds?: string[];
  dateAssignment?: Date;
  equipmentUsed?: string[];
  resourcesNeeded?: string[];
  budgetAlloue?: number;
  budgetUtilise?: number;
  relatedIncidentIds?: string[];
  relatedAlerteIds?: string[];
  numberOfIncidentsDetected?: number;
  conformiteAudit?: boolean;
  certificateNumber?: string;
  certificateExpiry?: Date;
  complianceStatus?: string;
  monitoringType: string;
  monitoringLocations?: string[];
  lastMonitoringDate?: Date;
  monitoringStatus?: string;
  securityScore: number;
  riskScore: number;
  riskLevel?: string;
  findings?: string[];
  recommendations?: string[];
  actionTaken?: string;
  dateActionTaken?: Date;
  reportedBy?: string;
  approvedBy?: string;
  notes?: string;
  dateModification?: Date;
  weatherAlert?: WeatherAlert;
  weatherCondition?: string;
}

export interface WeatherAlert {
  type: 'STORM' | 'FLOOD' | 'HEAT' | 'COLD' | 'WIND' | 'GENERAL';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  startTime?: Date;
  endTime?: Date;
  affectedZones?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class SecuriteService {
  private readonly apiUrl = 'http://localhost:8087/api/securite';
  private securitesSubject = new BehaviorSubject<Securite[]>([]);
  public securites$ = this.securitesSubject.asObservable();
  
  private selectedSecuriteSubject = new BehaviorSubject<Securite | null>(null);
  public selectedSecurite$ = this.selectedSecuriteSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadAllSecurites();
  }

  private loadAllSecurites(): void {
    this.getAll().subscribe(securites => {
      this.securitesSubject.next(securites);
    });
  }

  creer(securite: Securite): Observable<Securite> {
    return this.http.post<Securite>(this.apiUrl, securite)
      .pipe(tap(newSecurite => {
        const current = this.securitesSubject.value;
        this.securitesSubject.next([...current, newSecurite]);
      }));
  }

  getById(id: string): Observable<Securite> {
    return this.http.get<Securite>(`${this.apiUrl}/${id}`)
      .pipe(tap(securite => this.selectedSecuriteSubject.next(securite)));
  }

  getAll(): Observable<Securite[]> {
    return this.http.get<Securite[]>(this.apiUrl);
  }

  getBySite(siteCampingId: string): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/site/${siteCampingId}`);
  }

  getByStatut(statut: string): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/statut/${statut}`);
  }

  getByNiveau(niveau: string): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/niveau/${niveau}`);
  }

  getByRiskLevel(risque: string): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/risque/${risque}`);
  }

  getByResponsable(responsableId: string): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/responsable/${responsableId}`);
  }

  getHighRiskMeasures(): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/haut-risque`);
  }

  getLowSecurityScore(threshold: number): Observable<Securite[]> {
    return this.http.get<Securite[]>(`${this.apiUrl}/securite-faible/${threshold}`);
  }

  update(id: string, securite: Securite): Observable<Securite> {
    return this.http.put<Securite>(`${this.apiUrl}/${id}`, securite)
      .pipe(tap(updated => {
        const current = this.securitesSubject.value;
        const index = current.findIndex(s => s.id === id);
        if (index > -1) {
          current[index] = updated;
          this.securitesSubject.next([...current]);
        }
      }));
  }

  updateStatut(id: string, statut: string): Observable<Securite> {
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/statut/${statut}`, {});
  }

  assignTeamMember(id: string, memberId: string): Observable<Securite> {
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/equipe/${memberId}`, {});
  }

  removeTeamMember(id: string, memberId: string): Observable<Securite> {
    return this.http.delete<Securite>(`${this.apiUrl}/${id}/equipe/${memberId}`);
  }

  recordFinding(id: string, finding: string): Observable<Securite> {
    let params = new HttpParams().set('finding', finding);
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/constat`, {}, { params });
  }

  addRecommendation(id: string, recommendation: string): Observable<Securite> {
    let params = new HttpParams().set('recommendation', recommendation);
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/recommandation`, {}, { params });
  }

  recordIncident(id: string, incidentId: string): Observable<Securite> {
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/incident/${incidentId}`, {});
  }

  completeMonitoring(id: string): Observable<Securite> {
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/monitoring-complete`, {});
  }

  updateBudget(id: string, amount: number): Observable<Securite> {
    let params = new HttpParams().set('amount', amount.toString());
    return this.http.patch<Securite>(`${this.apiUrl}/${id}/budget`, {}, { params });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(tap(() => {
        const current = this.securitesSubject.value;
        this.securitesSubject.next(current.filter(s => s.id !== id));
      }));
  }

  getCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/count`);
  }

  getCountByStatut(statut: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/count/${statut}`);
  }

  setSelectedSecurite(securite: Securite | null): void {
    this.selectedSecuriteSubject.next(securite);
  }

  getSelectedSecurite(): Securite | null {
    return this.selectedSecuriteSubject.value;
  }
}
