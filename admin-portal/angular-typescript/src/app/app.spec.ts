import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AdminService } from './services/admin.service';
import { of } from 'rxjs';
import { describe, beforeEach, it, expect } from 'vitest';

class MockAdminService {
  getUsers() { return of([]); }
  getOrganizations() { return of([]); }
  getAnalyticsSummary() { return of({ totalRuns: 0, failedRuns: 0, averageScore: 0, aiIssuesCount: 0 }); }
  getAuditLogs() { return of([]); }
}

describe('App Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AdminService, useClass: MockAdminService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should default to metrics tab', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app.activeTab).toBe('metrics');
  });
});
