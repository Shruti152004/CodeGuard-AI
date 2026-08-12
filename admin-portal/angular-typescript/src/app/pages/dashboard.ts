import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private adminService = inject(AdminService);
  private router = inject(Router);

  // Signals/Properties
  public users = signal<any[]>([]);
  public organizations = signal<any[]>([]);
  public auditLogs = signal<any[]>([]);
  public summary = signal<any>({
    totalRuns: 0,
    failedRuns: 0,
    averageScore: 0.0,
    aiIssuesCount: 0
  });

  public newOrgName = '';
  public activeTab = 'metrics';

  ngOnInit() {
    this.refreshData();
  }

  refreshData() {
    this.adminService.getUsers().subscribe({
      next: (data) => this.users.set(data),
      error: () => console.warn('Failed to load users')
    });

    this.adminService.getOrganizations().subscribe({
      next: (data) => this.organizations.set(data),
      error: () => console.warn('Failed to load organizations')
    });

    this.adminService.getAnalyticsSummary().subscribe({
      next: (data) => this.summary.set(data),
      error: () => console.warn('Failed to load summary analytics')
    });

    this.adminService.getAuditLogs().subscribe({
      next: (data) => this.auditLogs.set(data),
      error: () => console.warn('Failed to load audit logs')
    });
  }

  setTab(tab: string) {
    this.activeTab = tab;
  }

  changeUserRole(userId: number, newRole: string) {
    this.adminService.updateUserRole(userId, newRole).subscribe({
      next: () => {
        this.refreshData();
        alert('User role updated successfully.');
      },
      error: () => alert('Failed to update user role.')
    });
  }

  createOrg() {
    if (!this.newOrgName.trim()) return;
    this.adminService.createOrganization(this.newOrgName).subscribe({
      next: () => {
        this.newOrgName = '';
        this.refreshData();
        alert('Organization created successfully.');
      },
      error: () => alert('Failed to create organization.')
    });
  }

  handleLogout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
