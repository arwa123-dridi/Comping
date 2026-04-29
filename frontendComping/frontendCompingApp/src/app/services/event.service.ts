import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Event as EventModel } from '../models/event.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private apiUrl = 'http://localhost:8087/api/events';
 
  constructor(private http: HttpClient) {}


  getAllEvents(): Observable<EventModel[]> {
    const token = localStorage.getItem('authToken');
console.log("TOKEN:", token);
  return this.http.get<EventModel[]>(this.apiUrl, {
    headers: token
      ? { Authorization: `Bearer ${token}` }
      : {}
  })
  }

  toggleStatut(id: string, statut: string): Observable<EventModel> {
    return this.http.patch<EventModel>(`${this.apiUrl}/${id}/statut`, { statut });
  }

deleteEvent(id: string): Observable<void> {
  const token = localStorage.getItem('authToken');

  return this.http.delete<void>(`${this.apiUrl}/${id}`, {
    headers: token
      ? { Authorization: `Bearer ${token}` }
      : {}
  });
}
createEvent(event: EventModel): Observable<EventModel> {
  const token = localStorage.getItem('authToken');

  return this.http.post<EventModel>(
    `${this.apiUrl}/CREATE/EVENT`,
    event,
    {
      headers: token
        ? { Authorization: `Bearer ${token}` }
        : {}
    }
  );
}
updateEvent(id: string, event: EventModel): Observable<EventModel> {
  const token = localStorage.getItem('authToken');

  return this.http.put<EventModel>(
    `${this.apiUrl}/UPDATE/${id}`, 
    event,
    {
      headers: token
        ? { Authorization: `Bearer ${token}` }
        : {}
    }
  );
}
getEventById(id: string): Observable<EventModel> {
  const token = localStorage.getItem('authToken');

  return this.http.get<EventModel>(
    `${this.apiUrl}/EVENTBYID/${id}`,
    {
      headers: token
        ? { Authorization: `Bearer ${token}` }
        : {}
    }
  );
}
 countValide(): Observable<number> {
  const token = localStorage.getItem('authToken');
    return this.http.get<number>(`${this.apiUrl}/count/valide`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
  }

  countTermine(): Observable<number> {
    const token = localStorage.getItem('authToken');
    return this.http.get<number>(`${this.apiUrl}/count/termine`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
  }
   countAnnule(): Observable<number> {
    const token = localStorage.getItem('authToken');
    return this.http.get<number>(`${this.apiUrl}/count/annule`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    });
  }
}