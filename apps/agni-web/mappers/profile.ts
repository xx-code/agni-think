import type { GetProfileResponse } from "~/types/api/profile";
import type { Profile } from "~/types/ui/profile";

export function profileResponseToProfile(data: GetProfileResponse): Profile {
    return data 
}