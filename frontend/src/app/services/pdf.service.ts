import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PdfService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pdf/recu-paiement';

  telechargerRecuPaiement(
    id: number,
    montant: number,
    date: string,
    type: string,
    statut: string,
    nomMembre: string,
    emailMembre: string
  ): Observable<Blob> {
    const params = new HttpParams()
      .set('id', id.toString())
      .set('montant', montant.toString())
      .set('date', date)
      .set('type', type)
      .set('statut', statut)
      .set('nomMembre', nomMembre)
      .set('emailMembre', emailMembre);

    return this.http.get(this.apiUrl, {
      params: params,
      responseType: 'blob'
    });
  }
}
