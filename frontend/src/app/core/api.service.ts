import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl;

  get<T = any>(url: string, params?: Record<string, any>) {
    let p = new HttpParams();
    Object.entries(params ?? {}).forEach(([k, v]) => { if (v !== null && v !== undefined) p = p.set(k, String(v)); });
    return this.http.get<T>(this.base + url, { params: p });
  }
  post<T = any>(url: string, body: any = {}) { return this.http.post<T>(this.base + url, body); }
  put<T = any>(url: string, body: any = {}) { return this.http.put<T>(this.base + url, body); }
  patch<T = any>(url: string, body: any = {}) { return this.http.patch<T>(this.base + url, body); }

  /** Telecharge un fichier protege (PDF / Excel) en conservant l'en-tete JWT. */
  download(url: string, filename: string, onError?: (e: any) => void) {
    this.http.get(this.base + url, { responseType: 'blob' }).subscribe({
      next: blob => {
        const a = document.createElement('a');
        const u = URL.createObjectURL(blob);
        a.href = u; a.download = filename; a.click();
        URL.revokeObjectURL(u);
      },
      error: e => onError?.(e)
    });
  }
}
