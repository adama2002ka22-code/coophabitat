export const ROLES_BUREAU = ['PRESIDENT', 'TRESORIER', 'SECRETAIRE'];

export function errMsg(e: any): string {
  if (e?.status === 0) return 'Serveur injoignable (vous êtes peut-être hors ligne).';
  return e?.error?.message || 'Une erreur est survenue.';
}

export const ROLE_LABEL: Record<string, string> = {
  PRESIDENT: 'Président', TRESORIER: 'Trésorier', SECRETAIRE: 'Secrétaire', MEMBRE: 'Membre'
};
