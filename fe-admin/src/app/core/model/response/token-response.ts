export interface TokenResponse {
    accessToken: string
    refreshToken: string
    tokenType: string
    accessExpiresIn: number
    refreshExpiresIn: number
}