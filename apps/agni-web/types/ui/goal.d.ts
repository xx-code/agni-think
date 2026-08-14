import type { GoalResponse } from "../api/goal";

export type Goal = Omit<GoalResponse, 'dueDate'> & { dueDate: Date } 