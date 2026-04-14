import api from './api';

/**
 * 流程管理 API
 */
export const flowApi = {
    /**
     * 创建流程
     * @param {Object} flowData - 流程数据（FlowDefinition 格式）
     * @returns {Promise<Object>} 创建结果
     */
    create: async (flowData) => {
        return api.post('/flows', flowData);
    },

    /**
     * 更新流程
     * @param {number} id - 流程ID
     * @param {Object} flowData - 流程数据
     * @returns {Promise<Object>} 更新结果
     */
    update: async (id, flowData) => {
        return api.put(`/flows/${id}`, flowData);
    },

    /**
     * 获取流程
     * @param {number} id - 流程ID
     * @returns {Promise<Object>} 流程详情
     */
    get: async (id) => {
        return api.get(`/flows/${id}`);
    },

    /**
     * 获取流程列表
     * @returns {Promise<Array>} 流程列表
     */
    list: async () => {
        return api.get('/flows');
    },

    /**
     * 删除流程
     * @param {number} id - 流程ID
     */
    delete: async (id) => {
        return api.delete(`/flows/${id}`);
    },

    /**
     * 部署流程
     * @param {number} id - 流程ID
     * @returns {Promise<Object>} 部署结果
     */
    deploy: async (id) => {
        return api.post(`/flows/${id}/deploy`);
    },
};

export default flowApi;
