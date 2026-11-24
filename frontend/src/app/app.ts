import { Component, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    @if (offline) {
      <div class="offline-banner">Mode hors ligne : les dernières données consultées restent visibles, les paiements et saisies sont désactivés.</div>
    }
    <router-outlet />
  `
})
export class App {
  offline = !navigator.onLine;
  @HostListener('window:online') on() { this.offline = false; }
  @HostListener('window:offline') off() { this.offline = true; }
}
