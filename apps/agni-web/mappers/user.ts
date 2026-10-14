import type { UserResponse } from "~/types/api/user";
import type { User } from "~/types/ui/user";

export function toUser(data: UserResponse): User { 
    return data
}
