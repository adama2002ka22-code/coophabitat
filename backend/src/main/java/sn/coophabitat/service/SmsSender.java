package sn.coophabitat.service;

/** Envoi de SMS. L'implementation par defaut journalise ; remplacer par un prestataire (Orange SMS API, Twilio...). */
public interface SmsSender { void envoyer(String telephone, String message); }
