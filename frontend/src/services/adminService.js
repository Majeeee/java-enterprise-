import api from './api'

export const adminService = {
  async getAllUsers() {
    const response = await api.get('/admin/users')
    return response.data
  },

  async enableUser(userId) {
    const response = await api.put(`/admin/users/${userId}/enable`)
    return response.data
  },

  async deleteUser(userId) {
    await api.delete(`/admin/users/${userId}`)
  }
}
