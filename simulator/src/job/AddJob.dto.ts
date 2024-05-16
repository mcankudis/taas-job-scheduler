// todo proper dtos with validation in zod
export interface AddCronJobDTO {
    name: string;
    requestedNodes: number;
    from: string;
    to: string;
    cronPattern: string;
    executionTimeLimitInMs: number;
    executionWindowDeviationInMs: number;
}

export interface AddImmediateJobDTO {
    name: string;
    requestedNodes: number;
    executionTimeLimitInMs: number;
    executionWindowInMs: number;
}

export const isCronJobInput = (
    input: AddCronJobDTO | AddImmediateJobDTO
): input is AddCronJobDTO => {
    return (input as AddCronJobDTO).cronPattern !== undefined;
};
