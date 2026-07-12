export type NotificationType = 'NOUVELLE_DEMANDE' | 'DEMANDE_TRAITEE' | 'NOUVEL_INCIDENT' | 'INCIDENT_TRAITE';
export type RefType = 'DEMANDE_TRANSPORT' | 'INCIDENT';

export interface NotificationResponse {
  id: string;
  type: NotificationType;
  titre: string;
  message: string;
  lu: boolean;
  dateCreation: string;
  refType: RefType;
  refId: string;
  lien: string;
}
