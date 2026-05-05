import api from './api'

export const migrationService = {
  async getMigrations(filters = {}) {
    const params = {}
    if (filters.region) params.region = filters.region
    if (filters.gender) params.gender = filters.gender
    if (filters.ageGroup) params.ageGroup = filters.ageGroup
    if (filters.year) params.year = filters.year

    const response = await api.get('/migrations', { params })
    return response.data
  },

  async getFilterOptions() {
    const response = await api.get('/migrations/filters')
    return response.data
  }
}
