import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activity } from '../models/activity.model';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {

  private baseUrl = 'http://localhost:8087/api/activities';

  constructor(private http: HttpClient) { }

 getAllActivities(): Observable<any[]> {
  const token = localStorage.getItem('authToken');

  return this.http.get<any[]>(`${this.baseUrl}/GetAllActivities`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

   createActivity(activity: Activity): Observable<Activity> {
    const token = localStorage.getItem('authToken');

    return this.http.post<Activity>(
      `${this.baseUrl}/add`,
      activity,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
  }
  deleteActivity(id: string): Observable<void> {
  const token = localStorage.getItem('authToken');

  return this.http.delete<void>(
    `${this.baseUrl}/deleteactivity/${id}`,
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  );
}
updateActivity(id: string, activity: Activity): Observable<Activity> {
  const token = localStorage.getItem('authToken');

  return this.http.put<Activity>(
    `${this.baseUrl}/updateactivity/${id}`,
    activity,
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  );
}
getActivityById(id: string): Observable<Activity> {
  const token = localStorage.getItem('authToken');

  return this.http.get<Activity>(
    `${this.baseUrl}/${id}`,
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  );
}
}