import { Status } from "./Status";


export interface Project {
    projectId : string,
    projectImageUrl: string | null,
    projectName: string,
    description: string | null,
    status: Status,
    totalCampaigns?: number,
    updatedAt?: string,
}