import type { ListResponse } from "../api";

export type List<T> = ListResponse<T>

export type Deleted = DeletedResponse

export interface ModalOverlayInstance {
  create: (component: Component, props?: Record<string, any>) => any
}

