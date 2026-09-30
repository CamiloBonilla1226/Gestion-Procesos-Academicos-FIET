export interface PaginacionRespuestaDTO<T> {
  content: T[];
  totalElements: number;
}