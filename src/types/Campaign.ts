import { Status } from "./Status";


export interface Campaign {
    campaignId : string,
    projectId: string,
    projectName: string,
    campaignName: string ,
    campaignImageUrl: string | null,
    totalBanners?: number,
    description: string | null,
    startDate: string | null, 
    endDate: string | null,
    status: Status,
    updatedAt?: string,
}