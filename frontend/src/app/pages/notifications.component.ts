import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../core/api.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [DatePipe],
  template: `
  <div class="dash-hero villa anim-in">
    <span class="role-chip" style="margin-bottom:.4rem">ALERTES · RAPPELS J-3 · CONVOCATIONS</span>
    <h1>Notifications</h1>
    <span class="muted">Suivi en temps réel de vos rappels d'échéance, paiements, attributions de lots et convocations AG</span>
  </div>
  @for (n of list; track n.id) {
    <div class="card" [style.opacity]="n.lu ? 0.6 : 1">
      <div class="row" style="margin:0"><b>{{ n.sujet }}</b><span class="muted">{{ n.dateCreation | date:'dd/MM/yyyy HH:mm' }}</span></div>
      <p style="margin:.4rem 0">{{ n.message }}</p>
      @if (!n.lu) { <button class="btn" (click)="lu(n)">Marquer comme lue</button> }
    </div>
  } @empty { <p class="muted">Aucune notification.</p> }`
})
export class NotificationsComponent implements OnInit {
  private api = inject(ApiService);
  list: any[] = [];
  ngOnInit() { this.api.get<any[]>('/notifications/me').subscribe(l => this.list = l); }
  lu(n: any) { this.api.post(`/notifications/${n.id}/lu`).subscribe(() => n.lu = true); }
}
