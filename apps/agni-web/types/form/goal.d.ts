import type { CalendarDate } from "@internationalized/date";
import type { CreateGoalRequest } from "../api/goal";

export type GoalForm = Omit<CreateGoalRequest, 'targetDate'> & { targetDate: Date }