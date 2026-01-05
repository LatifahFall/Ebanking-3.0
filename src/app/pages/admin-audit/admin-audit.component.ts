// ...existing code...
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuditService } from '../../core/services/audit.service';
import { AuditEvent, EventType } from '../../models';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="page">
      <h2>Audit — événements</h2>

      <div class="controls">
        <mat-form-field appearance="outline">
          <mat-label>Utilisateur</mat-label>
          <input matInput placeholder="username or id" [(ngModel)]="filterUser" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Type</mat-label>
          <mat-select [(ngModel)]="filterType">
            <mat-option [value]="''">Tous</mat-option>
            <mat-option *ngFor="let t of eventTypes" [value]="t">{{t}}</mat-option>
          </mat-select>
        </mat-form-field>

        <button mat-flat-button color="primary" (click)="applyFilters()">Filtrer</button>
        <button mat-stroked-button color="accent" (click)="refresh()">Rafraîchir</button>
      </div>

      <div *ngIf="loading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <table mat-table [dataSource]="events" class="mat-elevation-z1" *ngIf="!loading">
        <ng-container matColumnDef="timestamp">
          <th mat-header-cell *matHeaderCellDef>Date</th>
          <td mat-cell *matCellDef="let e">{{e.timestamp | date:'short'}}</td>
        </ng-container>

        <ng-container matColumnDef="eventType">
          <th mat-header-cell *matHeaderCellDef>Type</th>
          <td mat-cell *matCellDef="let e">{{e.eventType}}</td>
        </ng-container>

        <ng-container matColumnDef="username">
          <th mat-header-cell *matHeaderCellDef>Utilisateur</th>
          <td mat-cell *matCellDef="let e">{{e.username || e.userId}}</td>
        </ng-container>

        <ng-container matColumnDef="service">
          <th mat-header-cell *matHeaderCellDef>Service</th>
          <td mat-cell *matCellDef="let e">{{e.service}}</td>
        </ng-container>

        <ng-container matColumnDef="riskScore">
          <th mat-header-cell *matHeaderCellDef>Risque</th>
          <td mat-cell *matCellDef="let e">{{e.riskScore || '-'}}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let e">
            <button mat-icon-button title="Voir" (click)="viewDetails(e)"><mat-icon>visibility</mat-icon></button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
      </table>

      <div class="pager">
        <button mat-button (click)="prevPage()" [disabled]="page === 0">Précédent</button>
        <span>Page {{page + 1}} — {{total}} résultats</span>
        <button mat-button (click)="nextPage()" [disabled]="(page + 1) * size >= total">Suivant</button>
      </div>

      <div class="export">
        <h3>Exporter</h3>
        <label>Start date <input type="datetime-local" [(ngModel)]="startDate" /></label>
        <label>End date <input type="datetime-local" [(ngModel)]="endDate" /></label>
        <button mat-flat-button color="warn" (click)="exportReport()">Exporter (PDF)</button>
        <button mat-flat-button color="accent" (click)="exportCsv()">Exporter (CSV)</button>
        <div *ngIf="exportJobId">Job id: {{exportJobId}}</div>
        <div *ngIf="exportInProgress">Export en cours... <mat-spinner diameter="20"></mat-spinner></div>
        <button *ngIf="exportFileName" mat-flat-button color="accent" (click)="downloadFile()">Télécharger le fichier</button>
      </div>
    </div>
  `,
  styles: [
    `.page { padding: 16px; }
     .controls { display:flex; gap:12px; align-items:center; margin-bottom:12px; }
     table { width:100%; margin-top:12px; }
     .pager { margin-top:12px; display:flex; gap:12px; align-items:center }
     .export { margin-top:24px }
     .loading { display:flex; justify-content:center; margin:24px 0 }
    `
  ]
})
export class AdminAuditComponent {
  events: AuditEvent[] = [];
  displayedColumns = ['timestamp', 'eventType', 'username', 'service', 'riskScore', 'actions'];
  page = 0;
  size = 20;
  total = 0;
  loading = false;

  filterUser = '';
  filterType: string = '';
  eventTypes = Object.values(EventType);

  startDate: string | null = null;
  endDate: string | null = null;
  exportJobId: string | null = null;
  exportInProgress = false;
  exportFileName: string | null = null;

  constructor(private auditService: AuditService) {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.auditService.getEvents(this.page, this.size).subscribe({
      next: res => {
        this.events = res.content;
        this.total = res.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  applyFilters(): void {
    // simple filter: if filterType or filterUser set, use searchEvents
    if (this.filterType || this.filterUser) {
      const opts: any = { page: this.page, size: this.size };
      if (this.filterUser) opts.userId = isNaN(Number(this.filterUser)) ? undefined : Number(this.filterUser);
      if (this.filterUser && isNaN(Number(this.filterUser))) opts.username = this.filterUser;
      if (this.filterType) opts.eventType = this.filterType;
      this.loading = true;
      this.auditService.searchEvents(opts).subscribe({ next: res => { this.events = res.content; this.total = res.totalElements; this.loading = false; }, error: () => { this.loading = false; } });
      return;
    }
    // otherwise normal load
    this.loadEvents();
  }

  refresh(): void { this.loadEvents(); }

  prevPage(): void { if (this.page > 0) { this.page--; this.loadEvents(); } }
  nextPage(): void { if ((this.page + 1) * this.size < this.total) { this.page++; this.loadEvents(); } }

  viewDetails(e: AuditEvent): void { console.log('Audit event', e); alert(JSON.stringify(e)); }

  exportReport(): void {
    if (!this.startDate || !this.endDate) { alert('Veuillez fournir une période'); return; }
    this.exportInProgress = true;
    this.auditService.exportAuditReport(this.startDate, this.endDate, 'PDF').subscribe({ next: job => {
        this.exportJobId = job.jobId;
        // start polling and download when ready
        this.auditService.downloadExportWhenReady(job.jobId).subscribe({
          next: (blob) => {
            this.exportInProgress = false;
            // create filename
            const name = `audit-export-${(new Date()).toISOString().slice(0,19).replace(/[:T]/g,'-')}.pdf`;
            this.exportFileName = name;
            // trigger download
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = name;
            document.body.appendChild(a);
            a.click();
            a.remove();
            setTimeout(() => URL.revokeObjectURL(url), 5000);
          },
          error: (err) => {
            this.exportInProgress = false;
            alert('Échec du téléchargement de l’export : ' + (err?.message || 'timeout'));
          }
        });
      }, error: () => { this.exportInProgress = false; alert('Erreur export'); } });
  }

  exportCsv(): void {
    if (!this.events || this.events.length === 0) { alert('Aucun événement à exporter'); return; }
    const headers = ['id', 'timestamp', 'eventType', 'userId', 'username', 'service', 'riskScore', 'details'];
    const rows = this.events.map(e => {
      const details = typeof e.details === 'object' ? JSON.stringify(e.details) : (e.details || '');
      return [e.id || '', e.timestamp || '', e.eventType || '', e.userId ?? '', e.username || '', e.service || '', e.riskScore ?? '', details];
    });
    const csv = [headers.join(','), ...rows.map(r => r.map(cell => `"${String(cell).replace(/"/g,'""')}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const name = `audit-events-${(new Date()).toISOString().slice(0,19).replace(/[:T]/g,'-')}.csv`;
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 5000);
  }

  downloadFile(): void {
    // noop: actual file downloaded automatically after export. Provide CSV export as alternative.
    alert('Le fichier a été téléchargé automatiquement si disponible. Utilisez Export CSV pour exporter les événements visibles.');
  }
}
// ...existing code...
