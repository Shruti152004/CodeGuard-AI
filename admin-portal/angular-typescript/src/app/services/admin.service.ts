import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/admin';

  private getHeaders(): HttpHeaders {
    // Add token if present from login cache (we can retrieve it from localStorage shared context)
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token || ''}`
    });
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/users`, { headers: this.getHeaders() });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/users/${userId}/role`, { role }, { headers: this.getHeaders() });
  }

  getOrganizations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/organizations`, { headers: this.getHeaders() });
  }

  createOrganization(name: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/organizations`, { name }, { headers: this.getHeaders() });
  }

  getAnalyticsSummary(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/analytics/summary`, { headers: this.getHeaders() });
  }

  getAuditLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/audit-logs`, { headers: this.getHeaders() });
  }
}
